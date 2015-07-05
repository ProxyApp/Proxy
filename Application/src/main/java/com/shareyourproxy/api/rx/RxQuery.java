package com.shareyourproxy.api.rx;


import android.content.Context;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.rx.RxHelper.filterNullObject;

/**
 * Query the realm DB for data.
 */
public class RxQuery {
    @DebugLog
    public static rx.Observable<HashMap<String, User>> queryFilteredUsers(
        Context context, final String userId) {
        return Observable.just(context).map(new Func1<Context, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(Context context) {
                Realm realm = Realm.getInstance(context);
                realm.refresh();
                realm.beginTransaction();
                RealmResults<RealmUser> realmUsers =
                    realm.where(RealmUser.class).notEqualTo("id", userId).findAll();
                HashMap<String, User> users = UserFactory.createModelUsers(realmUsers);
                realm.commitTransaction();
                realm.close();
                return users;
            }
        }).compose(RxHelper.<HashMap<String, User>>applySchedulers());
    }

    @DebugLog
    public static rx.Observable<HashMap<String, Channel>> queryPermissionedChannels(
        Context context, final String loggedInUserId, final String contactId) {
        return Observable.just(context).map(getRealmUser(contactId))
            .map(getPermissionedChannels(loggedInUserId))
            .compose(RxHelper.<HashMap<String, Channel>>applySchedulers());
    }

    private static Func1<User, HashMap<String, Channel>> getPermissionedChannels(
        final String loggedInUserId) {
        return new Func1<User, HashMap<String, Channel>>() {
            @Override
            public HashMap<String, Channel> call(User contact) {
                HashMap<String, Channel> setChannels = new HashMap<>();
                for (Map.Entry<String, Group> entryGroup : contact.groups().entrySet()) {
                    for (Map.Entry<String, Contact> entryUser :
                        entryGroup.getValue().contacts().entrySet()) {
                        if (entryUser.getKey().equals(loggedInUserId)) {
                            setChannels.putAll(entryGroup.getValue().channels());
                        }
                    }
                }
                return new HashMap<>(setChannels);
            }
        };
    }

    @DebugLog
    public static User queryUser(Context context, final String userId) {
        return Observable.just(context).map(getRealmUser(userId)).compose(RxHelper
            .<User>applySchedulers()).toBlocking().single();
    }

    private static Func1<Context, User> getRealmUser(final String userId) {
        return new Func1<Context, User>() {
            @Override
            public User call(Context context) {
                Realm realm = Realm.getInstance(context);
                realm.refresh();
                realm.beginTransaction();
                RealmUser realmUser =
                    realm.where(RealmUser.class).contains("id", userId).findFirst();
                User user = UserFactory.createModelUser(realmUser);
                realm.commitTransaction();
                realm.close();
                return user;
            }
        };
    }

    public static Func1<String, HashMap<String, User>> searchUserString(
        final Context context, final String userId) {
        return new Func1<String, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(String username) {
                return updateSearchText(context, userId, username);
            }
        };
    }

    private static HashMap<String, User> updateSearchText(
        Context context, final String userId, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        realm.beginTransaction();
        realmUsers = realm.where(RealmUser.class)
            .notEqualTo("id", userId)
            .beginGroup()
            .contains("first", constraint.toString(), false)
            .or().contains("last", constraint.toString(), false)
            .or().contains("fullName", constraint.toString(), false)
            .endGroup()
            .findAll();

        HashMap<String, User> users = UserFactory.createModelUsers(realmUsers);
        realm.commitTransaction();
        realm.close();
        return users;
    }

    public static List<GroupEditContact> queryContactGroups(
        @NonNull final User user, final Contact selectedContact) {
        return Observable.from(user.groups().entrySet())
            .map(mapContacts(selectedContact))
            .toList()
            .toBlocking().single();
    }

    private static Func1<Map.Entry<String, Group>, GroupEditContact> mapContacts(
        final Contact selectedContact) {
        return new Func1<Map.Entry<String, Group>, GroupEditContact>() {
            @Override
            public GroupEditContact call(Map.Entry<String, Group> groupEntry) {
                Group group = groupEntry.getValue();
                HashMap<String, Contact> contacts = group.contacts();
                if (contacts != null && contacts.size() > 0) {
                    boolean hasContact = false;
                    for (Map.Entry<String, Contact> entryContacts : contacts.entrySet()) {
                        if (entryContacts.getKey().equals(selectedContact.id().value())) {
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

    public static GroupContactsUpdatedEventCallback queryContactGroups(
        ArrayList<GroupEditContact> groupEditContact, final Contact selectedContact) {
        return Observable.from(groupEditContact).map(filterSelectedGroups())
            .filter(filterNullObject())
            .toList()
            .map(packageGroupContacts(selectedContact))
            .toBlocking().single();
    }

    private static Func1<GroupEditContact, Group> filterSelectedGroups() {
        return new Func1<GroupEditContact, Group>() {
            @Override
            public Group call(GroupEditContact editContact) {
                return editContact.hasContact() ? editContact.getGroup() : null;
            }
        };
    }

    private static Func1<List<Group>, GroupContactsUpdatedEventCallback>
    packageGroupContacts(final Contact selectedContact) {
        return new Func1<List<Group>, GroupContactsUpdatedEventCallback>() {
            @Override
            public GroupContactsUpdatedEventCallback call(List<Group> groups) {
                return new GroupContactsUpdatedEventCallback(selectedContact, groups);
            }
        };
    }

}
