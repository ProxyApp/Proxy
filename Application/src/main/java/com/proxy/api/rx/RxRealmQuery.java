package com.proxy.api.rx;

import android.app.Activity;

import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmUser;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
                    Realm.getInstance(activity).where(RealmUser.class).findAllSorted("lastName");
                return UserFactory.createModelUsers(realmUsers);
            }
        }).compose(RxModelUpload.<ArrayList<User>>applySchedulers());
    }

    public static rx.Observable<ArrayList<User>> searchUsersTable(
        Activity activity, final String username) {
        return Observable.just(activity).map(new Func1<Activity, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(Activity activity) {
                return updateSearchText(Realm.getInstance(activity), username);
            }
        }).debounce(1, TimeUnit.SECONDS)
            .compose(RxModelUpload.<ArrayList<User>>applySchedulers());
    }

    public static ArrayList<User> updateSearchText(Realm realm, CharSequence constraint) {
        RealmResults<RealmUser> realmUsers;
        if (constraint.equals("")) {
            realmUsers = realm.where(RealmUser.class).findAllSorted("lastName");
        } else {
            realmUsers = realm.where(RealmUser.class)
                .contains("firstName", constraint.toString(), false)
                .or().contains("lastName", constraint.toString(), false)
                .or().contains("fullName", constraint.toString(), false)
                .findAllSorted("lastName");
        }
        realm.close();
        return UserFactory.createModelUsers(realmUsers);
    }
}
