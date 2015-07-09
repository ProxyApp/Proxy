package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Func1;

import static com.shareyourproxy.api.RestClient.getUserService;
import static com.shareyourproxy.api.domain.factory.ContactFactory.createModelContact;
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
     * Download All Users from firebase and sync them to realm. Used for swipe refresh in the {@link
     * com.shareyourproxy.app.fragment.MainContactsFragment} and {@link
     * com.shareyourproxy.app.fragment.MainGroupFragment}'s {@link android.support.v4.widget
     * .SwipeRefreshLayout}s
     *
     * @param context        for realm instance
     * @param loggedInUserId to identify the logged in user
     * @return {@link UsersDownloadedEventCallback} to rxBus
     */
    public static List<EventCallback> syncAllUsers(Context context, String loggedInUserId) {
        return getFirebaseUsers()
            .map(updateLoggedInUser(loggedInUserId))
            .map(saveRealmUsers(context))
            .map(usersDownloaded(loggedInUserId))
            .compose(RxHelper.<List<EventCallback>>applySchedulers()).toBlocking().single();
    }

    private static Func1<HashMap<String, User>, HashMap<String, User>> updateLoggedInUser(
        final String loggedInUserId) {
        return new Func1<HashMap<String, User>, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(HashMap<String, User> users) {
                User loggedInUser = users.get(loggedInUserId);
                //for every loggedInUserContact
                for (Map.Entry<String, Contact> contactEntry : loggedInUser.contacts().entrySet()) {
                    Contact contact = contactEntry.getValue();
                    String contactId = contact.id().value();
                    //if that contact is in any logged in user group, update it
                    for (Map.Entry<String, Group> groupEntry : loggedInUser.groups().entrySet()) {
                        Group group = groupEntry.getValue();
                        HashMap<String, Contact> groupContacts = group.contacts();
                        if (groupContacts.containsKey(contactId)){
                            group.contacts().put(contactId, contact);
                        }
                        loggedInUser.contacts().put(contactId, createModelContact(users.get
                            (contactId)));
                    }
                }
                users.put(loggedInUserId, loggedInUser);
                return users;
            }
        };

    }

    private static rx.Observable<HashMap<String, User>> getFirebaseUsers() {
        return getUserService().listUsers();
    }

    private static Func1<HashMap<String, User>, HashMap<String, User>>
    saveRealmUsers(final Context context) {
        return new Func1<HashMap<String, User>, HashMap<String, User>>() {
            @Override
            public HashMap<String, User> call(HashMap<String, User> users) {
                updateRealmUser(context, users);
                return users;
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
        return RestClient.getUserService().updateUser(newUser.id().value(),
            newUser)
            .map(saveRealmUser(context))
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    private static Func1<HashMap<String, User>, List<EventCallback>> usersDownloaded(
        final String loggedInUserId) {
        return new Func1<HashMap<String, User>, List<EventCallback>>() {
            @Override
            public List<EventCallback> call(HashMap<String, User> users) {
                User loggedInUser = users.get(loggedInUserId);
                UsersDownloadedEventCallback usersCallback =
                    new UsersDownloadedEventCallback(loggedInUser, users);
                LoggedInUserUpdatedEventCallback loggedInUserCallback =
                    new LoggedInUserUpdatedEventCallback(loggedInUser);
                ArrayList<EventCallback> list = new ArrayList<>();
                list.add(usersCallback);
                list.add(loggedInUserCallback);
                return list;
            }
        };
    }
}
