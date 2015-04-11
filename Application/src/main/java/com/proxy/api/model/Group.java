package com.proxy.api.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Groups only have names for now.
 */
public class Group extends RealmObject {

    @PrimaryKey
    private String groupId;
    private String label;
    private RealmList<Channel> channels;
    private RealmList<Contact> contacts;


    /**
     * Getter.
     *
     * @return unique group identifier
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Setter.
     *
     * @param groupId unique identifier
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Getter.
     *
     * @return groups name
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter.
     *
     * @param label name
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Getter.
     *
     * @return channel permissions shared with this {@link Group}
     */
    public RealmList<Channel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels group channels
     */
    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }

    /**
     * Getter.
     *
     * @return contacts in this {@link Group}
     */
    public RealmList<Contact> getContacts() {
        return contacts;
    }

    /**
     * Setter.
     *
     * @param contacts in this {@link Group}
     */
    public void setContacts(RealmList<Contact> contacts) {
        this.contacts = contacts;
    }

}
