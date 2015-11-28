package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.SharedLink;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.shareyourproxy.api.RestClient.getSharedLinkService;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Add and delete user groups.
 */
public class RxUserGroupSync {
    /**
     * Private constructor.
     */
    private RxUserGroupSync() {
    }

    public static EventCallback addUserGroup(Context context, User user, Group group) {
        return rx.Observable.zip(
            saveRealmUserGroup(context, group, user),
            saveFirebaseUserGroup(context, user.id(), group),
            zipAddUserGroup())
            .map(saveSharedLink(context))
            .compose(RxHelper.<EventCallback>subThreadObserveMain())
            .toBlocking().single();
    }

    public static EventCallback deleteUserGroup(Context context, User user, Group group) {
        return Observable.zip(
            deleteRealmUserGroup(context, group, user),
            deleteFirebaseUserGroup(context, user.id(), group),
            zipDeleteUserGroup())
            .map(deleteSharedLink(context))
            .compose(RxHelper.<EventCallback>subThreadObserveMain())
            .toBlocking().single();
    }

    private static Func1<UserGroupDeletedEventCallback, EventCallback> deleteSharedLink(
        final Context context) {
        return new Func1<UserGroupDeletedEventCallback, EventCallback>() {
            @Override
            public UserGroupDeletedEventCallback call(UserGroupDeletedEventCallback event) {
                getSharedLinkService(context)
                    .deleteSharedLink(event.group.id()).subscribe();
                return event;
            }
        };
    }

    private static Func1<UserGroupAddedEventCallback, EventCallback> saveSharedLink(
        final Context context) {
        return new Func1<UserGroupAddedEventCallback, EventCallback>() {
            @Override
            public UserGroupAddedEventCallback call(UserGroupAddedEventCallback event) {
                SharedLink link = SharedLink.create(event.user, event.group);
                getSharedLinkService(context)
                    .addSharedLink(link.id(), link).subscribe();
                return event;
            }
        };
    }

    private static Func2<User, Group, UserGroupAddedEventCallback> zipAddUserGroup() {
        return new Func2<User, Group, UserGroupAddedEventCallback>() {
            @Override
            public UserGroupAddedEventCallback call(User user, Group group) {
                return new UserGroupAddedEventCallback(user, group);
            }
        };
    }

    private static Func2<User, Group, UserGroupDeletedEventCallback> zipDeleteUserGroup() {
        return new Func2<User, Group, UserGroupDeletedEventCallback>() {
            @Override
            public UserGroupDeletedEventCallback call(User user, Group group) {
                return new UserGroupDeletedEventCallback(user, group);
            }
        };
    }

    private static rx.Observable<User> saveRealmUserGroup(
        Context context, Group group, User user) {
        return Observable.just(group)
            .map(addRealmUserGroup(context, user))
            .compose(RxHelper.<User>subThreadObserveMain());
    }

    private static Func1<Group, User> addRealmUserGroup(
        final Context context, final User user) {
        return new Func1<Group, User>() {
            @Override
            public User call(Group group) {
                User newUser = UserFactory.addUserGroup(user, group);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static rx.Observable<User> deleteRealmUserGroup(
        Context context, Group group, User user) {
        return Observable.just(group)
            .map(deleteRealmUserGroup(context, user))
            .compose(RxHelper.<User>subThreadObserveMain());
    }

    private static Func1<Group, User> deleteRealmUserGroup(
        final Context context, final User user) {
        return new Func1<Group, User>() {
            @Override
            public User call(Group group) {
                User newUser = UserFactory.deleteUserGroup(user, group);
                updateRealmUser(context, newUser);
                return newUser;
            }
        };
    }

    private static rx.Observable<Group> saveFirebaseUserGroup(
        Context context, String userId, Group group) {
        return RestClient.getUserGroupService(context)
            .addUserGroup(userId, group.id(), group);
    }

    private static rx.Observable<Group> deleteFirebaseUserGroup(
        Context context, String userId, Group group) {
        RestClient.getUserGroupService(context)
            .deleteUserGroup(userId, group.id()).subscribe();
        return Observable.just(group);
    }

}
