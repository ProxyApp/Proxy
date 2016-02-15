package com.shareyourproxy.api.rx.command

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.factory.ChannelFactory.createPublicChannel
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGroupChannelSync
import com.shareyourproxy.api.rx.RxUserChannelSync.saveUserChannel
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*

/**
 * Update users groups channel.
 * @param user    Logged in user
 * @param groups  selected groups to add channel to
 * @param channel this events group
 */
internal final class AddGroupChannelAndPublicCommand(val user: User, val groups: ArrayList<GroupToggle>, val channel: Channel) : BaseCommand() {
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as ArrayList<GroupToggle>, parcel.readValue(CL) as Channel)

    override fun execute(context: Context): EventCallback {
        val publicChannel = createPublicChannel(channel, true)
        val updatedUser = RxGroupChannelSync.addUserGroupsChannel(context, user, groups, publicChannel)
        return saveUserChannel(context, updatedUser.user, channel, publicChannel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(groups)
        dest.writeValue(channel)
    }

    companion object {
        private val CL = AddGroupChannelAndPublicCommand::class.java.classLoader
        @JvmField val CREATOR: Parcelable.Creator<AddGroupChannelAndPublicCommand> = object : Parcelable.Creator<AddGroupChannelAndPublicCommand> {
            override fun createFromParcel(parcel: Parcel): AddGroupChannelAndPublicCommand {
                return AddGroupChannelAndPublicCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddGroupChannelAndPublicCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
