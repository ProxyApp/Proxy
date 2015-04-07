package com.proxy.api.model;

import android.os.Parcelable;

import auto.parcel.AutoParcel;

/**
 * Users with some random information for now.
 */
@AutoParcel
public abstract class User implements Parcelable {

    /**
     * User Constructor.
     *
     * @param firstName user first name
     * @param lastName  user last name
     * @param email     user email
     * @param imageURL  user profile picture
     * @return the entered user data
     */
    @SuppressWarnings("unused")
    public static User create(
        String firstName, String lastName, String email, String imageURL) {
        return builder().firstName(firstName).lastName(lastName).email(email)
            .userImageURL(imageURL).build();
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
     * get users first name.
     *
     * @return first name
     */
    public abstract String firstName();

    /**
     * get users last name.
     *
     * @return last name
     */
    public abstract String lastName();

    /**
     * get users email.
     *
     * @return email
     */
    public abstract String email();

    /**
     * get user profile image.
     *
     * @return profile image
     */
    public abstract String userImageURL();

    /**
     * Validation conditions.
     */
    @AutoParcel.Validate
    public void validate() {
        if (firstName().length() == 0 || lastName() == null) {
            throw new IllegalStateException("Need a valid first name");
        }
        if (lastName().length() == 0 || lastName() == null) {
            throw new IllegalStateException("Need a valid last name");
        }
    }


    /**
     * User Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * set user first name.
         *
         * @param firstName user first name
         * @return first name string
         */
        Builder firstName(String firstName);

        /**
         * set users last name.
         *
         * @param lastName user last name
         * @return last name string
         */
        Builder lastName(String lastName);

        /**
         * set user email.
         *
         * @param email this email
         * @return email string
         */
        Builder email(String email);

        /**
         * set the user profile image URL.
         *
         * @param imageURL profile image url
         * @return URL string
         */
        Builder userImageURL(String imageURL);

        /**
         * BUILD.
         *
         * @return User
         */
        User build();
    }
}
