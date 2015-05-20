package com.proxy.api.rx;


import android.app.Activity;
import android.support.v4.util.Pair;

import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.User;

import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.proxy.api.domain.factory.UserFactory.createRealmUser;


/**
 * Created by Evan on 5/11/15.
 */
public class RxModelUpload {

    @SuppressWarnings("unchecked")
    public static Observable.Transformer<T, T> schedulersTransformer =
        new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };

    /**
     * Private constructor.
     */
    private RxModelUpload() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }

    private static Func1<Channel, User> transactUpdatedUser(
        final Activity activity, final User user) {
        return new Func1<Channel, User>() {
            @Override
            public User call(Channel channel) {
                Timber.i("Channel Object: " + channel.toString());
                User newUser = UserFactory.updateUserChannel(user, channel);
                Realm realm = Realm.getInstance(activity);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    public static rx.Observable<Pair<User, Channel>> syncChannel(
        Activity activity, final User user, final Channel channel) {
        return rx.Observable.zip(
            saveRealmChannel(activity, channel, user),
            saveChannelToFirebase(activity, user.userId(), channel),
            zipSyncChannel())
            .compose(RxModelUpload.<Pair<User, Channel>>applySchedulers());
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
        Activity activity, final Channel channel, final User user) {
        return Observable.just(channel)
            .map(transactUpdatedUser(activity, user))
            .compose(RxModelUpload.<User>applySchedulers());
    }

    private static rx.Observable<Channel> saveChannelToFirebase(
        final Activity activity, final String userId, Channel channel) {
        return RestClient.getChannelService(activity)
            .addUserChannel(userId, channel.channelId(), channel)
            .compose(RxModelUpload.<Channel>applySchedulers());
    }

    interface T {
    }

}