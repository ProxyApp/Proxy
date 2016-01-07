package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.api.domain.factory.ChannelFactory.getModelChannels
import com.shareyourproxy.api.domain.factory.ContactFactory.getContactIdSet
import com.shareyourproxy.api.domain.factory.GroupFactory.getModelGroups
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.domain.realm.RealmUser
import io.realm.RealmResults
import java.util.*


/**
 * Factory for creating domain model [User]s.
 */
internal object UserFactory {

    /**
     * Take in a realm user and create a model user.
     * @param realmUser to copy
     * @return model user
     */
    fun createModelUser(realmUser: RealmUser): User {
        return User(realmUser.id, realmUser.first, realmUser.last, realmUser.fullName, realmUser.email, realmUser.profileURL,
                realmUser.coverURL, getModelChannels(realmUser.channels), getContactIdSet(realmUser.contacts), getModelGroups(realmUser.groups),VERSION_CODE)
    }

    /**
     * Create a HashMap of Users from the input realm results.
     * @param realmUsers RealmUsers
     * @return User Map
     */
    fun createModelUsers(realmUsers: RealmResults<RealmUser>): HashMap<String, User> {
        val users = HashMap<String, User>()
        realmUsers.forEach { users.put(it.id, createModelUser(it)) }
        return users
    }

    /**
     * Create the same [User] with the updated channel value.

     * @param user to copy
     * *
     * @return updated user
     */
    fun addUserChannel(user: User, channel: Channel): User {
        user.channels.put(channel.id, channel)
        return user.copy(channels = user.channels)
    }

    /**
     * Create the same [User] without the input channel value.
     * @param user to copy
     * @return updated user
     */
    fun deleteUserChannel(user: User, channel: Channel): User {
        user.channels.remove(channel.id)
        return user.copy(channels = user.channels)
    }

    /**
     * Create the same [User] with the updated contact id value.
     * @param user to copy
     * @return updated user
     */
    fun addUserContact(user: User, contactId: String): User {
        user.contacts.add(contactId)
        return user.copy(contacts = user.contacts)
    }

    /**
     * Create the same [User] without the input contact id value.
     * @param user to copy
     * @return updated user
     */
    fun deleteUserContact(user: User, contactId: String): User {
        user.contacts.remove(contactId)
        return user.copy(contacts = user.contacts)
    }

    /**
     * Create the same [User] with the updated [Group] value.
     * @param user to copy
     * @return updated user
     */
    fun addUserGroup(user: User, newGroup: Group): User {
        user.groups.put(newGroup.id, newGroup)
        return user.copy(groups = user.groups)
    }

    /**
     * Create the same [User] without the input [Group] value.
     * @param user to copy
     * @return updated user
     */
    fun deleteUserGroup(user: User, group: Group): User {
        user.groups.remove(group.id)
        return user.copy(groups = user.groups)
    }
}
