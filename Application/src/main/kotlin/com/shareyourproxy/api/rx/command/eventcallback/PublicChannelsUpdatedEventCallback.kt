package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import java.util.*

/**
 * Public channel updated.
 */
internal final class PublicChannelsUpdatedEventCallback(user: User, val newChannels: HashMap<String, Channel>) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(newChannels);
    }

    companion object {
        private val CL = PublicChannelsUpdatedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<PublicChannelsUpdatedEventCallback> = object : Parcelable.Creator<PublicChannelsUpdatedEventCallback> {
            @Suppress("UNCHECKED_CAST")
            override fun createFromParcel(parcel: Parcel): PublicChannelsUpdatedEventCallback {
                return PublicChannelsUpdatedEventCallback(parcel.readValue(CL) as User, parcel.readValue(CL) as HashMap<String, Channel>)
            }

            override fun newArray(size: Int): Array<PublicChannelsUpdatedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
