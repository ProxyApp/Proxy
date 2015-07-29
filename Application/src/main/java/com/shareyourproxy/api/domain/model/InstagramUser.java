package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoGson;

import java.net.URI;
import java.net.URISyntaxException;

import auto.parcel.AutoParcel;

/**
 * Instagram User Object.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_InstagramUser.class)
public abstract class InstagramUser implements Parcelable {

    /**
     * User Constructor.
     *
     * @param id            user unique ID
     * @param username      user's username
     * @param fullName      users full name
     * @param profilePicURL picture url
     * @param bio           users biography
     * @param website       users website
     * @return the entered user data
     */
    public static InstagramUser create(
        String id, String username, String fullName, String profilePicURL, String bio,
        String website) {
        return builder().id(id).username(username).fullName(fullName).profilePicture(profilePicURL)
            .bio(bio).website(website).build();
    }

    public static String requestOAuthUrl(
        final String clientId, final String redirectUri) throws URISyntaxException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("response_type=").append("code");
        urlBuilder.append("&client_id=").append(clientId);
        urlBuilder.append("&redirect_uri=").append(redirectUri);
        return new URI("https", "instagram.com", "/oauth/authorize", urlBuilder.toString(), null)
            .toString();
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
        return new AutoParcel_InstagramUser.Builder();
    }


    public abstract String id();

    public abstract String username();

    public abstract String fullName();

    public abstract String profilePicture();

    public abstract String bio();

    public abstract String website();

    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set user id.
         *
         * @param id user unique id
         * @return user id
         */
        Builder id(String id);

        /**
         * Set user first name.
         *
         * @param userName user's username
         * @return username
         */
        Builder username(String userName);

        /**
         * Set users full name
         *
         * @param fullName user full name
         * @return name string
         */
        Builder fullName(String fullName);

        /**
         * Set user profile url.
         *
         * @param pictureUrl users profile picture url
         * @return profile picture url
         */
        Builder profilePicture(String pictureUrl);

        /**
         * Set the user biography.
         *
         * @param bio profile bio
         * @return bio
         */
        Builder bio(String bio);

        /**
         * Set this users website.
         *
         * @param website user webstie
         * @return List {@link Contact}
         */
        Builder website(String website);

        /**
         * BUILD.
         *
         * @return InstagramUser
         */
        InstagramUser build();
    }
}
