package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.User

/**
 * User contact has been added.
 */
internal final class UserContactAddedEventCallback(user: User, val contactId: String) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(contactId)
    }

    companion object {
        private val CL = UserContactAddedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserContactAddedEventCallback> = object : Parcelable.Creator<UserContactAddedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserContactAddedEventCallback {
                return UserContactAddedEventCallback(
                        parcel.readValue(CL) as User, parcel.readValue(CL) as String)
            }

            override fun newArray(size: Int): Array<UserContactAddedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
