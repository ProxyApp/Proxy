package com.shareyourproxy.api.rx;

import android.content.Context;
import android.util.Pair;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactDeletedEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.shareyourproxy.api.RestClient.getUserContactService;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Sync contacts asynchronously.
 */
public class RxUserContactSync {

    /**
     * Private constructor.
     */
    private RxUserContactSync() {
    }

    public static List<EventCallback> addUserContact(
        Context context, RxBusDriver rxBus, User user, String contactId) {
        return rx.Observable.zip(
            saveRealmUserContact(context, user, contactId),
            saveFirebaseUserContact(context, rxBus, user.id(), contactId),
            zipAddUserContact())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    public static List<EventCallback> deleteUserContact(
        Context context, RxBusDriver rxBus, User user, String contactId) {
        return rx.Observable.zip(
            deleteRealmUserContact(context, user, contactId),
            deleteFirebaseUserContact(context, rxBus, user.id(), contactId),
            zipDeleteUserContact())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    private static Func2<User, String, EventCallback> zipAddUserContact() {
        return new Func2<User, String, EventCallback>() {
            @Override
            public UserContactAddedEventCallback call(User user, String contactId) {
                return new UserContactAddedEventCallback(user, contactId);
            }
        };
    }

    private static Func2<User, String, EventCallback> zipDeleteUserContact() {
        return new Func2<User, String, EventCallback>() {
            @Override
            public UserContactDeletedEventCallback call(User user, String contactId) {
                return new UserContactDeletedEventCallback(user, contactId);
            }
        };
    }

    private static rx.Observable<User> saveRealmUserContact(
        Context context, User user, String contactId) {
        return Observable.just(contactId)
            .map(addRealmUserContact(context, user));
    }

    private static Func1<String, User> addRealmUserContact(
        final Context context, final User user) {
        return new Func1<String, User>() {
            @Override
            public User call(String contactId) {
                User newUser = UserFactory.addUserContact(user, contactId);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static rx.Observable<User> deleteRealmUserContact(
        Context context, User user, String contactId) {
        return Observable.just(contactId)
            .map(deleteRealmUserContact(context, user));
    }

    private static Func1<String, User> deleteRealmUserContact(
        final Context context, final User user) {
        return new Func1<String, User>() {
            @Override
            public User call(String contactId) {
                User newUser = UserFactory.deleteUserContact(user, contactId);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static rx.Observable<String> saveFirebaseUserContact(
        Context context,
        RxBusDriver rxBus, String userId, String contactId) {
        return getUserContactService(context, rxBus).addUserContact(userId, contactId);
    }


    private static rx.Observable<String> deleteFirebaseUserContact(
        Context context, RxBusDriver rxBus, String userId, String contactId) {
        getUserContactService(context, rxBus).deleteUserContact(userId, contactId).subscribe();
        return Observable.just(contactId);
    }

    public static List<EventCallback> checkContacts(
        final Context context, RxBusDriver rxBus, final User user,
        ArrayList<String> contacts, final HashMap<String, Group> userGroups) {
        return Observable.from(contacts)
            .map(findContactsToDelete(userGroups))
            .filter(filterMissingContacts())
            .map(unwrapPairedContact())
            .flatMap(zipDeleteUserContact(context, rxBus, user))
            .toList().toBlocking().single();
    }

    private static Func1<String, Pair<String, Boolean>> findContactsToDelete(
        final HashMap<String, Group> userGroups) {
        return new Func1<String, Pair<String, Boolean>>() {
            @Override
            public Pair<String, Boolean> call(String contactId) {
                if (userGroups != null) {
                    for (Map.Entry<String, Group> entryGroup : userGroups.entrySet()) {
                        Group group = entryGroup.getValue();
                        HashSet<String> groupContacts = group.contacts();
                        if (groupContacts != null) {
                            for (String groupContactId : groupContacts) {

                                if (groupContactId.equals(contactId)) {
                                    return new Pair<>(groupContactId, true);
                                }
                            }
                            return new Pair<>(contactId, false);
                        }
                    }
                }
                return new Pair<>(contactId, false);
            }
        };
    }

    private static Func1<Pair<String, Boolean>, Boolean> filterMissingContacts() {
        return new Func1<Pair<String, Boolean>, Boolean>() {
            @Override
            public Boolean call(Pair<String, Boolean> contactPair) {
                // we only want contacts to remove... false means remove
                return !contactPair.second;
            }
        };
    }

    private static Func1<Pair<String, Boolean>, String> unwrapPairedContact() {
        return new Func1<Pair<String, Boolean>, String>() {
            @Override
            public String call(Pair<String, Boolean> contact) {
                return contact.first;
            }
        };
    }

    private static Func1<String, Observable<EventCallback>> zipDeleteUserContact(
        final Context context, final RxBusDriver rxBus, final User user) {
        return new Func1<String, Observable<EventCallback>>() {
            @Override
            public Observable<EventCallback> call(String contactId) {
                return Observable.zip(
                    deleteRealmUserContact(context, user, contactId),
                    deleteFirebaseUserContact(context, rxBus, user.id(), contactId),
                    zipDeleteUserContact());
            }
        };
    }

}
