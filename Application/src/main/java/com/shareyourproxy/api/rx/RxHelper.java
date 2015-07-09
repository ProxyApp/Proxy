package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUsers;

/**
 * RxHelper for common rx.Observable method calls.
 */
public class RxHelper {

    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Func1<T, Boolean> filterNullObject() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T object) {
                return object != null;
            }
        };
    }

    public static void updateRealmUser(Context context, User user) {
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        final RealmUser realmUser = createRealmUser(user);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmUser);
        realm.commitTransaction();
        realm.close();
    }

    public static void updateRealmUser(Context context, Map<String, User> users) {
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        final RealmList<RealmUser> realmUsers = createRealmUsers(users);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmUsers);
        realm.commitTransaction();
        realm.close();
    }

    static Func1<Map<String, User>, Map<String, User>> addRealmUsers(final Context context) {
        return new Func1<Map<String, User>, Map<String, User>>() {
            @Override
            public Map<String, User> call(Map<String, User> users) {
                updateRealmUser(context, users);
                return users;
            }
        };
    }

    static Func1<User, User> addRealmUser(final Context context) {
        return new Func1<User, User>() {
            @Override
            public User call(User user) {
                updateRealmUser(context, user);
                return user;
            }
        };
    }
}
