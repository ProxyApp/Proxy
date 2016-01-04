package com.shareyourproxy.api.domain.factory

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.util.StringUtils.buildFullName

/**
 * The web service actually doesn't return a fullname param, we make/strip it here from the user object.
 */
object UserTypeAdapterFactory : CustomTypeAdapterFactory<User>(User::class.java) {

    override fun beforeWrite(source: User, toSerialize: JsonElement) {
        val custom = toSerialize.asJsonObject
        custom.remove("fullName")
    }

    override fun afterRead(deserialized: JsonElement) {
        if (deserialized.isJsonObject) {
            val obj = deserialized.asJsonObject
            val firstName = obj.get("first")
            val lastName = obj.get("last")
            val first = if (firstName == null) "" else firstName.asString
            val last = if (lastName == null) "" else lastName.asString
            obj.add("fullName", JsonPrimitive(buildFullName(first, last)))
        }
    }
}
