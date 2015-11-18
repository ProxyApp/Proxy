package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserChannelService;
import static com.shareyourproxy.api.RestClient.getUserGroupService;
import static com.shareyourproxy.api.domain.factory.UserFactory.addUserChannel;
import static com.shareyourproxy.api.domain.factory.UserFactory.deleteUserChannel;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;


/**
 * Sync newChannel operations.
 */
public class RxUserChannelSync {

    /**
     * Private constructor.
     */
    private RxUserChannelSync() {
    }

    public static List<EventCallback> saveUserChannel(
        Context context, User oldUser, Channel oldChannel, Channel newChannel) {
        return Observable.just(oldUser)
            .map(putUserChannel(newChannel))
            .map(addRealmUser(context))
            .map(saveChannelToFirebase(context, newChannel))
            .map(userChannelAddedEventCallback(oldChannel, newChannel))
            .toList().toBlocking().single();
    }

    public static List<EventCallback> deleteChannel(
        Context context, User oldUser, Channel channel, int position) {
        return Observable.just(oldUser)
            .map(removeUserChannel(channel))
            .map(addRealmUser(context))
            .map(deleteChannelFromFirebase(context, channel))
            .map(userChannelDeletedEventCallback(channel, position))
            .toList().toBlocking().single();
    }

    private static Func1<User, User> addRealmUser(final Context context) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                updateRealmUser(context, user);
                return user;
            }
        };
    }

    /**
     * Add the new channel to the users channel list and all groups.
     *
     * @param newChannel
     * @return
     */
    private static Func1<User, User> putUserChannel(final Channel newChannel) {
        return new Func1<User, User>() {
            @Override
            public User call(User oldUser) {
                User newUser = addUserChannel(oldUser, newChannel);
                return newUser;
            }
        };
    }

    private static Func1<User, User> saveChannelToFirebase(
        final Context context, final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                String userId = user.id();
                String channelId = channel.id();

                getUserChannelService(context)
                    .addUserChannel(userId, channelId, channel)
                    .subscribe();
                getUserGroupService(context)
                    .updateUserGroups(userId, user.groups())
                    .subscribe();
                return user;
            }
        };
    }

    private static Func1<User, EventCallback> userChannelAddedEventCallback(
        final Channel oldChannel, final Channel newChannel) {
        return new Func1<User, EventCallback>() {
            @Override
            public EventCallback call(User user) {
                return new UserChannelAddedEventCallback(user, oldChannel, newChannel);
            }
        };
    }

    private static Func1<User, User> removeUserChannel(final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User oldUser) {
                User newUser = deleteUserChannel(oldUser, channel);
                return newUser;
            }
        };
    }

    private static Func1<User, User> deleteChannelFromFirebase(
        final Context context, final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                String userId = user.id();
                String channelId = channel.id();
                getUserChannelService(context)
                    .deleteUserChannel(userId, channelId).subscribe();
                getUserGroupService(context)
                    .updateUserGroups(userId, user.groups())
                    .subscribe();
                return user;
            }
        };
    }

    private static Func1<User, EventCallback> userChannelDeletedEventCallback(
        final Channel channel, final int position) {
        return new Func1<User, EventCallback>() {
            @Override
            public EventCallback call(User user) {
                return new UserChannelDeletedEventCallback(user, channel, position);
            }
        };
    }
}
