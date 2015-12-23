package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.rx.RxMessageSync
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback


/**
 * Created by Evan on 6/18/15.
 */
class GetUserMessagesCommand(private val userId: String) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as String) {
    }

    override fun execute(service: Service): EventCallback {
        return RxMessageSync.getFirebaseMessages(service, userId)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(userId)
    }

    companion object {
        private val CL = GetUserMessagesCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<GetUserMessagesCommand> = object : Parcelable.Creator<GetUserMessagesCommand> {
            override fun createFromParcel(parcel: Parcel): GetUserMessagesCommand {
                return GetUserMessagesCommand(parcel)
            }

            override fun newArray(size: Int): Array<GetUserMessagesCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
