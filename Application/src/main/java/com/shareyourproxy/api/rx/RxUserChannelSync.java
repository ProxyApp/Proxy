package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.event.CommandEvent;
import com.shareyourproxy.api.rx.command.event.UserChannelAddedEvent;
import com.shareyourproxy.api.rx.command.event.UserChannelDeletedEvent;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

import static com.shareyourproxy.api.RestClient.getUserChannelService;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;


/**
 * Sync channel operations.
 */
public class RxUserChannelSync {

    /**
     * Private constructor.
     */
    private RxUserChannelSync() {
    }

    public static List<CommandEvent> addChannel(
        Context context, User user, Channel channel) {
        return rx.Observable.zip(
            saveRealmChannel(context, channel, user),
            saveChannelToFirebase(context, user.id().value(), channel),
            zipAddChannel())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    public static List<CommandEvent> deleteChannel(
        Context context, User user, Channel channel) {
        return rx.Observable.zip(
            deleteRealmUserChannel(context, channel, user),
            deleteChannelFromFirebase(context, user.id().value(), channel),
            zipDeleteChannel())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    private static Func2<User, Channel, CommandEvent> zipAddChannel() {
        return new Func2<User, Channel, CommandEvent>() {
            @Override
            public UserChannelAddedEvent call(User user, Channel channel) {
                return new UserChannelAddedEvent(user, channel);
            }
        };
    }

    private static Func2<User, Channel, CommandEvent> zipDeleteChannel() {
        return new Func2<User, Channel, CommandEvent>() {
            @Override
            public UserChannelDeletedEvent call(User user, Channel channel) {
                return new UserChannelDeletedEvent(user, channel);
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
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
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
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<Channel> saveChannelToFirebase(
        Context context, String userId, Channel channel) {
        return getUserChannelService(context)
            .addUserChannel(userId, channel.id().value(), channel);
    }

    private static rx.Observable<Channel> deleteChannelFromFirebase(
        Context context, String userId, Channel channel) {
        Observable<Channel> deleteObserver = getUserChannelService(context)
            .deleteUserChannel(userId, channel.id().value());
        deleteObserver.subscribe(new JustObserver<Channel>() {
            @Override
            public void onError() {
                Timber.e("error deleting channel");
            }

            @Override
            public void onNext(Channel event) {
                Timber.i("delete channel successful");
            }
        });
        ConnectableObservable<Channel> connectableObservable = deleteObserver.publish();
        return rx.Observable.merge(Observable.just(channel), connectableObservable)
            .filter(filterNullChannel());
    }

    private static Func1<Channel, Boolean> filterNullChannel() {
        return new Func1<Channel, Boolean>() {
            @Override
            public Boolean call(Channel channel) {
                return channel != null;
            }
        };
    }
}
