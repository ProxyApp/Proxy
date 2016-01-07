package com.shareyourproxy.api.rx.command

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserSync
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback


/**
 * Add a user to firebase.
 */
internal final class AddUserCommand(val user: User) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User)

    override fun execute(context: Context): EventCallback {
        return RxUserSync.saveUser(context, user)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
    }

    companion object {
        val CREATOR: Parcelable.Creator<AddUserCommand> = object : Parcelable.Creator<AddUserCommand> {
            override fun createFromParcel(parcel: Parcel): AddUserCommand {
                return AddUserCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddUserCommand?> {
                return arrayOfNulls(size)
            }
        }

        private val CL = AddUserCommand::class.java.classLoader
    }

}
