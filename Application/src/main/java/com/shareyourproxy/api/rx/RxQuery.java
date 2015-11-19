package com.shareyourproxy.api.rx;


import android.content.Context;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.domain.factory.UserFactory.createModelUser;
import static com.shareyourproxy.api.rx.RxHelper.filterNullObject;

/**
 * Query the realm DB for data.
 */
public class RxQuery {

    public static HashMap<String, User> queryUserContacts(
        Context context, final HashSet<String> contactIds) {
        return Observable.just(context).map(new Func1<Context, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(Context context) {
                HashMap<String, User> contacts;
                if (contactIds != null) {
                    contacts = new HashMap<>(contactIds.size());
                } else {
                    return new HashMap<>();
                }
                Realm realm = Realm.getInstance(context);
                realm.refresh();
                for (String contactId : contactIds) {
                    RealmUser realmUser =
                        realm.where(RealmUser.class).equalTo("id", contactId).findFirst();
                    if (realmUser != null) {
                        contacts.put(contactId, createModelUser(realmUser));
                    }
                }
                realm.close();
                return contacts;
            }
        }).toBlocking().first();
    }

    public static rx.Observable<HashMap<String, Channel>> queryPermissionedChannels(
        final User user, final String loggedInUserId) {
        return Observable.just(user)
            .map(getPermissionedChannels(loggedInUserId))
            .compose(RxHelper.<HashMap<String, Channel>>applySchedulers());
    }

    private static Func1<User, HashMap<String, Channel>> getPermissionedChannels(
        final String loggedInUserId) {
        return new Func1<User, HashMap<String, Channel>>() {
            @Override
            public HashMap<String, Channel> call(User contact) {
                ArrayList<String> permissionedIds = new ArrayList<>();
                HashMap<String, Channel> permissionedChannels = new HashMap<>();
                HashMap<String, Channel> channels = contact.channels();
                //escape early
                if (channels == null || channels.size() == 0) {
                    return permissionedChannels;
                } else {
                    HashMap<String, Group> groups = contact.groups();
                    //check the contacts groups for the logged in user and gather the channel
                    // Id's of that group
                    if (groups != null && groups.size() > 0) {
                        for (Group group : groups.values()) {
                            HashSet<String> contacts = group.contacts();
                            if (contacts != null) {
                                for (String contactId : contacts) {
                                    if (contactId.equals(loggedInUserId)) {
                                        permissionedIds.addAll(group.channels());
                                    }
                                }
                            }
                        }
                        // for the above key set data, find the channels associated
                        for (String channelId : permissionedIds) {
                            Channel channel = contact.channels().get(channelId);
                            if (channel != null) {
                                permissionedChannels.put(channel.id(), channel);
                            }
                        }
                    }
                    // add public channels
                    for (Channel channel : channels.values()) {
                        if (channel.isPublic()) {
                            permissionedChannels.put(channel.id(), channel);
                        }
                    }
                    return permissionedChannels;
                }
            }
        };
    }

    public static User queryUser(Context context, final String userId) {
        return Observable.just(context).map(getRealmUser(userId)).compose(RxHelper
            .<User>applySchedulers()).toBlocking().single();
    }

    public static Observable<Integer> getUserContactScore(Context context, final String userId) {
        return RestClient.getHerokuUserervice(context).userFollowerCount(userId).compose(RxHelper
            .<Integer>applySchedulers());
    }

    private static Func1<Context, User> getRealmUser(final String userId) {
        return new Func1<Context, User>() {
            @Override
            public User call(Context context) {
                return getRealmUser(context, userId);
            }
        };
    }

    public static User getRealmUser(Context context, final String userId) {
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        RealmUser realmUser =
            realm.where(RealmUser.class).contains("id", userId).findFirst();
        User user = createModelUser(realmUser);
        realm.close();
        return user;
    }

    public static Observable<HashMap<String, User>> searchMatchingUsers(
        final Context context, final String queryName, final String userId) {
        return Observable.concat(
            searchLocalMatchingUsers(context, queryName, userId),
            searchRemoteMatchingUsers(context, queryName));

    }

    private static Observable<HashMap<String, User>> searchLocalMatchingUsers(
        Context context, final String queryName, final String userId) {
        return Observable.just(queryName).map(searchLocalUserString(context, userId));
    }

    private static Observable<HashMap<String, User>> searchRemoteMatchingUsers(
        Context context, final String queryName) {
        return Observable.just(queryName).map(searchRemoteUserString(context));
    }

    private static Func1<String, HashMap<String, User>> searchLocalUserString(
        final Context context, final String userId) {
        return new Func1<String, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(String username) {
                return matchLocalUsers(context, userId, username);
            }
        };
    }

    private static HashMap<String, User> matchLocalUsers(
        Context context, final String userId, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        realm.beginTransaction();
        realmUsers = realm.where(RealmUser.class)
            .notEqualTo("id", userId)
            .beginsWith("fullName", constraint.toString(), false)
            .findAll();
        HashMap<String, User> users = UserFactory.createModelUsers(realmUsers);
        realm.commitTransaction();
        realm.close();
        return users;
    }

    private static Func1<String, HashMap<String, User>> searchRemoteUserString(
        final Context context) {
        return new Func1<String, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(String queryName) {
                return RestClient.getHerokuUserervice(context)
                    .searchUsers(queryName).map(arrayToUserHashMap()).toBlocking().single();
            }
        };
    }

    public static Func1<ArrayList<User>, HashMap<String, User>> arrayToUserHashMap() {
        return new Func1<ArrayList<User>, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(ArrayList<User> users) {
                HashMap<String, User> userMap = new HashMap<>(users.size());
                for (User user : users) {
                    userMap.put(user.id(), user);
                }
                return userMap;
            }
        };
    }

    public static List<GroupToggle> queryContactGroups(
        @NonNull final User user, final User selectedContact) {
        return Observable.from(user.groups().entrySet())
            .map(mapContacts(selectedContact))
            .toList()
            .toBlocking().single();
    }

    private static Func1<Map.Entry<String, Group>, GroupToggle> mapContacts(
        final User selectedContact) {
        return new Func1<Map.Entry<String, Group>, GroupToggle>() {
            @Override
            public GroupToggle call(Map.Entry<String, Group> groupEntry) {
                Group group = groupEntry.getValue();
                String contactId = selectedContact.id();
                HashSet<String> contacts = group.contacts();
                if (contacts != null &&
                    group.contacts().contains(contactId)) {
                    return new GroupToggle(group, true);
                }
                return new GroupToggle(group, false);
            }
        };
    }

    public static GroupContactsUpdatedEventCallback queryContactGroups(
        User user,
        ArrayList<GroupToggle> groupToggle, final String contactId) {
        return Observable.from(groupToggle).map(filterSelectedGroups())
            .filter(filterNullObject())
            .toList()
            .map(packageGroupContacts(user, contactId))
            .toBlocking().single();
    }

    private static Func1<GroupToggle, Group> filterSelectedGroups() {
        return new Func1<GroupToggle, Group>() {
            @Override
            public Group call(GroupToggle editContact) {
                return editContact.isChecked() ? editContact.getGroup() : null;
            }
        };
    }

    private static Func1<List<Group>, GroupContactsUpdatedEventCallback>
    packageGroupContacts(final User user, final String contactId) {
        return new Func1<List<Group>, GroupContactsUpdatedEventCallback>() {
            @Override
            public GroupContactsUpdatedEventCallback call(List<Group> groups) {
                return new GroupContactsUpdatedEventCallback(user, contactId, groups);
            }
        };
    }

}
