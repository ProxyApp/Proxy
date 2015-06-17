package com.shareyourproxy.api.rx;

import android.content.Context;

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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUsers;

/**
 * Created by Evan on 5/21/15.
 */
public class RxHelper {

    @SuppressWarnings("unchecked")
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
        realm.beginTransaction();
        RealmUser realmUser = createRealmUser(user);
        realm.copyToRealmOrUpdate(realmUser);
        realm.commitTransaction();
        realm.close();
    }

    public static void updateRealmUser(Context context, Map<String, User> users) {
        Realm realm = Realm.getInstance(context);
        realm.refresh();
        realm.beginTransaction();
        RealmList<RealmUser> realmUsers = createRealmUsers(users);
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
                            e.printStackTrace();
                        }
                    }
                } finally {
                    if (realm != null)
                        realm.close();
                    subscriber.unsubscribe();
                }
            }
        }).compose(applySchedulers()).subscribe();
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
