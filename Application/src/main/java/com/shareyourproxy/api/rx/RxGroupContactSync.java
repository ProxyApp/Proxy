package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Pair;

import com.shareyourproxy.api.domain.factory.GroupFactory;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactDeletedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

import static com.shareyourproxy.api.RestClient.getGroupContactService;
import static com.shareyourproxy.api.RestClient.getUserContactService;
import static com.shareyourproxy.api.RestClient.getUserGroupService;
import static com.shareyourproxy.api.rx.RxHelper.filterNullObject;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Update Group contacts and User contacts when they've been added or removed to any groups.
 */
public class RxGroupContactSync {

    /**
     * Private constructor.
     */
    private RxGroupContactSync() {
    }

    public static List<EventCallback> updateGroupContacts(
        final Context context, final User user,
        final ArrayList<GroupEditContact> editGroups, final Contact contact) {
        return Observable.just(editGroups)
            .map(userUpdateContacts(user, contact))
            .map(saveUserToDB(context, contact))
            .map(createGroupContactEvent(contact))
            .toList().toBlocking().single();
    }

    public static Func1<Pair<User, List<Group>>, Pair<User, List<Group>>> saveUserToDB(
        final Context context, final Contact contact) {
        return new Func1<Pair<User, List<Group>>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(Pair<User, List<Group>> userListPair) {
                User newUser = userListPair.first;
                String userId = newUser.id().value();
                String contactId = contact.id().value();
                List<Group> contactInGroup = userListPair.second;
                updateRealmUser(context, newUser);
                if (contactInGroup.size() > 0) {
                    getUserContactService().addUserContact(userId, contactId, contact).subscribe();
                } else {
                    getUserContactService().deleteUserContact(userId, contactId).subscribe();
                }
                getUserGroupService().updateUserGroups(userId, newUser.groups()).subscribe();
                return userListPair;
            }
        };
    }

    private static Func1<ArrayList<GroupEditContact>, Pair<User, List<Group>>> userUpdateContacts(
        final User user, final Contact contact) {
        return new Func1<ArrayList<GroupEditContact>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(ArrayList<GroupEditContact> groupEditContacts) {
                boolean groupHasContact = false;
                String contactId = contact.id().value();
                ArrayList<Group> contactInGroup = new ArrayList<>();
                for (GroupEditContact groupEditContact : groupEditContacts) {
                    String groupId = groupEditContact.getGroup().id().value();
                    if (groupEditContact.hasContact()) {
                        groupHasContact = true;
                        user.groups().get(groupId).contacts().put(contactId, contact);
                        contactInGroup.add(user.groups().get(groupId));
                    } else {
                        user.groups().get(groupId).contacts().remove(contactId);
                    }
                }
                if (groupHasContact) {
                    user.contacts().put(contactId, contact);
                } else {
                    user.contacts().remove(contactId);
                }
                return new Pair<User, List<Group>>(user, contactInGroup);
            }
        };
    }

    private static Func1<Pair<User, List<Group>>, EventCallback> createGroupContactEvent(
        final Contact contact) {
        return new Func1<Pair<User, List<Group>>, EventCallback>() {
            @Override
            public GroupContactsUpdatedEventCallback call(Pair<User, List<Group>> groups) {
                return new GroupContactsUpdatedEventCallback(groups.first, contact, groups.second);
            }
        };
    }

    public static Observable<EventCallback> addGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return rx.Observable.zip(
            saveRealmGroupContact(context, user, editGroup, contact),
            saveFirebaseGroupContact(user.id().value(), editGroup.id().value(), contact),
            zipAddGroupContact());
    }

    public static Observable<EventCallback> deleteGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return rx.Observable.zip(
            deleteRealmGroupContact(context, user, editGroup, contact),
            deleteFirebaseGroupContact(user.id().value(), editGroup.id().value(), contact),
            zipDeleteGroupContact());
    }

    private static Func2<Group, Contact, EventCallback> zipAddGroupContact() {
        return new Func2<Group, Contact, EventCallback>() {
            @Override
            public GroupContactAddedEventCallback call(Group group, Contact contact) {
                return new GroupContactAddedEventCallback(group, contact);
            }
        };
    }

    private static Func2<Group, Contact, EventCallback> zipDeleteGroupContact() {
        return new Func2<Group, Contact, EventCallback>() {
            @Override
            public GroupContactDeletedEventCallback call(Group group, Contact contact) {
                return new GroupContactDeletedEventCallback(group, contact);
            }
        };
    }

    private static rx.Observable<Group> saveRealmGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return Observable.just(editGroup).map(addRealmGroupContact(context, user, contact));
    }

    private static Func1<Group, Group> addRealmGroupContact(
        final Context context, final User user, final Contact contact) {
        return new Func1<Group, Group>() {
            @Override
            public Group call(Group oldGroup) {
                Group newGroup = GroupFactory.addGroupContact(oldGroup, contact);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                updateRealmUser(context, newUser);
                return newGroup;
            }
        };
    }

    private static rx.Observable<Group> deleteRealmGroupContact(
        Context context, User user, Group group, Contact contact) {
        return Observable.just(group)
            .map(deleteRealmGroupContact(context, user, contact));
    }

    private static Func1<Group, Group> deleteRealmGroupContact(
        final Context context, final User user, final Contact contact) {
        return new Func1<Group, Group>() {
            @Override
            public Group call(Group oldGroup) {
                Group newGroup = GroupFactory.deleteGroupContact(oldGroup, contact);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                updateRealmUser(context, newUser);
                return newGroup;
            }
        };
    }

    private static rx.Observable<Contact> saveFirebaseGroupContact(
        String userId, String groupId, Contact contact) {
        return getGroupContactService()
            .addGroupContact(userId, groupId, contact.id().value(), contact);
    }

    private static rx.Observable<Contact> deleteFirebaseGroupContact(
        String userId, String groupId, Contact contact) {
        Observable<Contact> deleteObserver = getGroupContactService()
            .deleteGroupContact(userId, groupId, contact.id().value());
        deleteObserver.subscribe(new JustObserver<Contact>() {
            @Override
            public void onError() {
                Timber.e("error deleting group group");
            }

            @Override
            public void onNext(Contact event) {
                Timber.i("delete group group successful");
            }
        });
        ConnectableObservable<Contact> connectableObservable = deleteObserver.publish();
        return rx.Observable.merge(
            Observable.just(contact), connectableObservable)
            .filter(filterNullObject());
    }

}
