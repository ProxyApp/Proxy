package com.shareyourproxy.api.domain.realm;


import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Users have a basic profile that contains their specific {@link RealmChannel}s, {@link
 * RealmContact}s, and {@link RealmGroup}s.
 */
public class RealmUser extends RealmObject {
    @PrimaryKey
    private String id;
    private String first;
    private String last;
    private String fullName;
    private String email;
    private String profileURL;
    @Nullable
    private String coverURL;
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
    public String getId() {
        return id;
    }

    /**
     * Setter.
     *
     * @param id unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter.
     *
     * @return Users First Name/Nickname
     */
    public String getFirst() {
        return first;
    }

    /**
     * Setter.
     *
     * @param first string
     */
    public void setFirst(String first) {
        this.first = first;
    }

    /**
     * Getter.
     *
     * @return family name
     */
    public String getLast() {
        return last;
    }

    /**
     * Setter.
     *
     * @param last string
     */
    public void setLast(String last) {
        this.last = last;
    }

    /**
     * Getter.
     *
     * @return fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Setter.
     *
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
    public String getProfileURL() {
        return profileURL;
    }

    /**
     * Setter.
     *
     * @param profileURL string
     */
    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    @Nullable
    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(@Nullable String coverURL) {
        this.coverURL = coverURL;
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
     * @return users contactGroups
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
