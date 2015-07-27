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

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;

import static com.shareyourproxy.api.RestClient.getSharedLinkService;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Created by Evan on 6/8/15.
 */
public class RxUserGroupSync {
    /**
     * Private constructor.
     */
    private RxUserGroupSync() {
    }

    public static List<EventCallback> addUserGroup(
        Context context, User user, Group group) {
        return rx.Observable.zip(
            saveRealmUserGroup(context, group, user),
            saveFirebaseUserGroup(user.id().value(), group),
            zipAddUserGroup())
            .map(saveSharedLink())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    public static Func1<UserGroupAddedEventCallback, EventCallback> saveSharedLink() {
        return new Func1<UserGroupAddedEventCallback, EventCallback>() {
            @Override
            public UserGroupAddedEventCallback call(UserGroupAddedEventCallback event) {
                SharedLink link = SharedLink.create(event.user, event.group);
                getSharedLinkService()
                    .addSharedLink(link.id(), link).subscribe();
                return event;
            }
        };
    }

    public static List<EventCallback> deleteUserGroup(
        Context context, User user, Group group) {
        return Observable.zip(
            deleteRealmUserGroup(context, group, user),
            deleteFirebaseUserGroup(user.id().value(), group),
            zipDeleteUserGroup())
            .map(deleteSharedLink())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    public static Func1<UserGroupDeletedEventCallback, EventCallback>
    deleteSharedLink() {
        return new Func1<UserGroupDeletedEventCallback, EventCallback>() {
            @Override
            public UserGroupDeletedEventCallback call(UserGroupDeletedEventCallback event) {
                getSharedLinkService()
                    .deleteSharedLink(event.group.id().value()).subscribe();
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
            .compose(RxHelper.<User>applySchedulers());
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
            .compose(RxHelper.<User>applySchedulers());
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

    private static rx.Observable<Group> saveFirebaseUserGroup(String userId, Group group) {
        return RestClient.getUserGroupService().addUserGroup(userId, group.id().value(), group);
    }

    private static rx.Observable<Group> deleteFirebaseUserGroup(String userId, Group group) {
        Observable<Group> deleteObserver = RestClient.getUserGroupService()
            .deleteUserGroup(userId, group.id().value());
        deleteObserver.subscribe();
        ConnectableObservable<Group> connectableObservable = deleteObserver.publish();
        return Observable.merge(Observable.just(group), connectableObservable)
            .filter(filterNullContact());
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
