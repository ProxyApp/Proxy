package com.proxy.api.domain.factory;

import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.realm.RealmContact;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

import static com.proxy.api.domain.factory.ChannelFactory.getModelChannels;
import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannels;

/**
 * Factory for creating Contacts.
 */
public class ContactFactory {

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
                realmContact.setContactId(contact.id());
                realmContact.setLabel(contact.label());
                realmContact.setChannels(getRealmChannels(contact.channels()));
                realmContactArray.add(realmContact);
            }
            return realmContactArray;
        }
        return null;
    }

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param realmContactsArray to get contacts from
     * @return RealmList of Contacts
     */
    public static ArrayList<Contact> getModelContacts(RealmList<RealmContact> realmContactsArray) {
        if(realmContactsArray != null) {
            ArrayList<Contact> contactArrayList = new ArrayList<>();
            for (RealmContact realmContact : realmContactsArray) {
                contactArrayList.add(Contact.create(realmContact.getContactId(),
                    realmContact.getLabel(), getModelChannels(realmContact.getChannels())));
            }
            return contactArrayList;
        }
        return null;
    }

}
