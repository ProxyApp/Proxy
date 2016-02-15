package com.shareyourproxy.api.rx.command

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserChannelSync.saveUserChannel
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Add a channel.
 */
internal final class AddUserChannelCommand(val user: User, val newChannel: Channel, val oldChannel: Channel) : BaseCommand() {

    constructor(user: User, newChannel: Channel) : this(user, newChannel, Channel())

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as Channel, parcel.readValue(CL) as Channel)

    override fun execute(context: Context): EventCallback {
        return saveUserChannel(context, user, oldChannel, newChannel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(newChannel)
        dest.writeValue(oldChannel)
    }

    companion object {
        private val CL = AddUserChannelCommand::class.java.classLoader
        @JvmField val CREATOR: Parcelable.Creator<AddUserChannelCommand> = object : Parcelable.Creator<AddUserChannelCommand> {
            override fun createFromParcel(parcel: Parcel): AddUserChannelCommand {
                return AddUserChannelCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddUserChannelCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
