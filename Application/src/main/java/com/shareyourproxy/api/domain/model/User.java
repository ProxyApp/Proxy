package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoGson;

import java.util.ArrayList;

import auto.parcel.AutoParcel;

/**
 * Users have a basic profile that contains their specific {@link Channel}s, {@link Contact}s, and
 * {@link Group}s.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_User.class)
public abstract class User implements Parcelable {

    /**
     * User Constructor.
     *
     * @param id        user unique ID
     * @param firstName user first name
     * @param lastName  user last name
     * @param email     user email
     * @param imageURL  user profile picture
     * @param channels  user channels
     * @param contacts  user contacts
     * @param groups    user groups
     * @return the entered user data
     */
    public static User create(
        Id id, String firstName, String lastName, String email, String imageURL,
        ArrayList<Channel> channels, ArrayList<Group> groups, ArrayList<Contact> contacts) {
        return builder().id(id).first(firstName).last(lastName).email(email)
            .imageURL(imageURL).channels(channels).groups(groups).contacts(contacts).build();
    }

    /**
     * User builder.
     *
     * @return this User.
     */
    public static Builder builder() {
        // The subclass AutoParcel_PackagelessValueType is created by the annotation processor
        // that is triggered by the presence of the @AutoParcel annotation. It has a constructor
        // for each of the abstract getter methods here, in order. The constructor stashes the
        // values here in private final fields, and each method is implemented to return the
        // value of the corresponding field.
        return new AutoParcel_User.Builder();
    }

    /**
     * Get users unique ID.
     *
     * @return first name
     */
    public abstract Id id();

    /**
     * Get users first name.
     *
     * @return first name
     */
    public abstract String first();

    /**
     * Get users last name.
     *
     * @return last name
     */
    public abstract String last();

    /**
     * Get users email.
     *
     * @return email
     */
    @Nullable
    public abstract String email();


    /**
     * Get user profile image.
     *
     * @return profile image
     */
    public abstract String imageURL();

    /**
     * Get users channels.
     *
     * @return channels
     */
    @Nullable
    public abstract ArrayList<Channel> channels();

    /**
     * Get users contacts.
     *
     * @return contacts
     */
    @Nullable
    public abstract ArrayList<Contact> contacts();

    /**
     * Get users groups.
     *
     * @return groups
     */
    @Nullable
    public abstract ArrayList<Group> groups();

    /**
     * Validation conditions.
     */
    @AutoParcel.Validate
    public void validate() {
        if (first().length() == 0 || last() == null) {
            throw new IllegalStateException("Need a valid first name");
        }
        if (last().length() == 0 || last() == null) {
            throw new IllegalStateException("Need a valid last name");
        }
    }


    /**
     * User Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set user id.
         *
         * @param id user unqiue id
         * @return user id
         */
        Builder id(Id id);

        /**
         * Set user first name.
         *
         * @param firstName user first name
         * @return first name string
         */
        Builder first(String firstName);

        /**
         * Set users last name.
         *
         * @param lastName user last name
         * @return last name string
         */
        Builder last(String lastName);

        /**
         * Set user email.
         *
         * @param email this email
         * @return email string
         */
        Builder email(String email);

        /**
         * Set the user profile image URL.
         *
         * @param imageURL profile image url
         * @return URL string
         */
        Builder imageURL(String imageURL);

        /**
         * Set this {@link User}s {@link Contact}s
         *
         * @param contacts user contacts
         * @return List {@link Contact}
         */
        @Nullable
        Builder contacts(ArrayList<Contact> contacts);

        /**
         * Set this {@link User}s {@link Group}s
         *
         * @param groups user groups
         * @return List {@link Group}
         */
        @Nullable
        Builder groups(ArrayList<Group> groups);

        /**
         * Set this {@link User}s {@link Channel}s
         *
         * @param channels user channels
         * @return List {@link Channel}
         */
        @Nullable
        Builder channels(ArrayList<Channel> channels);

        /**
         * BUILD.
         *
         * @return User
         */
        User build();
    }

}
