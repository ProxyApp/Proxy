package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserGroupSync.deleteUserGroup
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback


/**
 * Delete a group associated with a User.
 * @param user  logged in user
 * @param group this events group
 */
internal final class DeleteUserGroupCommand(val user: User, val group: Group) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as Group)

    override fun execute(service: Service): EventCallback {
        return deleteUserGroup(service, user, group)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(group)
    }

    companion object {
        private val CL = DeleteUserGroupCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<DeleteUserGroupCommand> = object : Parcelable.Creator<DeleteUserGroupCommand> {
            override fun createFromParcel(parcel: Parcel): DeleteUserGroupCommand {
                return DeleteUserGroupCommand(parcel)
            }

            override fun newArray(size: Int): Array<DeleteUserGroupCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
