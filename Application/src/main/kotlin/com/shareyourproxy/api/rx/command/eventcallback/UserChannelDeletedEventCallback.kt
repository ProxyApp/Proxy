package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User

/**
 * User deleted a channel.
 */
internal final class UserChannelDeletedEventCallback(user: User, val channel: Channel, val position: Int) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(channel)
        dest.writeValue(position)
    }

    companion object {
        private val CL = UserChannelDeletedEventCallback::class.java.classLoader
        @JvmField val CREATOR: Parcelable.Creator<UserChannelDeletedEventCallback> = object : Parcelable.Creator<UserChannelDeletedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserChannelDeletedEventCallback {
                return UserChannelDeletedEventCallback(parcel.readValue(CL) as User, parcel.readValue(CL) as Channel, parcel.readValue(CL) as Int)
            }

            override fun newArray(size: Int): Array<UserChannelDeletedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
