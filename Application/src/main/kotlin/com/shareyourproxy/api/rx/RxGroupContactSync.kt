package com.shareyourproxy.api.rx

import android.content.Context
import android.util.Pair
import com.shareyourproxy.api.RestClient.userService
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback
import rx.Observable
import rx.functions.Func1
import java.util.*

/**
 * Update Group contacts and User contacts when they've been added or removed to any groups.
 */
object RxGroupContactSync {
    fun updateGroupContacts(context: Context, user: User, editGroups: ArrayList<GroupToggle>, contact: User): EventCallback {
        return Observable.just(editGroups).map(userUpdateContacts(user, contact.id)).map(saveUserToDB(context, contact)).map(createGroupContactEvent(contact.id)).toBlocking().single()
    }

    fun saveUserToDB(context: Context, contact: User): Func1<Pair<User, List<Group>>, Pair<User, List<Group>>> {
        return Func1 { userListPair ->
            val newUser = userListPair.first
            updateRealmUser(context, newUser)
            updateRealmUser(context, contact)
            userService.updateUser(newUser.id, newUser).subscribe()
            userListPair
        }
    }

    private fun userUpdateContacts(user: User, contactId: String): Func1<ArrayList<GroupToggle>, Pair<User, List<Group>>> {
        return Func1 { groupToggles ->
            var groupHasContact = false
            val contactInGroup = ArrayList<Group>()
            for (groupToggle in groupToggles) {
                val groupId = groupToggle.group.id
                if (groupToggle.isChecked) {
                    groupHasContact = true
                    user.groups[groupId]?.contacts?.add(contactId)
                    contactInGroup.add(user.groups[groupId]!!)
                } else {
                    user.groups[groupId]?.contacts?.remove(contactId)
                }
            }
            if (groupHasContact) {
                user.contacts.add(contactId)
            } else {
                user.contacts.remove(contactId)
            }
            Pair(user, contactInGroup)
        }
    }

    private fun createGroupContactEvent(contactId: String): Func1<Pair<User, List<Group>>, EventCallback> {
        return Func1 { groups ->
            GroupContactsUpdatedEventCallback(groups.first, contactId, groups.second)
        }
    }
}

