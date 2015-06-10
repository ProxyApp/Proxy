package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.event.CommandEvent;
import com.shareyourproxy.api.rx.command.event.UserGroupAddedEvent;
import com.shareyourproxy.api.rx.command.event.UserGroupDeletedEvent;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;

/**
 * Created by Evan on 6/8/15.
 */
public class RxUserGroupSync {
    /**
     * Private constructor.
     */
    private RxUserGroupSync() {
    }

    public static List<CommandEvent> addUserGroup(
        Context context, User user, Group group) {
        return rx.Observable.zip(
            saveRealmUserGroup(context, group, user),
            saveFirebaseUserGroup(context, user.id().value(), group),
            zipAddUserGroup())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    public static List<CommandEvent> deleteUserGroup(
        Context context, User user, Group group) {
        return rx.Observable.zip(
            deleteRealmUserGroup(context, group, user),
            deleteFirebaseUserGroup(context, user.id().value(), group),
            zipDeleteUserGroup())
            .toList()
            .compose(RxHelper.<List<CommandEvent>>applySchedulers())
            .toBlocking().single();
    }

    private static Func2<User, Group, CommandEvent> zipAddUserGroup() {
        return new Func2<User, Group, CommandEvent>() {
            @Override
            public UserGroupAddedEvent call(User user, Group group) {
                return new UserGroupAddedEvent(user, group);
            }
        };
    }

    private static Func2<User, Group, CommandEvent> zipDeleteUserGroup() {
        return new Func2<User, Group, CommandEvent>() {
            @Override
            public UserGroupDeletedEvent call(User user, Group group) {
                return new UserGroupDeletedEvent(user, group);
            }
        };
    }

    private static rx.Observable<User> saveRealmUserGroup(
        Context context, Group group, User user) {
        return Observable.just(group)
            .map(addRealmUserGroup(context, user))
            .compose(RxHelper.<User>applySchedulers());
    }

    private static Func1<Group, User> addRealmUserGroup(
        final Context context, final User user) {
        return new Func1<Group, User>() {
            @Override
            public User call(Group group) {
                User newUser = UserFactory.addUserGroup(user, group);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<User> deleteRealmUserGroup(
        Context context, Group group, User user) {
        return Observable.just(group)
            .map(deleteRealmUserGroup(context, user))
            .compose(RxHelper.<User>applySchedulers());
    }

    private static Func1<Group, User> deleteRealmUserGroup(
        final Context context, final User user) {
        return new Func1<Group, User>() {
            @Override
            public User call(Group group) {
                User newUser = UserFactory.deleteUserGroup(user, group);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newUser;
            }
        };
    }

    private static rx.Observable<Group> saveFirebaseUserGroup(
        Context context, String userId, Group group) {
        return RestClient.getUserGroupService(context)
            .addUserGroup(userId, group.id().value(), group);
    }


    private static rx.Observable<Group> deleteFirebaseUserGroup(
        Context context, String userId, Group group) {
        return rx.Observable.merge(Observable.just(group), RestClient.getUserGroupService(context)
            .deleteUserGroup(userId, group.id().value())).filter(filterNullContact());
    }

    private static Func1<Group, Boolean> filterNullContact() {
        return new Func1<Group, Boolean>() {
            @Override
            public Boolean call(Group group) {
                return group != null;
            }
        };
    }

}
