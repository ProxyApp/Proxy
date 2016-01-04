package com.shareyourproxy.api.rx.command.eventcallback


import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User

/**
 * A user group has been deleted.
 * @param group this events group
 */
internal final class UserGroupDeletedEventCallback(user: User, val group: Group) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(group)
    }

    companion object {
        private val CL = UserGroupDeletedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserGroupDeletedEventCallback> = object : Parcelable.Creator<UserGroupDeletedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserGroupDeletedEventCallback {
                return UserGroupDeletedEventCallback(
                        parcel.readValue(CL) as User, parcel.readValue(CL) as Group)
            }

            override fun newArray(size: Int): Array<UserGroupDeletedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
