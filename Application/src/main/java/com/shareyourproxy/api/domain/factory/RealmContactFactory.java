package com.shareyourproxy.api.domain.factory;

import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.realm.RealmContact;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.factory.RealmChannelFactory.getRealmChannels;

/**
 * Factory for creating {@link RealmContact}s.
 */
public class RealmContactFactory {
    /**
     * Return a RealmList of Contacts from a user's contacts.
     *
     * @param contacts array of user contacts
     * @return RealmList of Contacts
     */
    public static RealmList<RealmContact> getRealmContacts(HashMap<String, Contact> contacts) {
        if (contacts != null) {
            RealmList<RealmContact> realmContactArray = new RealmList<>();
            for (Map.Entry<String, Contact> entryContact : contacts.entrySet()) {
                Contact contact = entryContact.getValue();
                realmContactArray.add(createRealmContact(contact));
            }
            return realmContactArray;
        }
        return null;
    }

    public static RealmContact getRealmContact(Contact contact) {
        return createRealmContact(contact);
    }

    private static RealmContact createRealmContact(@NonNull Contact contact) {
        RealmContact realmContact = new RealmContact();
        realmContact.setId(contact.id().value());
        realmContact.setFirst(contact.first());
        realmContact.setLast(contact.last());
        realmContact.setProfileURL(contact.profileURL());
        realmContact.setCoverURL(contact.coverURL());
        realmContact.setChannels(getRealmChannels(contact.channels()));
        return realmContact;
    }
}
