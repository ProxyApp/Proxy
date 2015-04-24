package com.proxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.proxy.api.gson.AutoGson;

import java.util.ArrayList;

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
     * @param groupId  ID of the group
     * @param label    name of the group
     * @param channels channel permissions for the group
     * @param contacts contacts in the group
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Group create(
        String groupId, String label, ArrayList<Channel> channels, ArrayList<Contact> contacts) {
        return new AutoParcel_Group(groupId, label, channels, contacts);
    }

    /**
     * Get the ID of the {@link Group}
     *
     * @return name
     */
    @Nullable
    public abstract String groupId();

    /**
     * Get the name of the {@link Group}.
     *
     * @return name
     */
    public abstract String label();

    /**
     * Get the list of {@link Channel}s tied to this {@link Group}.
     *
     * @return list of {@link Channel}s
     */
    @Nullable
    public abstract ArrayList<Channel> channels();

    /**
     * Get the list of {@link Contact}s in this {@link Group}.
     *
     * @return list of {@link Contact}s
     */
    @Nullable
    public abstract ArrayList<Contact> contacts();


}
