package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;
import com.shareyourproxy.api.rx.command.callback.UserSavedEvent;
import com.shareyourproxy.api.rx.command.callback.UsersDownloadedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUsers;

/**
 * Created by Evan on 6/9/15.
 */
public class RxUserSync {

    /**
     * Private constructor.
     */
    private RxUserSync() {
    }

    public static List<CommandEvent> getAllUsers(Context context) {
        return getFirebaseUsers(context).map(saveRealmUsers(context))
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    private static rx.Observable<ArrayList<User>> getFirebaseUsers(Context context) {
        return getUserService(context).listUsers().map(
            UsersToListArrayList());
    }

    private static Func1<Map<String, User>, ArrayList<User>> UsersToListArrayList() {
        return new Func1<Map<String, User>, ArrayList<User>>() {
            @Override
            public ArrayList<User> call(Map<String, User> userMap) {
                return new ArrayList<>(userMap.values());
            }
        };
    }

    private static Func1<ArrayList<User>, CommandEvent> saveRealmUsers(final Context context) {
        return new Func1<ArrayList<User>, CommandEvent>() {
            @Override
            public UsersDownloadedEvent call(ArrayList<User> users) {
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUsers(users));
                realm.commitTransaction();
                realm.close();
                return new UsersDownloadedEvent(users);
            }
        };
    }

    private static Func1<User, CommandEvent> saveRealmUser(final Context context) {
        return new Func1<User, CommandEvent>() {
            @Override
            public UserSavedEvent call(User user) {
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(user));
                realm.commitTransaction();
                realm.close();
                return new UserSavedEvent(user);
            }
        };
    }

    public static List<CommandEvent> saveUser(Context context, User newUser) {
        return RestClient.getUserService(context).updateUser(newUser.id().value(),
            newUser)
            .map(saveRealmUser(context))
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }
}
