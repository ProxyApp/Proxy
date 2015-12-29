package com.shareyourproxy.api.rx

import android.content.Context
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UsersDownloadedEventCallback
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import rx.functions.Func1
import java.util.*

/**
 * Get all user contacts or save a User to firebase.
 */
object RxUserSync {
    /**
     * Download All User contacts from firebase and sync them to realm.
     * @param context      for realm instance
     * @param loggedInUser user logged into the app
     * @return [UsersDownloadedEventCallback] to rxBus
     */
    fun syncAllContacts(context: Context, loggedInUser: User): EventCallback {
        val contacts = loggedInUser.contacts
        return if (contacts.size > 0)
            getFirebaseUsers(context, loggedInUser)
                    .map(saveRealmUsers(context))
                    .map(usersDownloaded(loggedInUser))
                    .compose(RxHelper.observeMain<EventCallback>())
                    .toBlocking().single()
        else
            SyncAllContactsSuccessEvent()
    }

    fun saveUser(context: Context, newUser: User): EventCallback {
        return RestClient(context).herokuUserService
                .updateUser(newUser.id, newUser)
                .map(saveRealmUser(context))
                .compose(RxHelper.observeMain<EventCallback>())
                .toBlocking().single()
    }

    private fun getFirebaseUsers(context:Context, user: User): rx.Observable<ArrayList<User>> {
        return RestClient(context).herokuUserService.listUsers(user.contacts)
    }

    private fun saveRealmUsers(context: Context): Func1<ArrayList<User>, HashMap<String, User>> {
        return Func1 { users ->
            val usersMap = HashMap<String, User>(users.size)
            for (user in users) {
                usersMap.put(user.id, user)
            }
            updateRealmUser(context, usersMap)
            usersMap
        }
    }

    private fun saveRealmUser(context: Context): Func1<User, EventCallback> {
        return Func1 { user ->
            updateRealmUser(context, user)
            LoggedInUserUpdatedEventCallback(user)
        }
    }

    private fun usersDownloaded(loggedInUser: User): Func1<HashMap<String, User>, EventCallback> {
        return Func1 { users -> UsersDownloadedEventCallback(loggedInUser, users) }
    }
}