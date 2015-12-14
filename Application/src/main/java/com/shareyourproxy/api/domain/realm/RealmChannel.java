package com.shareyourproxy.api.domain.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Channels are other apps and services that you will use to communicate with {@link RealmContact}s.
 */
public class RealmChannel extends RealmObject {

    @PrimaryKey
    private String id;
    private String label;
    private String actionAddress;
    private RealmChannelType channelType;
    private boolean isPublic;

    /**
     * Getter
     *
     * @return id of the newChannel
     */
    public String getId() {
        return id;
    }

    /**
     * Setter.
     *
     * @param id ID of the newChannel
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter.
     *
     * @return newChannel label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter.
     *
     * @param label of the newChannel
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public RealmChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(RealmChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Getter.
     *
     * @return action address
     */
    public String getActionAddress() {
        return actionAddress;
    }

    public void setActionAddress(String actionAddress) {
        this.actionAddress = actionAddress;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

}
