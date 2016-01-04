package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.api.domain.realm.RealmString
import io.realm.RealmList
import java.util.*

/**
 * Factory for creating [RealmContact]s.
 */
internal object RealmContactFactory {
    /**
     * Return a RealmList of Contacts from a user's contacts.
     * @param contacts array of user contacts
     * @return RealmList of Contacts
     */
    fun getRealmContacts(contacts: HashSet<String>): RealmList<RealmString> {
        val realmContactArray = RealmList<RealmString>()
        for (id in contacts) {
            val realmContact = RealmString()
            realmContact.value = id
            realmContactArray.add(realmContact)
        }
        return realmContactArray
    }
}
