package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Pair;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactDeletedEventCallback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;
import static java.util.Arrays.asList;

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
        final Context context, RxBusDriver rxBus, final User user,
        final ArrayList<GroupToggle> editGroups, final String contactId) {
        return Observable.just(editGroups)
            .map(userUpdateContacts(user, contactId))
            .map(saveUserToDB(context, rxBus))
            .map(createGroupContactEvent(contactId))
            .toBlocking().single();
    }

    public static Func1<Pair<User, List<Group>>, Pair<User, List<Group>>> saveUserToDB(
        final Context context, final RxBusDriver rxBus) {
        return new Func1<Pair<User, List<Group>>, Pair<User, List<Group>>>() {
            @Override
            public Pair<User, List<Group>> call(Pair<User, List<Group>> userListPair) {
                User newUser = userListPair.first;
                String userId = newUser.id();
                updateRealmUser(context, newUser);
                getUserService(context, rxBus).updateUser(userId, newUser).subscribe();
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

    private static Func1<Pair<User, List<Group>>, List<EventCallback>>
    createGroupContactEvent(final String contactId) {
        return new Func1<Pair<User, List<Group>>, List<EventCallback>>() {
            @Override
            public List<EventCallback> call(Pair<User, List<Group>> groups) {
                if (groups.second.size() > 0) {
                    return new ArrayList<EventCallback>(
                        asList(new UserContactAddedEventCallback(groups.first, contactId),
                            new GroupContactsUpdatedEventCallback(
                                groups.first, contactId, groups.second)));
                } else {
                    //contact in group list is empty, so the contact has been removed from groups
                    return new ArrayList<EventCallback>(
                        asList(new UserContactDeletedEventCallback(groups.first, contactId),
                            new GroupContactsUpdatedEventCallback(
                                groups.first, contactId, groups.second)));
                }
            }
        };
    }

}
