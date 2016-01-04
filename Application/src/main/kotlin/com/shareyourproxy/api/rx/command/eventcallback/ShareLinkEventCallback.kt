package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

/**
 * A ShareLink message has been generated.
 */
internal final class ShareLinkEventCallback(val message: String) : EventCallback() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(message)
    }

    companion object {
        private val CL = ShareLinkEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<ShareLinkEventCallback> = object : Parcelable.Creator<ShareLinkEventCallback> {
            override fun createFromParcel(parcel: Parcel): ShareLinkEventCallback {
                return ShareLinkEventCallback(parcel.readValue(CL) as String)
            }

            override fun newArray(size: Int): Array<ShareLinkEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
