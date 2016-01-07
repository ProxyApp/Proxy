package com.shareyourproxy.api.rx

import android.content.Context
import android.util.Pair
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.factory.UserFactory
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserContactDeletedEventCallback
import rx.Observable
import rx.functions.Func1
import rx.functions.Func2
import java.util.*

/**
 * Sync contacts asynchronously.
 */
internal object RxUserContactSync {
    fun checkContacts(context: Context, user: User, contacts: ArrayList<String>, userGroups: HashMap<String, Group>): EventCallback {
        return Observable.from(contacts)
                .map(findContactsToDelete(userGroups))
                .filter(filterMissingContacts())
                .map(unwrapPairedContact())
                .flatMap(zipDeleteUserContact(context, user))
                .toBlocking().single()
    }

    private fun zipDeleteUserContact(): Func2<User, String, EventCallback> {
        return Func2 { user, contactId -> UserContactDeletedEventCallback(user, contactId) }
    }

    private fun deleteRealmUserContact(
            context: Context, user: User, contactId: String): Observable<User> {
        return Observable.just(contactId).map(deleteRealmUserContact(context, user))
    }

    private fun deleteRealmUserContact(context: Context, user: User): Func1<String, User> {
        return Func1 { contactId ->
            val newUser = UserFactory.deleteUserContact(user, contactId)
            updateRealmUser(context, newUser)
            newUser
        }
    }

    private fun deleteFirebaseUserContact(context :Context, userId: String, contactId: String): Observable<String> {
        RestClient(context).herokuUserService.deleteUserContact(userId, contactId).subscribe()
        return Observable.just(contactId)
    }

    private fun findContactsToDelete(userGroups: HashMap<String, Group>?): Func1<String, Pair<String?, Boolean>> {
        return Func1 { contactId ->
            getUserGroupPair(contactId, userGroups)
        }
    }

    private fun getUserGroupPair(contactId: String?, userGroups: HashMap<String, Group>?): Pair<String?, Boolean> {
        if (userGroups != null) {
            userGroups.entries.forEach {
                val group = it.value
                val groupContacts = group.contacts
                for (groupContactId in groupContacts) {
                    if (groupContactId == contactId) {
                        return Pair(groupContactId, true)
                    }
                }
                return Pair(contactId, false)
            }
        }
        return Pair(contactId, false)
    }

    private fun filterMissingContacts(): Func1<in Pair<String?, Boolean>, Boolean>? {
        return Func1 { contactPair ->
            // we only want contacts to remove... false means remove
            !contactPair.second
        }
    }

    private fun unwrapPairedContact(): Func1<in Pair<String?, Boolean>, out String>? {
        return Func1 { contact -> contact.first }
    }

    private fun zipDeleteUserContact(context: Context, user: User): Func1<String, Observable<EventCallback>> {
        return Func1 { contactId ->
            Observable.zip(
                    deleteRealmUserContact(context, user, contactId),
                    deleteFirebaseUserContact(context, user.id, contactId),
                    zipDeleteUserContact())
        }
    }
}
