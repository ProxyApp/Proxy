package com.proxy.api.domain.model;

import android.os.Parcelable;

import com.proxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Complex object to hold unique id data.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Id.class)
public abstract class Id implements Parcelable {

    /**
     * User Constructor.
     *
     * @param id        user unique ID
     * @return the entered user data
     */
    public static Id create(String id) {
        return builder().value(id).build();
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
        return new AutoParcel_Id.Builder();
    }

    /**
     * Get users unique ID.
     *
     * @return first name
     */
    public abstract String value();

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
        Builder value(String id);

        /**
         * BUILD.
         *
         * @return User
         */
        Id build();
    }
}
