package com.proxy.api.domain.model;

import android.os.Parcelable;

import com.proxy.api.gson.AutoGson;


import auto.parcel.AutoParcel;

/**
 * Used to signify if a {@link Channel} is in a {@link Group} or not.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_GroupEditChannel.class)
public abstract class GroupEditChannel implements Parcelable{

    public static GroupEditChannel create(Channel channel, boolean inGroup) {
        return builder().channel(channel).inGroup(inGroup).build();
    }

    /**
     * EditGroupChannel builder.
     *
     * @return this EditGroupChannel.
     */
    public static Builder builder() {
        return new AutoParcel_GroupEditChannel.Builder();
    }


    public abstract Channel channel();
    public abstract boolean inGroup();

    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contacts Id.
         *
         * @param id contact unique id
         * @return contact id
         */
        Builder channel(Channel channel);

        /**
         * Set the contacts name.
         *
         * @param label contact name
         * @return label
         */
        Builder inGroup(boolean inGroup);

        /**
         * BUILD.
         *
         * @return Contact
         */
        GroupEditChannel build();
    }
}
