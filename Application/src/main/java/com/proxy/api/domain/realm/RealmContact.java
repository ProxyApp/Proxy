package com.proxy.api.domain.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Contacts are {@link RealmUser}s who you'd like to communicate with.
 */
public class RealmContact extends RealmObject {

    @PrimaryKey
    private String contactId;
    private String label;
    private RealmList<RealmChannel> channels;

    /**
     * Getter.
     *
     * @return unique id
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
    public RealmList<RealmChannel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels list
     */
    public void setChannels(RealmList<RealmChannel> channels) {
        this.channels = channels;
    }

}
