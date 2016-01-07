package com.shareyourproxy.api.rx.command

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserSync.syncAllContacts
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Sync All Users data from firebase to Realm and return the logged in User.
 */
internal final class SyncContactsCommand(val user: User) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User)

    override fun execute(context: Context): EventCallback {
        return syncAllContacts(context, user)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
    }

    companion object {
        private val CL = SyncContactsCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<SyncContactsCommand> = object : Parcelable.Creator<SyncContactsCommand> {
            override fun createFromParcel(parcel: Parcel): SyncContactsCommand {
                return SyncContactsCommand(parcel)
            }

            override fun newArray(size: Int): Array<SyncContactsCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
