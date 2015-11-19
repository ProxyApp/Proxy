package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getHerokuUserervice;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;
import static java.util.Collections.singleton;

/**
 * Cold Observables to sync users to firebase and realm.
 */
public class RxUserSync {

    /**
     * Private constructor.
     */
    private RxUserSync() {
    }

    /**
     * Download All User contacts from firebase and sync them to realm. Used for swipe refresh in
     * the {@link com.shareyourproxy.app.fragment.MainContactsFragment} and {@link
     * com.shareyourproxy.app.fragment.MainGroupFragment}'s {@link android.support.v4.widget
     * .SwipeRefreshLayout}s
     *
     * @param context      for realm instance
     * @param loggedInUser user logged into the app
     * @return {@link UsersDownloadedEventCallback} to rxBus
     */
    public static List<EventCallback> syncAllContacts(
        Context context, User loggedInUser) {
        HashSet<String> contacts = loggedInUser.contacts();

        return (contacts != null && contacts.size() > 0) ?
            getFirebaseUsers(context, loggedInUser)
                .map(saveRealmUsers(context))
                .map(usersDownloaded(loggedInUser))
                .compose(RxHelper.<List<EventCallback>>applySchedulers()).toBlocking().single() :
            new ArrayList<EventCallback>(singleton(
                new SyncAllUsersSuccessEvent()));
    }

    public static List<EventCallback> saveUser(
        Context context, User newUser) {
        return RestClient.getUserService(context).updateUser(newUser.id(), newUser)
            .map(saveRealmUser(context))
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    private static rx.Observable<ArrayList<User>> getFirebaseUsers(
        Context context, User user) {
        return getHerokuUserervice(context).listUsers(user.contacts());
    }

    private static Func1<ArrayList<User>, HashMap<String, User>> saveRealmUsers(
        final Context context) {
        return new Func1<ArrayList<User>, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(ArrayList<User> users) {
                HashMap<String, User> usersMap = new HashMap<>(users.size());
                for (User user : users) {
                    usersMap.put(user.id(), user);
                }
                updateRealmUser(context, usersMap);
                return usersMap;
            }
        };
    }

    private static Func1<User, EventCallback> saveRealmUser(final Context context) {
        return new Func1<User, EventCallback>() {
            @Override
            public LoggedInUserUpdatedEventCallback call(User user) {
                updateRealmUser(context, user);
                return new LoggedInUserUpdatedEventCallback(user);
            }
        };
    }

    private static Func1<HashMap<String, User>, List<EventCallback>> usersDownloaded(
        final User loggedInUser) {
        return new Func1<HashMap<String, User>, List<EventCallback>>() {
            @Override
            public List<EventCallback> call(HashMap<String, User> users) {
                UsersDownloadedEventCallback usersCallback =
                    new UsersDownloadedEventCallback(loggedInUser, users);
                LoggedInUserUpdatedEventCallback loggedInUserCallback =
                    new LoggedInUserUpdatedEventCallback(loggedInUser);
                ArrayList<EventCallback> list = new ArrayList<>();
                list.add(usersCallback);
                list.add(loggedInUserCallback);
                list.add(new SyncAllUsersSuccessEvent());
                return list;
            }
        };
    }
}
