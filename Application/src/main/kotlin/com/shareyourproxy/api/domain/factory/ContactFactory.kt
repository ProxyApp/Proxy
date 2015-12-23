package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.api.domain.realm.RealmString
import io.realm.RealmList
import java.util.*

/**
 * Factory for creating domain model [Contact]s.
 */
object ContactFactory {
    /**
     * Convert a list of RealmString values into a HashSet of contact id Strings.
     * @param values contact ids saved in realm
     * @return HashSet of contact id values
     */
    fun getContactIdSet(values: RealmList<RealmString>): HashSet<String> {
            val contactList = HashSet<String>(values.size)
            for (realmContact in values) {
                contactList.add(realmContact.value)
            }
            return contactList
    }
}
