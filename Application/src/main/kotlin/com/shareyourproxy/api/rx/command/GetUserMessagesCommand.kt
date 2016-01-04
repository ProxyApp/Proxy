package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.rx.RxMessageSync.getFirebaseMessages
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback


/**
 * Get user messages.
 */
class GetUserMessagesCommand(private val userId: String) : BaseCommand() {
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as String)

    override fun execute(service: Service): UserMessagesDownloadedEventCallback {
        return getFirebaseMessages(service, userId)
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
