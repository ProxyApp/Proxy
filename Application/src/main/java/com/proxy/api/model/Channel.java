package com.proxy.api.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Channels are other apps and services that you will use to communicate with {@link Contact}s.
 */
public class Channel extends RealmObject {
    @PrimaryKey
    private String channelId;
    private String label;
    private String uri;
    private String category;

    /**
     * Getter
     *
     * @return id of the channel
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
    public String getUri() {
        return uri;
    }

    /**
     * Setter.
     *
     * @param uri app uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Getter.
     *
     * @return category of the application
     */
    public String getCategory() {
        return category;
    }

    /**
     * Setter.
     *
     * @param category of application
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
