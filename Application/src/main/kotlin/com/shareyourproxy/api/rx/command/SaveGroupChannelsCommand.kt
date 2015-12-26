package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGroupChannelSync
import com.shareyourproxy.api.rx.RxGroupChannelSync.updateGroupChannels
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import java.util.*

/**
 * Save channels associated with a group.
 */
class SaveGroupChannelsCommand(private val user: User, private val newTitle: String, private val group: Group, private val channels: HashSet<String>, private val groupEditType: GroupEditType) : BaseCommand() {
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as String, parcel.readValue(CL) as Group, parcel.readValue(CL) as HashSet<String>, parcel.readValue(CL) as GroupEditType)

    override fun execute(service: Service): EventCallback {
        return updateGroupChannels(service, user, newTitle, group, channels, groupEditType)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(newTitle)
        dest.writeValue(group)
        dest.writeValue(channels)
        dest.writeValue(groupEditType)
    }

    companion object {
        private val CL = SaveGroupChannelsCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<SaveGroupChannelsCommand> = object : Parcelable.Creator<SaveGroupChannelsCommand> {
            override fun createFromParcel(parcel: Parcel): SaveGroupChannelsCommand {
                return SaveGroupChannelsCommand(parcel)
            }

            override fun newArray(size: Int): Array<SaveGroupChannelsCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
