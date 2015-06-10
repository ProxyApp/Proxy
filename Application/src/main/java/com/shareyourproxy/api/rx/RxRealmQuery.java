package com.shareyourproxy.api.rx;


import android.content.Context;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

/**
 * Query the realm DB.
 */
public class RxRealmQuery {

    public static rx.Observable<ArrayList<User>> queryFilteredUsers(
        Context context, final String userId) {
        return Observable.just(context).map(new Func1<Context, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(Context context) {
                Realm realm = Realm.getInstance(context);
                RealmResults<RealmUser> realmUsers =
                    realm.where(RealmUser.class)
                        .notEqualTo("id", userId).findAll();
                ArrayList<User> users = UserFactory.createModelUsers(realmUsers);
                realm.close();
                return users;
            }
        }).compose(RxHelper.<ArrayList<User>>applySchedulers());
    }

    public static BlockingObservable<User> queryUser(Context context, final String userId) {
        return Observable.just(context).map(new Func1<Context, User>() {
            @Override
            public User call(Context context) {
                Realm realm = Realm.getInstance(context);
                RealmUser realmUser =
                    realm.where(RealmUser.class).contains("id", userId).findFirst();
                User user = UserFactory.createModelUser(realmUser);
                realm.close();
                return user;
            }
        }).compose(RxHelper.<User>applySchedulers()).toBlocking();
    }

    public static Func1<String, ArrayList<User>> searchUserString(
        final Context context, final String userId) {
        return new Func1<String, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(String username) {
                return updateSearchText(context, userId, username);
            }
        };
    }

    private static ArrayList<User> updateSearchText(
        Context context, final String userId, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        Realm realm = Realm.getInstance(context);
        if (constraint.equals("")) {
            realmUsers = realm.where(RealmUser.class).findAllSorted("last");
        } else {
            realmUsers = realm.where(RealmUser.class)
                .contains("first", constraint.toString(), false)
                .or().contains("last", constraint.toString(), false)
                .or().contains("fullName", constraint.toString(), false)
                .notEqualTo("id", userId)
                .findAllSorted("last");
        }
        ArrayList<User> users = UserFactory.createModelUsers(realmUsers);
        realm.close();
        return users;
    }
}
