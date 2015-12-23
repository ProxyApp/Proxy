package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Used to signify if a [Channel] is in a [Group] or not.
 */
class ChannelToggle(var channel: Channel, var inGroup: Boolean) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(channel)
        dest.writeValue(inGroup)
    }

    companion object {
        val CL = ChannelToggle::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<ChannelToggle> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<ChannelToggle?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = ChannelToggle(parcel.readParcelable(CL), parcel.readValue(CL) as Boolean)
    }
}
