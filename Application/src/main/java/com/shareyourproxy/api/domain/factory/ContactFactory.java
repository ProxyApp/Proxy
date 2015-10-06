package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashSet;

import io.realm.RealmList;

/**
 * Factory for creating domain model {@link Contact}s.
 */
public class ContactFactory {

    /**
     * Convert a list of RealmString values into a HashSet of contact id Strings.
     *
     * @param values contact ids saved in realm
     * @return HashSet of contact id values
     */
    public static HashSet<String> getContactIdSet(RealmList<RealmString> values) {
        if (values != null) {
            HashSet<String> contactList = new HashSet<>(values.size());
            for (RealmString realmContact : values) {
                contactList.add(realmContact.getValue());
            }
            return contactList;
        }
        return null;
    }

}
