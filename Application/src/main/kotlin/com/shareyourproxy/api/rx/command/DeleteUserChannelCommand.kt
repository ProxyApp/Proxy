package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserChannelSync.deleteChannel
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback


/**
 * Delete a channel associated with a user.
 */
internal final class DeleteUserChannelCommand(val user: User, val channel: Channel, val position: Int) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as Channel, parcel.readValue(CL) as Int)

    override fun execute(service: Service): EventCallback {
        return deleteChannel(service, user, channel, position)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(channel)
        dest.writeValue(position)
    }

    companion object {
        private val CL = DeleteUserChannelCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<DeleteUserChannelCommand> = object : Parcelable.Creator<DeleteUserChannelCommand> {
            override fun createFromParcel(parcel: Parcel): DeleteUserChannelCommand {
                return DeleteUserChannelCommand(parcel)
            }

            override fun newArray(size: Int): Array<DeleteUserChannelCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
