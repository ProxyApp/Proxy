package com.shareyourproxy.api.rx


import android.content.Context
import com.shareyourproxy.api.RestClient.herokuUserService
import com.shareyourproxy.api.domain.factory.UserFactory
import com.shareyourproxy.api.domain.factory.UserFactory.createModelUser
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.domain.realm.RealmUser
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback
import io.realm.Case.INSENSITIVE
import io.realm.Realm
import io.realm.RealmResults
import rx.Observable
import rx.functions.Func1
import java.util.*

/**
 * Query the realm DB for data.
 */
object RxQuery {
    fun queryUserContacts(
            context: Context, contactIds: HashSet<String>?): HashMap<String, User> {
        return Observable.just<Context>(context).map<HashMap<String, User>>(
                getUserContacts(contactIds)).toBlocking().single()
    }

    private fun getUserContacts(contactIds: HashSet<String>?): Func1<Context, HashMap<String, User>> {
        return Func1 { context ->
            val contacts: HashMap<String, User>
            if (contactIds != null) {
                contacts = HashMap<String, User>(contactIds.size)
            } else {
                return@Func1 HashMap()
            }
            val realm = Realm.getInstance(context)
            realm.refresh()
            for (contactId in contactIds) {
                val realmUser = realm.where<RealmUser>(RealmUser::class.java).equalTo("id", contactId).findFirst()
                if (realmUser != null) {
                    contacts.put(contactId, createModelUser(realmUser))
                }
            }
            realm.close()
            contacts
        }
    }

    fun queryPermissionedChannels(
            user: User, loggedInUserId: String): Observable<HashMap<String, Channel>> {
        return Observable.just<User>(user).map<HashMap<String, Channel>>(getPermissionedChannels(loggedInUserId)).compose<HashMap<String, Channel>>(observeMain<HashMap<String, Channel>>())
    }

    private fun getPermissionedChannels(
            loggedInUserId: String): Func1<User, HashMap<String, Channel>> {
        return Func1 { contact ->
            val permissionedIds = ArrayList<String>()
            val permissionedChannels = HashMap<String, Channel>()
            val channels = contact.channels
            //escape early
            if (channels.size == 0) {
                permissionedChannels
            } else {
                val groups = contact.groups
                //check the contacts groups for the logged in user and gather the channel
                // Id's of that group
                if (groups != null && groups.size > 0) {
                    for (group in groups.values) {
                        val contacts = group.contacts
                        if (contacts != null) {
                            for (contactId in contacts) {
                                if (contactId == loggedInUserId) {
                                    permissionedIds.addAll(group.channels)
                                }
                            }
                        }
                    }
                    // for the above key set data, find the channels associated
                    for (channelId in permissionedIds) {
                        val channel = contact.channels[channelId]
                        if (channel != null) {
                            permissionedChannels.put(channel.id, channel)
                        }
                    }
                }
                // add public channels
                for (channel in channels.values) {
                    if (channel.isPublic) {
                        permissionedChannels.put(channel.id, channel)
                    }
                }
                permissionedChannels
            }
        }
    }

    fun queryUser(context: Context, userId: String): User {
        return Observable.just<Context>(context).map<User>(getRealmUser(userId)).compose<User>(observeMain<User>()).toBlocking().single()
    }

    fun getUserContactScore(userId: String): Observable<Int> {
        return herokuUserService.userFollowerCount(userId).compose<Int>(observeMain<Int>())
    }

    private fun getRealmUser(userId: String): Func1<Context, User> {
        return Func1 { context -> getRealmUser(context, userId) }
    }

    fun getRealmUser(context: Context, userId: String): User {
        val realm = Realm.getInstance(context)
        realm.refresh()
        val realmUser = realm.where<RealmUser>(RealmUser::class.java).contains("id", userId).findFirst()
        val user = createModelUser(realmUser)
        realm.close()
        return user
    }

    fun searchMatchingUsers(context: Context, queryName: String, userId: String): Observable<HashMap<String, User>> {
        return Observable.concat<HashMap<String, User>>(
                searchLocalMatchingUsers(context, queryName, userId),
                searchRemoteMatchingUsers(queryName))

    }

    private fun searchLocalMatchingUsers(context: Context, queryName: String, userId: String): Observable<HashMap<String, User>> {
        return Observable.just<String>(queryName).map<HashMap<String, User>>(searchLocalUserString(context, userId))
    }

    private fun searchRemoteMatchingUsers(queryName: String): Observable<HashMap<String, User>> {
        return Observable.just<String>(queryName).map<HashMap<String, User>>(searchRemoteUserString())
    }

    private fun searchLocalUserString(context: Context, userId: String): Func1<String, HashMap<String, User>> {
        return Func1 { username -> matchLocalUsers(context, userId, username) }
    }

    private fun matchLocalUsers(context: Context, userId: String, constraint: CharSequence): HashMap<String, User> {
        val realmUsers: RealmResults<RealmUser>
        val realm = Realm.getInstance(context)
        realm.refresh()
        realm.beginTransaction()
        realmUsers = realm.where<RealmUser>(RealmUser::class.java)
                .notEqualTo("id", userId)
                .beginsWith("fullName", constraint.toString(), INSENSITIVE)
                .findAll()
        val users = UserFactory.createModelUsers(realmUsers)
        realm.commitTransaction()
        realm.close()
        return users
    }

    private fun searchRemoteUserString(): Func1<String, HashMap<String, User>> {
        return Func1 { queryName ->
            herokuUserService.searchUsers(queryName)
                    .map<HashMap<String, User>>(arrayToUserHashMap())
                    .toBlocking().single()
        }
    }

    fun arrayToUserHashMap(): Func1<ArrayList<User>, HashMap<String, User>> {
        return Func1 { users ->
            val userMap = HashMap<String, User>(users.size)
            for (user in users) {
                userMap.put(user.id, user)
            }
            userMap
        }
    }

    fun queryContactGroups(user: User, selectedContact: User?): List<GroupToggle> {
        return Observable.from<Group>(user.groups.values)
                .map<GroupToggle>(mapContacts(selectedContact))
                .toList().toBlocking().single()
    }

    private fun mapContacts(selectedContact: User?): Func1<Group, GroupToggle> {
        return Func1 { group ->
            val contactId = selectedContact.id
            if (group.contacts.contains(contactId)) {
                return@Func1 GroupToggle(group, true)
            }
            GroupToggle(group, false)
        }
    }

    fun queryContactGroups(user: User, groupToggle: ArrayList<GroupToggle>, contactId: String):
            GroupContactsUpdatedEventCallback {
        return Observable.from<GroupToggle>(groupToggle)
                .map(filterSelectedGroups())
                .filter(RxHelper.filterNullObject())
                .toList()
                .map(packageGroupContacts(user, contactId))
                .toBlocking().single()
    }

    private fun filterSelectedGroups(): Func1<GroupToggle, Group> {
        return Func1 { editContact ->
            if (editContact.isChecked) editContact.group else null
        }
    }

    private fun packageGroupContacts(user: User, contactId: String):
            Func1<List<Group>, GroupContactsUpdatedEventCallback> {
        return Func1 { groups ->
            GroupContactsUpdatedEventCallback(user, contactId, groups)
        }
    }
}
