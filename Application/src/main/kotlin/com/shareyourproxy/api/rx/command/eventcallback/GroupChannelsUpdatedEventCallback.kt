package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import java.util.*

/**
 * Group Name may have changed, channels updated.
 */
class GroupChannelsUpdatedEventCallback(user: User, val oldGroup: Group, val group: Group, val channels: HashSet<String>, val groupEditType: GroupEditType) : UserEventCallback(user) {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(oldGroup)
        dest.writeValue(group)
        dest.writeValue(channels)
        dest.writeValue(groupEditType)
    }

    companion object {
        private val CL = GroupChannelsUpdatedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<GroupChannelsUpdatedEventCallback> = object : Parcelable.Creator<GroupChannelsUpdatedEventCallback> {
            override fun createFromParcel(parcel: Parcel): GroupChannelsUpdatedEventCallback {
                return GroupChannelsUpdatedEventCallback(parcel.readValue(CL) as User, parcel.readValue(CL) as Group, parcel.readValue(CL) as Group, parcel.readValue(CL) as HashSet<String>, parcel.readValue(CL) as GroupEditType)
            }

            override fun newArray(size: Int): Array<GroupChannelsUpdatedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
