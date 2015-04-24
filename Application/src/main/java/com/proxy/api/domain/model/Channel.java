package com.proxy.api.domain.model;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.proxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Channels are other apps and services that you will use to communicate with {@link Contact}s.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Channel.class)
public abstract class Channel implements Parcelable {

    /**
     * Create a new {@link Channel}.
     *
     * @param id             unique userId
     * @param label          name of the group
     * @param packageName    Name of the channels package
     * @param channelSection section divider category
     * @param channelType    channel intent type
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Channel create(
        String id, String label, String packageName,
        ChannelSection channelSection, ChannelType channelType, String actionAddress) {
        return new AutoParcel_Channel(id, label, packageName, channelSection, channelType,
            actionAddress);
    }

    /**
     * Get the ID of the {@link Channel}.
     *
     * @return name
     */
    @Nullable
    public abstract String channelId();

    /**
     * Get the name of the {@link Channel}.
     *
     * @return channel label
     */
    public abstract String label();

    /**
     * Get the name of the {@link Uri} {@link Intent}.
     *
     * @return uri
     */
    public abstract String packageName();

    /**
     * Get the channelSection, or section header for this {@link Channel}.
     *
     * @return header string
     */
    public abstract ChannelSection channelSection();

    /**
     * Channel image resource.
     *
     * @return image resource
     */
    public abstract ChannelType channelType();

    /**
     * Channel image resource.
     *
     * @return image resource
     */
    public abstract String actionAddress();

}