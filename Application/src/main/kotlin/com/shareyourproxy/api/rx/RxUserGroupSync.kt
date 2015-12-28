package com.shareyourproxy.api.rx

import android.content.Context
import com.shareyourproxy.api.RestClient.herokuUserService
import com.shareyourproxy.api.domain.factory.UserFactory
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.SharedLink
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback
import rx.Observable
import rx.functions.Func1
import rx.functions.Func2

/**
 * Add and delete user groups.
 */
object RxUserGroupSync {
    fun addUserGroup(context: Context, user: User, group: Group): EventCallback {
        return Observable.zip(
                saveRealmUserGroup(context, group, user),
                saveFirebaseUserGroup(user.id, group),
                zipAddUserGroup()).map(saveSharedLink()).compose(RxHelper.observeMain<EventCallback>()).toBlocking().single()
    }

    fun deleteUserGroup(context: Context, user: User, group: Group): EventCallback {
        return Observable.zip(
                deleteRealmUserGroup(context, group, user),
                deleteFirebaseUserGroup(user.id, group),
                zipDeleteUserGroup()).map(deleteSharedLink()).compose(RxHelper.observeMain<EventCallback>()).toBlocking().single()
    }

    private fun deleteSharedLink(): Func1<UserGroupDeletedEventCallback, EventCallback> {
        return Func1 { event ->
            herokuUserService.deleteSharedLink(event.group.id).subscribe()
            event
        }
    }

    private fun saveSharedLink(): Func1<UserGroupAddedEventCallback, EventCallback> {
        return Func1 { event ->
            val link = SharedLink(event.user.id, event.group.id)
            herokuUserService.addSharedLink(link.id, link).subscribe()
            event
        }
    }

    private fun zipAddUserGroup(): Func2<User, Group, UserGroupAddedEventCallback> {
        return Func2 { user, group -> UserGroupAddedEventCallback(user, group) }
    }

    private fun zipDeleteUserGroup(): Func2<User, Group, UserGroupDeletedEventCallback> {
        return Func2 { user, group -> UserGroupDeletedEventCallback(user, group) }
    }

    private fun saveRealmUserGroup(context: Context, group: Group, user: User): Observable<User> {
        return Observable.just(group).map(addRealmUserGroup(context, user)).compose(RxHelper.observeMain<User>())
    }

    private fun addRealmUserGroup(context: Context, user: User): Func1<Group, User> {
        return Func1 { group ->
            val newUser = UserFactory.addUserGroup(user, group)
            updateRealmUser(context, newUser)
            newUser
        }
    }

    private fun deleteRealmUserGroup(context: Context, group: Group, user: User): Observable<User> {
        return Observable.just(group)
                .map(deleteRealmUserGroup(context, user))
                .compose(RxHelper.observeMain<User>())
    }

    private fun deleteRealmUserGroup(context: Context, user: User): Func1<Group, User> {
        return Func1 { group ->
            val newUser = UserFactory.deleteUserGroup(user, group)
            updateRealmUser(context, newUser)
            newUser
        }
    }

    private fun saveFirebaseUserGroup(userId: String, group: Group): Observable<Group> {
        return herokuUserService.addUserGroup(userId, group.id, group)
    }

    private fun deleteFirebaseUserGroup(userId: String, group: Group): Observable<Group> {
        herokuUserService.deleteUserGroup(userId, group.id).subscribe()
        return Observable.just(group)
    }
}
