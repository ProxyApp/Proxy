package com.proxy.api.domain.factory;

import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.realm.RealmContact;

import java.util.List;

import io.realm.RealmList;

import static com.proxy.api.domain.factory.RealmChannelFactory.getRealmChannels;

/**
 * Factory for creating {@link RealmContact}s.
 */
public class RealmContactFactory {
    /**
     * Return a RealmList of Contacts from a user
     *
     * @param contacts array to get contacts from
     * @return RealmList of Contacts
     */
    public static RealmList<RealmContact> getRealmContacts(List<Contact> contacts) {
        if (contacts != null) {
            RealmList<RealmContact> realmContactArray = new RealmList<>();
            RealmContact realmContact = new RealmContact();

            for (Contact contact : contacts) {
                realmContact.setId(contact.id().value());
                realmContact.setLabel(contact.label());
                realmContact.setChannels(getRealmChannels(contact.channels()));
                realmContactArray.add(realmContact);
            }
            return realmContactArray;
        }
        return null;
    }
}
