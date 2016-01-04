package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.api.domain.factory.ChannelFactory.getModelChannelList
import com.shareyourproxy.api.domain.factory.ContactFactory.getContactIdSet
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.realm.RealmGroup
import io.realm.RealmList
import java.util.*

/**
 * Factory for creating domain model [Group]s.
 */
internal object GroupFactory {
    const val PUBLIC: String = "public";
    const val BLANK: String = "";

    /**
     * Return a RealmList of Contacts from a user
     * @param realmGroupArray to get contactGroups from
     * @return RealmList of Contacts
     */
    fun getModelGroups(realmGroupArray: RealmList<RealmGroup>): HashMap<String, Group> {
        val groups = HashMap<String, Group>(realmGroupArray.size)
        for (realmGroup in realmGroupArray) {
            groups.put(realmGroup.id, getModelGroup(realmGroup))
        }
        return groups
    }

    fun getModelGroup(realmGroup: RealmGroup): Group {
        return Group(realmGroup.id, realmGroup.label, getModelChannelList(realmGroup.channels), getContactIdSet(realmGroup.contacts))
    }

    fun addGroupChannels(newTitle: String, oldGroup: Group, channels: HashSet<String>): Group {
        return Group(oldGroup.id, newTitle, channels, oldGroup.contacts)
    }

    fun createPublicGroup() :Group{
        return Group(PUBLIC, PUBLIC, HashSet(),HashSet())
    }

    fun createBlankGroup() :Group{
        return Group(UUID.randomUUID().toString(), BLANK, HashSet(),HashSet())
    }

}
