package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Log;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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

    public static void saveRealmFile(final Context context) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Realm realm = null;
                try {
                    realm = Realm.getInstance(context);
                    File f = new File(realm.getPath());
                    if (f.exists()) {
                        try {
                            copyRealmFile(f, new File("/sdcard/default.realm"));
                        } catch (IOException e) {
                            Timber.e(Log.getStackTraceString(e));
                        }
                    }
                } finally {
                    if (realm != null)
                        realm.close();
                    subscriber.unsubscribe();
                }
            }
        }).compose(applySchedulers()).subscribe(saveFileObserver());
    }

    public static Observer<Object> saveFileObserver() {
        return new Observer<Object>() {
            @Override
            public void onCompleted() {
                Timber.i("Realm File saved");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(Log.getStackTraceString(e));
                Timber.e("Realm File failed to save");
            }

            @Override
            public void onNext(Object event) {
                Timber.i("Realm File save onNext");
            }
        };
    }

    private static void copyRealmFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
