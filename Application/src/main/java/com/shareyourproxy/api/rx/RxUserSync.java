package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getHerokuUserervice;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

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
    public static EventCallback syncAllContacts(
        Context context, User loggedInUser) {
        HashSet<String> contacts = loggedInUser.contacts();

        return (contacts != null && contacts.size() > 0) ?
            getFirebaseUsers(context, loggedInUser)
                .map(saveRealmUsers(context))
                .map(usersDownloaded(loggedInUser))
                .compose(RxHelper.<EventCallback>subThreadObserveMain()).toBlocking().single() :
            new SyncAllContactsSuccessEvent();
    }

    public static EventCallback saveUser(
        Context context, User newUser) {
        return RestClient.getUserService(context).updateUser(newUser.id(), newUser)
            .map(saveRealmUser(context))
            .compose(RxHelper.<EventCallback>subThreadObserveMain())
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

    private static Func1<HashMap<String, User>, EventCallback> usersDownloaded(
        final User loggedInUser) {
        return new Func1<HashMap<String, User>, EventCallback>() {
            @Override
            public EventCallback call(HashMap<String, User> users) {
                return new UsersDownloadedEventCallback(loggedInUser, users);
            }
        };
    }
}
