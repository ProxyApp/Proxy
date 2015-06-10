package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.event.CommandEvent;
import com.shareyourproxy.api.rx.command.event.UserContactAddedEvent;
import com.shareyourproxy.api.rx.command.event.UserContactDeletedEvent;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

import static com.shareyourproxy.api.RestClient.getUserContactService;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;

/**
 * Sync contacts asynchronously.
 */
public class RxUserContactSync {

    /**
     * Private constructor.
     */
    private RxUserContactSync() {
    }

    public static List<CommandEvent> addUserContact(
        Context context, User user, Contact contact) {
        return rx.Observable.zip(
            saveRealmUserGroup(context, contact, user),
            saveFirebaseUserGroup(context, user.id().value(), contact),
            zipAddUserContact())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    public static List<CommandEvent> deleteUserContact(
        Context context, User user, Contact contact) {
        return rx.Observable.zip(
            deleteRealmUserContact(context, contact, user),
            deleteFirebaseUserContact(context, user.id().value(), contact),
            zipDeleteUserContact())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    private static Func2<User, Contact, CommandEvent> zipAddUserContact() {
        return new Func2<User, Contact, CommandEvent>() {
            @Override
            public UserContactAddedEvent call(User user, Contact contact) {
                return new UserContactAddedEvent(user, contact);
            }
        };
    }

    private static Func2<User, Contact, CommandEvent> zipDeleteUserContact() {
        return new Func2<User, Contact, CommandEvent>() {
            @Override
            public UserContactDeletedEvent call(User user, Contact contact) {
                return new UserContactDeletedEvent(user, contact);
            }
        };
    }

    private static rx.Observable<User> saveRealmUserGroup(
        Context context, Contact Contact, User user) {
        return Observable.just(Contact)
            .map(addRealmUserContact(context, user));
    }

    private static Func1<Contact, User> addRealmUserContact(
        final Context context, final User user) {
        return new Func1<Contact, User>() {
            @Override
            public User call(Contact Contact) {
                Timber.i("Contact Object: " + Contact.toString());
                User newUser = UserFactory.addUserContact(user, Contact);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<User> deleteRealmUserContact(
        Context context, Contact contact, User user) {
        return Observable.just(contact)
            .map(deleteRealmUserContact(context, user))
            .compose(RxHelper.<User>applySchedulers());
    }

    private static Func1<Contact, User> deleteRealmUserContact(
        final Context context, final User user) {
        return new Func1<Contact, User>() {
            @Override
            public User call(Contact Contact) {
                Timber.i("Contact Object: " + Contact.toString());
                User newUser = UserFactory.deleteUserContact(user, Contact);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<Contact> saveFirebaseUserGroup(
        Context context, String userId, Contact contact) {
        return getUserContactService(context)
            .addUserContact(userId, contact.id().value(), contact);
    }


    private static rx.Observable<Contact> deleteFirebaseUserContact(
        Context context, String userId, Contact contact) {
        //TODO:WHY DOES THIS NEED TO BE A CONNECTIBLE OBSERVABLE FLOW, WHY CANT IT BE LIKE SAVE?
        Observable<Contact> deleteObserver = getUserContactService(context)
            .deleteUserContact(userId, contact.id().value());
        deleteObserver.subscribe(new JustObserver<Contact>() {
            @Override
            public void onError() {
                Timber.e("error deleting user contact");
            }

            @Override
            public void onNext(Contact event) {
                Timber.i("delete user contact successful");
            }
        });
        ConnectableObservable<Contact> connectableObservable = deleteObserver.publish();

        return rx.Observable.merge(Observable.just(contact), connectableObservable)
            .filter(filterNullContact());
    }

    private static Func1<Contact, Boolean> filterNullContact() {
        return new Func1<Contact, Boolean>() {
            @Override
            public Boolean call(Contact contact) {
                return contact != null;
            }
        };
    }

}
