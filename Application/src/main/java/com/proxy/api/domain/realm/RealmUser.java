package com.proxy.api.domain.realm;


import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Users have a basic profile that contains their specific {@link RealmChannel}s, {@link RealmContact}s, and
 * {@link RealmGroup}s.
 */
public class RealmUser extends RealmObject {
    @PrimaryKey
    @SerializedName("userId")
    private String userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String imageURL;
    @Nullable
    private RealmList<RealmChannel> channels;
    @Nullable
    private RealmList<RealmContact> contacts;
    @Nullable
    private RealmList<RealmGroup> groups;

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
     * @return fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Setter.
     * @param fullName string
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    public RealmList<RealmChannel> getChannels() {
        return channels;
    }

    /**
     * Setter.
     *
     * @param channels list
     */
    public void setChannels(@Nullable RealmList<RealmChannel> channels) {
        this.channels = channels;
    }

    /**
     * Getter.
     *
     * @return users contacts
     */
    @Nullable
    public RealmList<RealmContact> getContacts() {
        return contacts;
    }

    /**
     * Setter.
     *
     * @param contacts list
     */
    public void setContacts(@Nullable RealmList<RealmContact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Getter.
     *
     * @return users groups
     */
    @Nullable
    public RealmList<RealmGroup> getGroups() {
        return groups;
    }

    /**
     * Setter.
     *
     * @param groups list
     */
    public void setGroups(@Nullable RealmList<RealmGroup> groups) {
        this.groups = groups;
    }


}
