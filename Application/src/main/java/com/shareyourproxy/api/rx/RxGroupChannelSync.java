package com.shareyourproxy.api.rx;

import android.content.Context;
import android.support.v7.util.SortedList;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.GroupFactory;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelToggle;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.SharedLink;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.PublicChannelsUpdatedEvent;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback;
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.shareyourproxy.api.RestClient.getUserChannelService;
import static com.shareyourproxy.api.RestClient.getUserGroupService;
import static com.shareyourproxy.api.domain.factory.ChannelFactory.createPublicChannel;
import static com.shareyourproxy.api.domain.factory.UserFactory.addUserGroups;
import static com.shareyourproxy.api.domain.factory.UserFactory.addUserPublicChannels;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Update a groups channel map, or update a channel in all groups.
 */
public class RxGroupChannelSync {
    private RxGroupChannelSync() {
    }

    public static List<EventCallback> addUserGroupsChannel(
        final Context context, RxBusDriver rxBus, final User user,
        final ArrayList<GroupToggle> groups, final Channel channel) {
        return Observable.create(addChannelToSelectedGroups(groups, channel))
            .map(zipAndSaveGroups(context, rxBus, user))
            .toList().toBlocking().single();
    }

    public static Observable.OnSubscribe<HashMap<String, Group>> addChannelToSelectedGroups(
        final ArrayList<GroupToggle> groups, final Channel channel) {
        return new Observable.OnSubscribe<HashMap<String, Group>>() {
            @Override
            public void call(Subscriber<? super HashMap<String, Group>> subscriber) {
                try {
                    String channelId = channel.id();
                    HashMap<String, Group> newGroups = new HashMap<>(groups.size());
                    for (GroupToggle entryGroup : groups) {
                        if (entryGroup.isChecked()) {
                            entryGroup.getGroup().channels().add(channelId);
                        }
                        newGroups.put(entryGroup.getGroup().id(), entryGroup.getGroup());
                    }
                    subscriber.onNext(newGroups);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        };
    }

    public static List<EventCallback> updateGroupChannels(
        Context context, RxBusDriver rxBus, User user,
        String newTitle, Group oldGroup, HashSet<String> channels,
        GroupEditType groupEditType) {
        return Observable.zip(
            saveRealmGroupChannels(context, user, newTitle, oldGroup, channels),
            saveFirebaseGroupChannels(context, user.id(), rxBus, newTitle, oldGroup, channels),
            zipAddGroupChannels(user, channels, groupEditType))
            .map(saveSharedLink(context, rxBus))
            .toList().toBlocking().single();
    }

    public static List<EventCallback> updatePublicGroupChannels(
        Context context, RxBusDriver rxBus, User user, ArrayList<ChannelToggle> channels) {
        HashMap<String, Channel> newChannels = new HashMap<>(channels.size());
        for (int i = 0; i < channels.size(); i++) {
            ChannelToggle channelToggle = channels.get(i);
            Channel channel = channelToggle.getChannel();
            Boolean isPublic = channelToggle.inGroup();

            Channel newChannel = createPublicChannel(channel, isPublic);
            newChannels.put(newChannel.id(), newChannel);
        }
        return Observable.zip(
            saveRealmPublicGroupChannels(context, user, newChannels),
            saveFirebasePublicChannels(context, user.id(), rxBus, newChannels),
            zipAddPublicChannels())
            .toList().toBlocking().single();
    }

    public static HashSet<String> getSelectedChannels(
        SortedList<ChannelToggle> channels) {
        return Observable.just(channels).map(getSelectedChannels()).toBlocking().single();
    }

    private static Func1<HashMap<String, Group>, EventCallback> zipAndSaveGroups(
        final Context context, final RxBusDriver rxBus, final User user) {
        return new Func1<HashMap<String, Group>, EventCallback>() {
            @Override
            public EventCallback call(HashMap<String, Group> newGroups) {
                return Observable.zip(
                    saveRealmGroupChannels(context, user, newGroups),
                    saveFirebaseUserGroups(context, rxBus, user.id(), newGroups),
                    zipAddGroupsChannel()).toBlocking().single();
            }
        };
    }

    private static Func2<User, Group, EventCallback> zipAddGroupsChannel() {
        return new Func2<User, Group, EventCallback>() {
            @Override
            public UserGroupAddedEventCallback call(User user, Group group) {
                return new UserGroupAddedEventCallback(user, group);
            }
        };
    }

    private static Func1<GroupChannelsUpdatedEventCallback, EventCallback> saveSharedLink(
        final Context context, final RxBusDriver rxBus) {
        return new Func1<GroupChannelsUpdatedEventCallback, EventCallback>() {
            @Override
            public GroupChannelsUpdatedEventCallback call(GroupChannelsUpdatedEventCallback event) {
                SharedLink link = SharedLink.create(event.user, event.group);
                RestClient.getSharedLinkService(context, rxBus).addSharedLink(link.id(), link)
                    .subscribe();
                return event;
            }
        };
    }

    private static Observable<User> saveRealmGroupChannels(
        final Context context, final User user, final HashMap<String, Group> groups) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    User newUser = addUserGroups(user, groups);
                    updateRealmUser(context, newUser);
                    subscriber.onNext(newUser);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<User> saveRealmPublicGroupChannels(
        final Context context, final User user, final HashMap<String, Channel> channels) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    User newUser = addUserPublicChannels(user, channels);
                    updateRealmUser(context, newUser);
                    subscriber.onNext(newUser);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Observable<Group> saveFirebaseUserGroups(
        Context context, RxBusDriver rxBus, String userId, HashMap<String, Group> groups) {
        return RestClient.getUserGroupService(context, rxBus).updateUserGroups(userId, groups);
    }

    private static Func2<User, HashMap<String, Channel>, EventCallback> zipAddPublicChannels() {
        return new Func2<User, HashMap<String, Channel>, EventCallback>() {
            @Override
            public EventCallback call(User user, HashMap<String, Channel> newChannels) {
                return new PublicChannelsUpdatedEvent(user, newChannels);
            }
        };
    }

    private static Observable<HashMap<String, Channel>> saveFirebasePublicChannels(
        Context context, String userId, RxBusDriver rxBus, HashMap<String, Channel> newChannels) {
        return getUserChannelService(context, rxBus).addUserChannels(userId, newChannels);
    }


    private static Func1<SortedList<ChannelToggle>, HashSet<String>>
    getSelectedChannels() {
        return new Func1<SortedList<ChannelToggle>, HashSet<String>>() {
            @Override
            public HashSet<String> call(SortedList<ChannelToggle> groupEditChannels) {
                HashSet<String> selectedChannels = new HashSet<>();
                for (int i = 0; i < groupEditChannels.size(); i++) {
                    ChannelToggle editChannel = groupEditChannels.get(i);
                    if (editChannel.inGroup()) {
                        Channel channel = editChannel.getChannel();
                        selectedChannels.add(channel.id());
                    }
                }
                return selectedChannels;
            }
        };
    }

    private static rx.Observable<Group> saveRealmGroupChannels(
        Context context, User user, String newTitle, Group oldGroup,
        HashSet<String> channels) {
        return Observable.just(channels)
            .map(addRealmGroupChannels(context, user, newTitle, oldGroup));
    }

    private static Func1<HashSet<String>, Group> addRealmGroupChannels(
        final Context context, final User user, final String newTitle, final Group oldGroup) {
        return new Func1<HashSet<String>, Group>() {
            @Override
            public Group call(HashSet<String> channels) {
                Group newGroup = GroupFactory.addGroupChannels(newTitle, oldGroup, channels);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                updateRealmUser(context, newUser);
                return newGroup;
            }
        };
    }

    private static Observable<Group> saveFirebaseGroupChannels(
        Context context, String userId, RxBusDriver rxBus, String newTitle,
        Group group, HashSet<String> channels) {
        String groupId = group.id();
        Group newGroup = Group.copy(group, newTitle, channels);
        return getUserGroupService(context, rxBus).addUserGroup(userId, groupId, newGroup);
    }

    private static Func2<Group, Group, GroupChannelsUpdatedEventCallback> zipAddGroupChannels(
        final User user, final HashSet<String> channels, final GroupEditType groupEditType) {
        return new Func2<Group, Group, GroupChannelsUpdatedEventCallback>() {
            @Override
            public GroupChannelsUpdatedEventCallback call(Group group, Group group2) {
                return new GroupChannelsUpdatedEventCallback(user, group, channels, groupEditType);
            }
        };
    }
}
