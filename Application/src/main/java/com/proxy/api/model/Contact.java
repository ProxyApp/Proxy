package com.proxy.api.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Contacts are {@link User}s who you'd like to communicate with.
 */
public class Contact extends RealmObject {

    @PrimaryKey
    private String contactId;
    private String label;
    private RealmList<Channel> channels;

    /**
     * Getter.
     *
     * @return unique contactId
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * Setter.
     *
     * @param contactId string
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * Getter.
     *
     * @return contact's name
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter.
     *
     * @param label string
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Getter.
     *
     * @return Contact's channel permissions
     */
    public RealmList<Channel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels list
     */
    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }

}
