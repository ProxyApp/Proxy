package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Message

/**
 * User message added.
 * @param message notification content
 */
internal final class UserMessageAddedEventCallback(val message: Message) : EventCallback() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(message)
    }

    companion object {
        private val CL = UserMessageAddedEventCallback::class.java.classLoader
        @JvmField val CREATOR: Parcelable.Creator<UserMessageAddedEventCallback> = object : Parcelable.Creator<UserMessageAddedEventCallback> {
            @Suppress("UNCHECKED_CAST")
            override fun createFromParcel(parcel: Parcel): UserMessageAddedEventCallback {
                return UserMessageAddedEventCallback(parcel.readValue(CL) as Message)
            }

            override fun newArray(size: Int): Array<UserMessageAddedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
