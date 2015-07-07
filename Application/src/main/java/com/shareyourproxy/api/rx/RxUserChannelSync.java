package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.factory.GroupFactory;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

import static com.shareyourproxy.api.RestClient.getUserChannelService;
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

    public static List<EventCallback> addChannel(
        Context context, User user, Channel channel, Channel oldChannel) {
        return rx.Observable.zip(
            saveRealmChannel(context, channel, user),
            saveChannelToFirebase(user.id().value(), channel),
            zipAddChannel(oldChannel))
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    public static List<EventCallback> deleteChannel(
        Context context, User user, Channel channel) {
        return rx.Observable.zip(
            deleteRealmUserChannel(context, channel, user),
            deleteChannelFromFirebase(user.id().value(), channel),
            zipDeleteChannel())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    private static Func2<User, Channel, EventCallback> zipAddChannel(final Channel oldChannel) {
        return new Func2<User, Channel, EventCallback>() {
            @Override
            public UserChannelAddedEventCallback call(User user, Channel channel) {
                return new UserChannelAddedEventCallback(user, channel, oldChannel);
            }
        };
    }

    private static Func2<User, Channel, EventCallback> zipDeleteChannel() {
        return new Func2<User, Channel, EventCallback>() {
            @Override
            public UserChannelDeletedEventCallback call(User user, Channel channel) {
                return new UserChannelDeletedEventCallback(user, channel);
            }
        };
    }

    private static rx.Observable<User> saveRealmChannel(
        Context context, Channel channel, User user) {
        return Observable.just(channel)
            .map(addRealmUserChannel(context, user));
    }

    private static rx.Observable<User> deleteRealmUserChannel(
        Context context, Channel channel, User user) {
        return Observable.just(channel)
            .map(deleteRealmUserChannel(context, user));
    }

    private static Func1<Channel, User> addRealmUserChannel(
        final Context context, final User user) {
        return new Func1<Channel, User>() {
            @Override
            public User call(Channel channel) {
                Timber.i("Channel Object: " + channel.toString());
                User newUser = UserFactory.addUserChannel(user, channel);
                GroupFactory.addUserGroupsChannel(newUser, channel);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static Func1<Channel, User> deleteRealmUserChannel(
        final Context context, final User user) {
        return new Func1<Channel, User>() {
            @Override
            public User call(Channel channel) {
                Timber.i("Channel Object: " + channel.toString());
                User newUser = UserFactory.deleteUserChannel(user, channel);
                GroupFactory.removeUserGroupsChannel(newUser, channel);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static rx.Observable<Channel> saveChannelToFirebase(String userId, Channel channel) {
        return getUserChannelService().addUserChannel(userId, channel.id().value(), channel);
    }

    private static rx.Observable<Channel> deleteChannelFromFirebase(String userId, Channel channel) {
        Observable<Channel> deleteObserver = getUserChannelService()
            .deleteUserChannel(userId, channel.id().value());
        deleteObserver.subscribe(getDeleteChannelObserver());
        ConnectableObservable<Channel> connectibleObservable = deleteObserver.publish();
        connectibleObservable.connect();
        return rx.Observable.merge(Observable.just(channel), connectibleObservable);
    }

    private static JustObserver<Channel> getDeleteChannelObserver() {
        return new JustObserver<Channel>() {
            @Override
            public void onError() {
                Timber.e("error deleting newChannel");
            }

            @Override
            public void onNext(Channel event) {
                Timber.i("delete newChannel successful");
            }
        };
    }
}
