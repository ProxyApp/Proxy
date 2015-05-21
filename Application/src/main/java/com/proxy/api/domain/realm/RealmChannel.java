package com.proxy.api.domain.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Channels are other apps and services that you will use to communicate with {@link
 * RealmContact}s.
 */
public class RealmChannel extends RealmObject {

    @PrimaryKey
    private String id;
    private String label;
    private String packageName;
    private String actionAddress;
    private RealmChannelSection channelSection;
    private RealmChannelType channelType;

    /**
     * Getter
     *
     * @return id of the channel
     */
    public String getId() {
        return id;
    }

    /**
     * Setter.
     *
     * @param id ID of the channel
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter.
     *
     * @return channel label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter.
     *
     * @param label of the channel
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Getter.
     *
     * @return URI of the application to be called
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Setter.
     *
     * @param packageName app packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Getter.
     *
     * @return channelSection of the application
     */
    public RealmChannelSection getChannelSection() {
        return channelSection;
    }

    /**
     * Setter.
     *
     * @param channelSection of intent
     */
    public void setChannelSection(RealmChannelSection channelSection) {
        this.channelSection = channelSection;
    }

    public RealmChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(RealmChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * @return
     */
    public String getActionAddress() {
        return actionAddress;
    }

    public void setActionAddress(String actionAddress) {
        this.actionAddress = actionAddress;
    }

}
