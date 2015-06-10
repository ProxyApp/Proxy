package com.shareyourproxy.api.domain.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Contacts are {@link RealmUser}s who you'd like to communicate with.
 */
public class RealmContact extends RealmObject {

    @PrimaryKey
    private String id;
    private String first;
    private String last;
    private String imageURL;
    private RealmList<RealmChannel> channels;

    /**
     * Getter.
     *
     * @return unique id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter.
     *
     * @param id string
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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
