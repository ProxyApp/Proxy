package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.realm.RealmGroup
import com.shareyourproxy.api.domain.realm.RealmString
import io.realm.RealmList
import java.util.*

/**
 * Factory for creating [RealmGroup]s.
 */
internal object RealmGroupFactory {

    /**
     * Return a RealmList of Contacts from a user
     * @param groupHashMap to get contacts from
     * @return RealmList of Contacts
     */
    fun getRealmGroups(groupHashMap: HashMap<String, Group>): RealmList<RealmGroup> {
        val realmGroupArray = RealmList<RealmGroup>()
        groupHashMap.entries.forEach {
            val group = it.value
            val realmGroup = RealmGroup()
            realmGroup.id = group.id
            realmGroup.label = group.label
            realmGroup.channels = createRealmStringList(group.channels)
            realmGroup.contacts = createRealmStringList(group.contacts)
            realmGroupArray.add(realmGroup)
        }
        return realmGroupArray
    }

    private fun createRealmStringList(values: HashSet<String>?): RealmList<RealmString> {
        val list = RealmList<RealmString>()
        return when (values) {
            null -> list
            else -> {
                for (id in values) {
                    val newChannel = RealmString()
                    newChannel.value = id
                    list.add(newChannel)
                }
                list
            }
        }
    }
}
