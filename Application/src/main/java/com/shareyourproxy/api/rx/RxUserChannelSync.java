package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.factory.UserFactory;
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
import static com.shareyourproxy.api.rx.RxHelper.addRealmUser;


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
        final Context context, final User oldUser, final Channel oldChannel,
        final Channel newChannel) {
        return Observable.just(oldUser)
            .map(putUserChannel(newChannel))
            .map(addRealmUser(context))
            .map(saveChannelToFirebase(newChannel))
            .map(userChannelAddedEventCallback(oldChannel, newChannel))
            .toList().toBlocking().single();
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

    private static Func1<User, User> saveChannelToFirebase(final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                String userId = user.id().value();
                String channelId = channel.id().value();

                getUserChannelService()
                    .addUserChannel(userId, channelId, channel)
                    .subscribe();
                getUserGroupService()
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

    public static List<EventCallback> deleteChannel(
        Context context, User oldUser, Channel channel) {
        return Observable.just(oldUser)
            .map(removeUserChannel(channel))
            .map(addRealmUser(context))
            .map(deleteChannelFromFirebase(channel))
            .map(userChannelDeletedEventCallback(channel))
            .toList().toBlocking().single();
    }

    private static Func1<User, User> removeUserChannel(final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User oldUser) {
                User newUser = UserFactory.deleteUserChannel(oldUser, channel);
                return newUser;
            }
        };
    }

    private static Func1<User, User> deleteChannelFromFirebase(final Channel channel) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                String userId = user.id().value();
                String channelId = channel.id().value();
                getUserChannelService()
                    .deleteUserChannel(userId, channelId).subscribe();
                getUserGroupService()
                    .updateUserGroups(userId, user.groups())
                    .subscribe();
                return user;
            }
        };
    }

    private static Func1<User, EventCallback> userChannelDeletedEventCallback(
        final Channel channel) {
        return new Func1<User, EventCallback>() {
            @Override
            public EventCallback call(User user) {
                return new UserChannelDeletedEventCallback(user, channel);
            }
        };
    }
}
