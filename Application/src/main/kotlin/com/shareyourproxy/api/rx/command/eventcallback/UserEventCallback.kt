package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import com.shareyourproxy.api.domain.model.User

/**
 * EventCallbacks that have a user update.
 */
abstract class UserEventCallback(val user: User) : EventCallback() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
    }
}
