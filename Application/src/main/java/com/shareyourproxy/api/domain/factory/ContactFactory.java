package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmContact;

import java.util.HashMap;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.getModelChannels;

/**
 * Factory for creating domain model {@link Contact}s.
 */
public class ContactFactory {

    /**
     * Return a RealmList of Contacts from a user.
     *
     * @param realmContactsArray to get contacts from
     * @return RealmList of Contacts
     */
    public static HashMap<String, Contact> getModelContacts(
        RealmList<RealmContact> realmContactsArray) {
        if (realmContactsArray != null) {
            HashMap<String, Contact> contactHashMap = new HashMap<>(realmContactsArray.size());
            for (RealmContact realmContact : realmContactsArray) {
                contactHashMap.put(realmContact.getId(), createModelContact(realmContact));
            }
            return contactHashMap;
        }
        return null;
    }

    public static Contact createModelContact(User user) {
        return Contact.create(user.id(), user.first(), user.last(),
            user.profileURL(), user.coverURL(), user.channels());
    }

    public static Contact createModelContact(RealmContact realmContact) {
        return Contact.create(Id.builder().value(realmContact.getId()).build(),
            realmContact.getFirst(), realmContact.getLast(), realmContact.getProfileURL(),
            realmContact.getCoverURL(), getModelChannels(realmContact.getChannels()));
    }
}
