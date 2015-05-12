package com.proxy.api.domain.realm;

import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Groups only have names for now.
 */
public class RealmGroup extends RealmObject {

    @PrimaryKey
    private String groupId;
    private String label;
    private RealmList<RealmChannel> channels;
    private RealmList<RealmContact> contacts;


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
     * @return channel permissions shared with this {@link RealmGroup}
     */
    public RealmList<RealmChannel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels group channels
     */
    public void setChannels(RealmList<RealmChannel> channels) {
        this.channels = channels;
    }

    /**
     * Getter.
     *
     * @return contacts in this {@link RealmGroup}
     */
    public RealmList<RealmContact> getContacts() {
        return contacts;
    }

    /**
     * Setter.
     *
     * @param contacts in this {@link RealmGroup}
     */
    public void setContacts(RealmList<RealmContact> contacts) {
        this.contacts = contacts;
    }

}
