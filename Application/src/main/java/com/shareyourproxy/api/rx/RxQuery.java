package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;
import com.shareyourproxy.api.rx.command.callback.GroupContactsUpdatedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

import static com.shareyourproxy.api.rx.RxHelper.filterNullContact;

/**
 * Query the realm DB.
 */
public class RxQuery {

    public static rx.Observable<ArrayList<User>> queryFilteredUsers(
        Context context, final String userId) {
        return Observable.just(context).map(new Func1<Context, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(Context context) {
                Realm realm = Realm.getInstance(context);
                RealmResults<RealmUser> realmUsers =
                    realm.where(RealmUser.class)
                        .notEqualTo("id", userId).findAll();
                ArrayList<User> users = UserFactory.createModelUsers(realmUsers);
                realm.close();
                return users;
            }
        }).compose(RxHelper.<ArrayList<User>>applySchedulers());
    }

    public static rx.Observable<ArrayList<Channel>> queryPermissionedChannels(
        Context context, final String loggedInUserId, final String contactId) {
        return Observable.just(context).map(getRealmUser(contactId))
            .map(getPermissionedChannels(loggedInUserId))
            .compose(RxHelper.<ArrayList<Channel>>applySchedulers());
    }

    private static Func1<User, ArrayList<Channel>> getPermissionedChannels(
        final String loggedInUserId) {
        return new Func1<User, ArrayList<Channel>>() {
            @Override
            public ArrayList<Channel> call(User contact) {
                HashSet<Channel> setChannels = new HashSet<>();
                for (Group group : contact.groups()) {
                    for (Contact user : group.contacts()) {
                        if (user.id().value().equals(loggedInUserId)) {
                            setChannels.addAll(group.channels());
                        }
                    }

                }
                return new ArrayList<>(setChannels);
            }
        };
    }

    public static BlockingObservable<User> queryUser(Context context, final String userId) {
        return Observable.just(context).map(getRealmUser(userId)).compose(RxHelper
            .<User>applySchedulers()).toBlocking();
    }

    private static Func1<Context, User> getRealmUser(final String userId) {
        return new Func1<Context, User>() {
            @Override
            public User call(Context context) {
                Realm realm = Realm.getInstance(context);
                RealmUser realmUser =
                    realm.where(RealmUser.class).contains("id", userId).findFirst();
                User user = UserFactory.createModelUser(realmUser);
                realm.close();
                return user;
            }
        };
    }

    public static Func1<String, ArrayList<User>> searchUserString(
        final Context context, final String userId) {
        return new Func1<String, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(String username) {
                return updateSearchText(context, userId, username);
            }
        };
    }

    private static ArrayList<User> updateSearchText(
        Context context, final String userId, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        Realm realm = Realm.getInstance(context);

        realmUsers = realm.where(RealmUser.class)
            .notEqualTo("id", userId)
            .beginGroup()
            .contains("first", constraint.toString(), false)
            .or().contains("last", constraint.toString(), false)
            .or().contains("fullName", constraint.toString(), false)
            .endGroup()
            .findAll();

        ArrayList<User> users = UserFactory.createModelUsers(realmUsers);
        realm.close();
        return users;
    }

    public static List<GroupEditContact> queryContactGroups(
        final User user, final Contact selectedContact) {
        return Observable.from(user.groups())
            .map(mapContacts(selectedContact))
            .toList()
            .toBlocking().single();
    }

    private static Func1<Group, GroupEditContact> mapContacts(final Contact selectedContact) {
        return new Func1<Group, GroupEditContact>() {
            @Override
            public GroupEditContact call(Group group) {
                ArrayList<Contact> contacts = group.contacts();
                if (contacts != null && contacts.size() > 0) {
                    boolean hasContact = false;
                    for (Contact contact : contacts) {
                        if (contact.id().value().equals(selectedContact.id().value())) {
                            hasContact = true;
                            break;
                        }
                    }
                    return new GroupEditContact(group, hasContact);
                }
                return new GroupEditContact(group, false);
            }
        };
    }

    public static GroupContactsUpdatedEvent queryContactGroups(
        ArrayList<GroupEditContact> groupEditContact, final Contact selectedContact) {
        return Observable.from(groupEditContact).map(filterSelectedGroups())
            .filter(filterNullContact())
            .toList()
            .map(packageGroupContacts(selectedContact))
            .toBlocking().single();
    }

    private static Func1<GroupEditContact, Group> filterSelectedGroups() {
        return new Func1<GroupEditContact, Group>() {
            @Override
            public Group call(GroupEditContact editContact) {

                if (editContact.hasContact()) {
                    return editContact.getGroup();
                } else {
                    return null;
                }
            }
        };
    }

    private static Func1<List<Group>, GroupContactsUpdatedEvent> packageGroupContacts(final
                                                                                      Contact
                                                                                          selectedContact) {
        return new Func1<List<Group>, GroupContactsUpdatedEvent>() {
            @Override
            public GroupContactsUpdatedEvent call(List<Group> groups) {
                return new GroupContactsUpdatedEvent(
                    selectedContact, new ArrayList<Group>(groups));
            }
        };
    }

}
