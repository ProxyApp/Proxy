package com.proxy.api.domain.factory;

import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.model.Id;
import com.proxy.api.domain.realm.RealmContact;

import java.util.ArrayList;

import io.realm.RealmList;

import static com.proxy.api.domain.factory.ChannelFactory.getModelChannels;

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
    public static ArrayList<Contact> getModelContacts(RealmList<RealmContact> realmContactsArray) {
        if (realmContactsArray != null) {
            ArrayList<Contact> contactArrayList = new ArrayList<>();
            for (RealmContact realmContact : realmContactsArray) {
                contactArrayList.add(
                    Contact.create(Id.builder().value(realmContact.getId()).build(),
                    realmContact.getLabel(), getModelChannels(realmContact.getChannels())));
            }
            return contactArrayList;
        }
        return null;
    }

}
