package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User

/**
 * User group channel updated.
 * @param group this events group
 */
class UserGroupAddedEventCallback(user: User, val group: Group) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(group)
    }

    companion object {
        private val CL = UserGroupAddedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserGroupAddedEventCallback> = object : Parcelable.Creator<UserGroupAddedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserGroupAddedEventCallback {
                return UserGroupAddedEventCallback(
                        parcel.readValue(CL) as User, parcel.readValue(CL) as Group)
            }

            override fun newArray(size: Int): Array<UserGroupAddedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
