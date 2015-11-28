package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Pair;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserService;
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

    public static EventCallback updateGroupContacts(
        final Context context, final User user,
        final ArrayList<GroupToggle> editGroups, final User contact) {
        return Observable.just(editGroups)
            .map(userUpdateContacts(user, contact.id()))
            .map(saveUserToDB(context, contact))
            .map(createGroupContactEvent(contact.id()))
            .toBlocking().single();
    }

    public static Func1<Pair<User, List<Group>>, Pair<User, List<Group>>> saveUserToDB(
        final Context context, final User contact) {
        return new Func1<Pair<User, List<Group>>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(Pair<User, List<Group>> userListPair) {
                User newUser = userListPair.first;
                String userId = newUser.id();
                updateRealmUser(context, newUser);
                updateRealmUser(context, contact);
                getUserService(context).updateUser(userId, newUser).subscribe();
                return userListPair;
            }
        };
    }

    private static Func1<ArrayList<GroupToggle>, Pair<User, List<Group>>> userUpdateContacts(
        final User user, final String contactId) {
        return new Func1<ArrayList<GroupToggle>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(ArrayList<GroupToggle> groupToggles) {
                boolean groupHasContact = false;
                ArrayList<Group> contactInGroup = new ArrayList<>();
                for (GroupToggle groupToggle : groupToggles) {
                    String groupId = groupToggle.getGroup().id();
                    if (groupToggle.isChecked()) {
                        groupHasContact = true;
                        user.groups().get(groupId).contacts().add(contactId);
                        contactInGroup.add(user.groups().get(groupId));
                    } else {
                        user.groups().get(groupId).contacts().remove(contactId);
                    }
                }
                if (groupHasContact) {
                    user.contacts().add(contactId);
                } else {
                    user.contacts().remove(contactId);
                }
                return new Pair<User, List<Group>>(user, contactInGroup);
            }
        };
    }

    private static Func1<Pair<User, List<Group>>, EventCallback>
    createGroupContactEvent(final String contactId) {
        return new Func1<Pair<User, List<Group>>, EventCallback>() {
            @Override
            public EventCallback call(Pair<User, List<Group>> groups) {
                return new GroupContactsUpdatedEventCallback(
                    groups.first, contactId, groups.second);
            }
        };
    }

}
