package com.shareyourproxy.api.rx;


import android.app.Activity;
import android.support.v4.util.Pair;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;


/**
 * Created by Evan on 5/11/15.
 */
public class RxChannelSync {

    /**
     * Private constructor.
     */
    private RxChannelSync() {
    }

    public static rx.Observable<Pair<User, Channel>> addChannel(
        Activity activity, User user, Channel channel) {
        return rx.Observable.zip(
            saveRealmChannel(activity, channel, user),
            saveChannelToFirebase(activity, user.id().value(), channel),
            zipSyncChannel())
            .compose(RxHelper.<Pair<User, Channel>>applySchedulers());
    }

    public static rx.Observable<Pair<User, Channel>> deleteChannel(
        Activity activity, User user, Channel channel) {
        return rx.Observable.zip(
            deleteRealmChannel(activity, channel, user),
            deleteChannelFromFirebase(activity, user.id().value(), channel),
            zipSyncChannel())
            .compose(RxHelper.<Pair<User, Channel>>applySchedulers());
    }

    private static Func2<User, Channel, Pair<User, Channel>> zipSyncChannel() {
        return new Func2<User, Channel, Pair<User, Channel>>() {
            @Override
            public Pair<User, Channel> call(User user, Channel channel) {
                return new Pair<>(user, channel);
            }
        };
    }

    private static rx.Observable<User> saveRealmChannel(
        Activity activity, Channel channel, User user) {
        return Observable.just(channel)
            .map(addRealmUserChannel(activity, user))
            .compose(RxHelper.<User>applySchedulers());
    }

    private static rx.Observable<User> deleteRealmChannel(
        Activity activity, Channel channel, User user) {
        return Observable.just(channel)
            .map(deleteRealmUserChannel(activity, user))
            .compose(RxHelper.<User>applySchedulers());
    }

    private static Func1<Channel, User> addRealmUserChannel(
        final Activity activity, final User user) {
        return new Func1<Channel, User>() {
            @Override
            public User call(Channel channel) {
                Timber.i("Channel Object: " + channel.toString());
                User newUser = UserFactory.addUserChannel(user, channel);
                Realm realm = Realm.getInstance(activity);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static Func1<Channel, User> deleteRealmUserChannel(
        final Activity activity, final User user) {
        return new Func1<Channel, User>() {
            @Override
            public User call(Channel channel) {
                Timber.i("Channel Object: " + channel.toString());
                User newUser = UserFactory.deleteUserChannel(user, channel);
                Realm realm = Realm.getInstance(activity);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<Channel> saveChannelToFirebase(
        Activity activity, String userId, Channel channel) {
        return RestClient.getChannelService(activity)
            .addUserChannel(userId, channel.id().value(), channel)
            .compose(RxHelper.<Channel>applySchedulers());
    }

    private static rx.Observable<Channel> deleteChannelFromFirebase(
        Activity activity, String userId, Channel channel) {
        return rx.Observable.zip(Observable.just(channel), RestClient.getChannelService(activity)
            .deleteUserChannel(userId, channel.id().value()), removeNullChannel()).compose
            (RxHelper.<Channel>applySchedulers());
    }

    private static Func2<Channel, Channel, Channel> removeNullChannel() {
        return new Func2<Channel, Channel, Channel>() {
            @Override
            public Channel call(Channel channel, Channel channel2) {
                if (channel == null) {
                    return channel2;
                } else {
                    return channel;
                }
            }
        };
    }

}
