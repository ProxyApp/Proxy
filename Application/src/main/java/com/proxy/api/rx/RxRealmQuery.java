package com.proxy.api.rx;

import android.app.Activity;

import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmUser;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Evan on 5/20/15.
 */
public class RxRealmQuery {

    public static rx.Observable<ArrayList<User>> queryAllUsers(Activity activity) {
        return Observable.just(activity).map(new Func1<Activity, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(Activity activity) {
                RealmResults<RealmUser> realmUsers =
                    Realm.getInstance(activity).where(RealmUser.class).findAllSorted("last");
                return UserFactory.createModelUsers(realmUsers);
            }
        }).compose(RxHelper.<ArrayList<User>>applySchedulers());
    }

    public static Func1<String, ArrayList<User>> searchUserString(final Activity activity) {
        return new Func1<String, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(String username) {
                return updateSearchText(Realm.getInstance(activity), username);
            }
        };
    }

    private static ArrayList<User> updateSearchText(Realm realm, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        if (constraint.equals("")) {
            realmUsers = realm.where(RealmUser.class).findAllSorted("last");
        } else {
            realmUsers = realm.where(RealmUser.class)
                .contains("first", constraint.toString(), false)
                .or().contains("last", constraint.toString(), false)
                .or().contains("fullName", constraint.toString(), false)
                .findAllSorted("last");
        }
        realm.close();
        return UserFactory.createModelUsers(realmUsers);
    }
}
