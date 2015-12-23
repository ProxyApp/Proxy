package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGroupContactSync
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*

/**
 * Created by Evan on 6/8/15.
 */
class SaveGroupContactsCommand(val user: User, val groups: ArrayList<GroupToggle>, val contact: User) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User,
            parcel.readValue(CL) as ArrayList<GroupToggle>, parcel.readValue(CL) as User) {
    }

    override fun execute(service: Service): EventCallback {
        return RxGroupContactSync.updateGroupContacts(service, user, groups, contact)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(groups)
        dest.writeValue(contact)
    }

    companion object {
        private val CL = SaveGroupContactsCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<SaveGroupContactsCommand> = object : Parcelable.Creator<SaveGroupContactsCommand> {
            override fun createFromParcel(parcel: Parcel): SaveGroupContactsCommand {
                return SaveGroupContactsCommand(parcel)
            }

            override fun newArray(size: Int): Array<SaveGroupContactsCommand?> {
                return arrayOfNulls(size)
            }
        }
    }

}
