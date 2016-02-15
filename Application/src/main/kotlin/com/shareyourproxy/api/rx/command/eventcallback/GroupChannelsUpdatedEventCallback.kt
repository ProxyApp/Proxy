package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.util.Enumerations
import java.util.*

/**
 * Group Name may have changed, channels updated.
 */
internal class GroupChannelsUpdatedEventCallback(user: User, val oldGroup: Group, val group: Group, val channels: HashSet<String>, val groupEditType: Enumerations.GroupEditType) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(oldGroup)
        dest.writeValue(group)
        dest.writeValue(channels)
        dest.writeValue(groupEditType)
    }

    companion object {
        private val CL = GroupChannelsUpdatedEventCallback::class.java.classLoader
        @JvmField val CREATOR: Parcelable.Creator<GroupChannelsUpdatedEventCallback> = object : Parcelable.Creator<GroupChannelsUpdatedEventCallback> {
            @Suppress("UNCHECKED_CAST")
            override fun createFromParcel(parcel: Parcel): GroupChannelsUpdatedEventCallback {
                return GroupChannelsUpdatedEventCallback(parcel.readValue(CL) as User, parcel.readValue(CL) as Group, parcel.readValue(CL) as Group, parcel.readValue(CL) as HashSet<String>, parcel.readValue(CL) as Enumerations.GroupEditType)
            }

            override fun newArray(size: Int): Array<GroupChannelsUpdatedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
