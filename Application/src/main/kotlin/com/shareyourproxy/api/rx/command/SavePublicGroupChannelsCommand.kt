package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.ChannelToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGroupChannelSync
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*


/**
 * Created by Evan on 10/1/15.
 */
class SavePublicGroupChannelsCommand(private val user: User, private val channels: ArrayList<ChannelToggle>) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as ArrayList<ChannelToggle>) {
    }

    override fun execute(service: Service): EventCallback {
        return RxGroupChannelSync.updatePublicGroupChannels(service, user, channels)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(channels)
    }

    companion object {
        val CREATOR: Parcelable.Creator<SavePublicGroupChannelsCommand> = object : Parcelable.Creator<SavePublicGroupChannelsCommand> {
            override fun createFromParcel(parcel: Parcel): SavePublicGroupChannelsCommand {
                return SavePublicGroupChannelsCommand(parcel)
            }

            override fun newArray(size: Int): Array<SavePublicGroupChannelsCommand?> {
                return arrayOfNulls(size)
            }
        }
        private val CL = SavePublicGroupChannelsCommand::class.java.classLoader
    }
}
