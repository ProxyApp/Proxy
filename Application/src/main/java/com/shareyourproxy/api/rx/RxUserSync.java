package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.ContactFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Created by Evan on 6/9/15.
 */
public class RxUserSync {

    /**
     * Private constructor.
     */
    private RxUserSync() {
    }

    /**
     * Download all users from firebase, save them to Realm, and return them as an HashMap<User>.
     *
     * @param context to get realm instance
     * @return HashMap<User>
     */
    public static List<EventCallback> getAllUsers(Context context) {
        return getFirebaseUsers(context)
            .map(saveRealmUsers(context))
            .map(usersDownloaded())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    public static List<EventCallback> syncAllUsers(Context context, String loggedInUserId) {
        User newUser = getFirebaseUsers(context).map(saveRealmUsers(context))
            .map(updateCachedContacts(loggedInUserId))
            .compose(RxHelper.<User>applySchedulers())
            .toBlocking().single();
        return saveUser(context, newUser);
    }

    private static Func1<HashMap<String, User>, User> updateCachedContacts(final String userId) {
        return new Func1<HashMap<String, User>, User>() {
            @Override
            public User call(HashMap<String, User> users) {
                User loggedInUser = users.get(userId);
                //for every contact that's in the entire user list, remove the old copy and
                // replace it with the new user data
                for(Map.Entry<String, Contact> contactEntry : loggedInUser.contacts().entrySet()){
                    for (Map.Entry<String, User> userEntry : users.entrySet()) {
                        if (userEntry.getKey().equals(contactEntry.getKey())) {
                            loggedInUser.contacts()
                                .put(userEntry.getKey(),
                                ContactFactory.createModelContact(userEntry.getValue()));
                        }
                    }
                }
                return loggedInUser;
            }
        };
    }

    private static rx.Observable<HashMap<String, User>> getFirebaseUsers(Context context) {
        return getUserService(context).listUsers();
    }

    private static Func1<HashMap<String, User>, HashMap<String, User>> saveRealmUsers(final Context context) {
        return new Func1<HashMap<String, User>, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(HashMap<String, User> users) {
                updateRealmUser(context, users);
                return users;
            }
        };
    }

    private static Func1<HashMap<String, User>, EventCallback> usersDownloaded() {
        return new Func1<HashMap<String, User>, EventCallback>() {
            @Override
            public UsersDownloadedEventCallback call(HashMap<String, User> users) {
                return new UsersDownloadedEventCallback(users);
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

    public static List<EventCallback> saveUser(Context context, User newUser) {
        return RestClient.getUserService(context).updateUser(newUser.id().value(),
            newUser)
            .map(saveRealmUser(context))
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }
}
