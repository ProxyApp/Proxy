package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserGroupSync.addUserGroup
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Add a new user group.
 * @param user  logged in user
 * @param group this events group
 */
internal final class AddUserGroupCommand(val user: User, val group: Group) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as Group)

    override fun execute(service: Service): EventCallback {
        return addUserGroup(service, user, group)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(group)
    }

    companion object {
        private val CL = AddUserGroupCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<AddUserGroupCommand> = object : Parcelable.Creator<AddUserGroupCommand> {
            override fun createFromParcel(parcel: Parcel): AddUserGroupCommand {
                return AddUserGroupCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddUserGroupCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
