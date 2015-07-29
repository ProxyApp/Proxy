package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Pair;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getGroupContactService;
import static com.shareyourproxy.api.RestClient.getUserContactService;
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
        final ArrayList<GroupEditContact> editGroups, final String contactId) {
        return Observable.just(editGroups)
            .map(userUpdateContacts(user, contactId))
            .map(saveUserToDB(context, contactId))
            .map(createGroupContactEvent(contactId))
            .toBlocking().single();
    }

    public static Func1<Pair<User, List<Group>>, Pair<User, List<Group>>> saveUserToDB(
        final Context context, final String contactId) {
        return new Func1<Pair<User, List<Group>>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(Pair<User, List<Group>> userListPair) {
                User newUser = userListPair.first;
                String userId = newUser.id().value();
                List<Group> contactInGroup = userListPair.second;
                updateRealmUser(context, newUser);
                if (contactInGroup.size() > 0) {
                    getUserContactService().addUserContact(userId, contactId, Id.create(contactId))
                        .subscribe();
                } else {
                    getUserContactService().deleteUserContact(userId, contactId).subscribe();
                }
                for(Group group : contactInGroup){
                    getGroupContactService().addGroupContact(userId, group.id().value(), contactId,
                        Id.create(contactId)).subscribe();
                }
                return userListPair;
            }
        };
    }

    private static Func1<ArrayList<GroupEditContact>, Pair<User, List<Group>>> userUpdateContacts(
        final User user, final String contactId) {
        return new Func1<ArrayList<GroupEditContact>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(ArrayList<GroupEditContact> groupEditContacts) {
                boolean groupHasContact = false;
                ArrayList<Group> contactInGroup = new ArrayList<>();
                for (GroupEditContact groupEditContact : groupEditContacts) {
                    String groupId = groupEditContact.getGroup().id().value();
                    if (groupEditContact.isChecked()) {
                        groupHasContact = true;
                        user.groups().get(groupId).contacts().put(contactId, Id.create(contactId));
                        contactInGroup.add(user.groups().get(groupId));
                    } else {
                        user.groups().get(groupId).contacts().remove(contactId);
                    }
                }
                if (groupHasContact) {
                    user.contacts().put(contactId, Id.create(contactId));
                } else {
                    user.contacts().remove(contactId);
                }
                return new Pair<User, List<Group>>(user, contactInGroup);
            }
        };
    }

    private static Func1<Pair<User, List<Group>>, List<EventCallback>> createGroupContactEvent(
        final String contactId) {
        return new Func1<Pair<User, List<Group>>, List<EventCallback>>() {
            @Override
            public List<EventCallback> call(Pair<User, List<Group>> groups) {
                ArrayList<EventCallback> events = new ArrayList<>(2);
                events.add(new UserContactAddedEventCallback(groups.first, contactId));
                events.add(
                    new GroupContactsUpdatedEventCallback(groups.first, contactId, groups.second));
                return events;
            }
        };
    }

}
