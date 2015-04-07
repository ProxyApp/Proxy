package com.proxy.api.model;

import android.os.Parcelable;

import com.proxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Groups only have names for now.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Group.class)
public abstract class Group implements Parcelable {
    /**
     * create a new Group.
     *
     * @param name name of the group
     * @return Immutible group
     */
    @SuppressWarnings("unused")
    public static Group create(String name) {
        return new AutoParcel_Group(name);
    }

    /**
     * get the name of the group.
     *
     * @return name
     */
    public abstract String name();

}
