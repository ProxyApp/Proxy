package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoGson;

import java.util.HashMap;
import java.util.UUID;

import auto.parcel.AutoParcel;

/**
 * Groups are collections of {@link User}s.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Group.class)
public abstract class Group implements Parcelable {
    /**
     * create a new blank Group.
     *
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Group createBlank() {
        String groupId = UUID.randomUUID().toString();
        Id id = Id.builder().value(groupId).build();
        return builder().id(id).label("").channels(null)
            .contacts(null).build();
    }

    public static Group create(String label) {
        String groupId = UUID.randomUUID().toString();
        Id id = Id.builder().value(groupId).build();
        return builder().id(id).label(label).channels(null)
            .contacts(null).build();
    }

    public static Group copy(Group group, String newTitle, HashMap<String, Id> channels) {
        return builder().id(group.id()).label(newTitle).channels(channels)
            .contacts(group.contacts()).build();
    }

    public static Group copy(
        Id id, String label, HashMap<String, Id> channels, HashMap<String, Id> contacts) {
        return builder().id(id).label(label).channels(channels)
            .contacts(contacts).build();
    }

    /**
     * Group builder.
     *
     * @return this Group.
     */
    public static Builder builder() {
        return new AutoParcel_Group.Builder();
    }

    /**
     * Get the ID of the {@link Group}
     *
     * @return name
     */
    public abstract Id id();

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
    public abstract HashMap<String, Id> channels();

    /**
     * Get the list of {@link Contact}s in this {@link Group}.
     *
     * @return list of {@link Contact}s
     */
    @Nullable
    public abstract HashMap<String, Id> contacts();

    /**
     * Group Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contactGroups Id.
         *
         * @param id group unique groupId
         * @return group groupId
         */
        Builder id(Id id);

        /**
         * Set the contactGroups name.
         *
         * @param label group name
         * @return label
         */
        Builder label(String label);

        /**
         * Set group channels.
         *
         * @param channels group channels
         * @return channels
         */
        @Nullable
        Builder channels(HashMap<String, Id> channels);

        /**
         * Set group contacts.
         *
         * @param contacts this contactGroups contacts
         * @return contacts
         */
        @Nullable
        Builder contacts(HashMap<String, Id> contacts);

        /**
         * BUILD.
         *
         * @return Group
         */
        Group build();
    }


}
