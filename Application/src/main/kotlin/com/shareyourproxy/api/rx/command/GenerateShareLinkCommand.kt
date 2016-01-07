package com.shareyourproxy.api.rx.command

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxShareLink.getShareLinkMessageObservable
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*


/**
 * Generate public link urls.
 */
internal final class GenerateShareLinkCommand(val user: User, val groups: ArrayList<GroupToggle>) : BaseCommand() {
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as ArrayList<GroupToggle>)

    override fun execute(context: Context): EventCallback {
        return getShareLinkMessageObservable(context, user, groups)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(groups)
    }

    companion object {
        private val CL = GenerateShareLinkCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<GenerateShareLinkCommand> = object : Parcelable.Creator<GenerateShareLinkCommand> {
            override fun createFromParcel(parcel: Parcel): GenerateShareLinkCommand {
                return GenerateShareLinkCommand(parcel)
            }

            override fun newArray(size: Int): Array<GenerateShareLinkCommand?> {
                return arrayOfNulls(size)
            }
        }
    }


}
