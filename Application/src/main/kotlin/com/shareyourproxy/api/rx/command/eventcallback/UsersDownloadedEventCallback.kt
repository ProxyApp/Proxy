package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.User
import java.util.*

/**
 * Created by Evan on 6/9/15.
 */
class UsersDownloadedEventCallback(loggedInUser: User, val users: HashMap<String, User>) : UserEventCallback(loggedInUser) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(users)
    }

    companion object {
        private val CL = UsersDownloadedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UsersDownloadedEventCallback> = object : Parcelable.Creator<UsersDownloadedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UsersDownloadedEventCallback {
                return UsersDownloadedEventCallback(
                        parcel.readValue(CL) as User, parcel.readValue(CL) as HashMap<String, User>)
            }

            override fun newArray(size: Int): Array<UsersDownloadedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
