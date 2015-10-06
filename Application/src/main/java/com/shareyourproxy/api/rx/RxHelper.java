package com.shareyourproxy.api.rx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.shareyourproxy.Constants.KEY_GOOGLE_PLUS_AUTH;
import static com.shareyourproxy.Constants.PROVIDER_GOOGLE;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUsers;

/**
 * RxHelper for common rx.Observable method calls.
 */
public class RxHelper {

    private static Firebase.AuthResultHandler _handler;
    public static final Firebase _firebaseRef = new Firebase(BuildConfig.FIREBASE_ENDPOINT);

    private RxHelper() {
    }

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

    public static Observable<String> refreshGooglePlusToken(
        final Context context, final GoogleApiClient client) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String token = null;
                try {
                    if (client.isConnected()) {
                        token = GoogleAuthUtil.getToken(context, Plus.AccountApi
                            .getAccountName(client), String.format("oauth2:%s", Scopes.PLUS_LOGIN));
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                if(token != null) {
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                }
                else{
                    subscriber.onError(new NullPointerException("Null Google Plus Token"));
                }
            }
        });
    }

    public static Observable<String> refreshFirebaseAuth(
        final Context context, final GoogleApiClient client, final SharedPreferences sharedPref) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                refreshGooglePlusToken(context, client).doOnNext(
                    getFirebaseToken(subscriber, sharedPref)).subscribe(getGoogleOAuthObserver());
            }
        }).compose(RxHelper.<String>applySchedulers());
    }

    private static JustObserver<String> getGoogleOAuthObserver() {
        return new JustObserver<String>() {
        @Override
        public void next(String s) {

        }

        @Override
        public void error(Throwable e) {
            Timber.e(e.getMessage());
        }
    };
    }

    private static Action1<String> getFirebaseToken(
        final Subscriber<? super String> subscriber, final SharedPreferences sharedPref) {
        return new Action1<String>() {
            @Override
            public void call(String token) {
                _firebaseRef
                    .authWithOAuthToken(
                        PROVIDER_GOOGLE, token, getHandler(subscriber, sharedPref));
            }
        };
    }

    private static Firebase.AuthResultHandler getHandler(
        final Subscriber<? super String> subscriber, final SharedPreferences sharedPref) {
        if (_handler == null) {
            _handler = new Firebase.AuthResultHandler() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onAuthenticated(AuthData authData) {
                    String token = authData.getToken();
                    sharedPref.edit()
                        .putString(KEY_GOOGLE_PLUS_AUTH, token)
                        .commit();
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                }
                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Timber.e(firebaseError.getMessage());
                    subscriber.onError(firebaseError.toException());
                }
            };
        }
        return _handler;
    }
}
