package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User

/**
 * A newChannel was added or updated. If oldChannel is non-null, then this is an update.
 */
class UserChannelAddedEventCallback(user: User, val oldChannel: Channel?, val newChannel: Channel) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(oldChannel)
        dest.writeValue(newChannel)
    }

    companion object {
        private val CL = UserChannelAddedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserChannelAddedEventCallback> = object : Parcelable.Creator<UserChannelAddedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserChannelAddedEventCallback {
                return UserChannelAddedEventCallback(parcel.readValue(CL) as User, parcel.readValue(CL) as Channel, parcel.readValue(CL) as Channel)
            }

            override fun newArray(size: Int): Array<UserChannelAddedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
