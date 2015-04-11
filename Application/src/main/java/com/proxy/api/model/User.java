package com.proxy.api.model;


import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Users have a basic profile that contains their specific {@link Channel}s, {@link Contact}s, and
 * {@link Group}s.
 */
public class User extends RealmObject {
    @PrimaryKey
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String imageURL;
    @Nullable
    private RealmList<Channel> channels;
    @Nullable
    private RealmList<Contact> contacts;
    @Nullable
    private RealmList<Group> groups;

    /**
     * Getter.
     *
     * @return unique user identifier
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter.
     *
     * @param userId unique identifier
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter.
     *
     * @return Users First Name/Nickname
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setter.
     *
     * @param firstName string
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter.
     *
     * @return family name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setter.
     *
     * @param lastName string
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Getter.
     *
     * @return users email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter.
     *
     * @param email string
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter.
     *
     * @return users profile picture url
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Setter.
     *
     * @param imageURL string
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Getter.
     *
     * @return users used channels list
     */
    @Nullable
    public RealmList<Channel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels list
     */
    public void setChannels(@Nullable RealmList<Channel> channels) {
        this.channels = channels;
    }

    /**
     * Getter.
     *
     * @return users contacts
     */
    @Nullable
    public RealmList<Contact> getContacts() {
        return contacts;
    }

    /**
     * Setter.
     *
     * @param contacts list
     */
    public void setContacts(@Nullable RealmList<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Getter.
     *
     * @return users groups
     */
    @Nullable
    public RealmList<Group> getGroups() {
        return groups;
    }

    /**
     * Setter.
     *
     * @param groups list
     */
    public void setGroups(@Nullable RealmList<Group> groups) {
        this.groups = groups;
    }


}
