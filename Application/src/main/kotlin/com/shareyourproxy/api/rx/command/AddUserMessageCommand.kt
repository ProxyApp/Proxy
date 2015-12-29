package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Message
import com.shareyourproxy.api.rx.RxMessageSync.saveFirebaseMessage
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Add a user message to firebase.
 * @param message to send
 */
class AddUserMessageCommand(val userId: String, val message: Message) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as String, parcel.readValue(CL) as Message)

    override fun execute(service: Service): EventCallback {
        return saveFirebaseMessage(service, userId, message)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(userId)
        dest.writeValue(message)
    }

    companion object {
        private val CL = AddUserMessageCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<AddUserMessageCommand> = object : Parcelable.Creator<AddUserMessageCommand> {
            override fun createFromParcel(parcel: Parcel): AddUserMessageCommand {
                return AddUserMessageCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddUserMessageCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
