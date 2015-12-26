package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.User

/**
 * User contact has been deleted.
 */
class UserContactDeletedEventCallback(user: User, private val contactId: String) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(contactId)
    }

    companion object {
        private val CL = UserContactDeletedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserContactDeletedEventCallback> = object : Parcelable.Creator<UserContactDeletedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserContactDeletedEventCallback {
                return UserContactDeletedEventCallback(
                        parcel.readValue(CL) as User, parcel.readValue(CL) as String)
            }

            override fun newArray(size: Int): Array<UserContactDeletedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
