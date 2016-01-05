package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Channels are other apps and services that you will share with [User] contacts.
 */
internal data class Channel(val id: String, val label: String, val channelType: ChannelType, val actionAddress: String, val isPublic: Boolean) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(label)
        dest.writeSerializable(channelType)
        dest.writeString(actionAddress)
        dest.writeValue(isPublic)
    }

    companion object {
        val CL = Channel::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<Channel> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<Channel?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = Channel(parcel.readString(), parcel.readString(), parcel.readSerializable() as ChannelType, parcel.readString(), parcel.readValue(CL) as Boolean)
    }
}