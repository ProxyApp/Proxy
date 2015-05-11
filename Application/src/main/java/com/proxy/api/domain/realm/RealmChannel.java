package com.proxy.api.domain.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Channels are other apps and services that you will use to communicate with {@link
 * RealmContact}s.
 */
public class RealmChannel extends RealmObject {

    @PrimaryKey
    private String channelId;
    private String label;
    private String packageName;
    private String actionAddress;
    private RealmChannelSection section;
    private RealmChannelType channelType;

    /**
     * Getter
     *
     * @return userId of the channel
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Setter.
     *
     * @param channelId ID of the channel
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
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
     * @return section of the application
     */
    public RealmChannelSection getSection() {
        return section;
    }

    /**
     * Setter.
     *
     * @param section of intent
     */
    public void setSection(RealmChannelSection section) {
        this.section = section;
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
