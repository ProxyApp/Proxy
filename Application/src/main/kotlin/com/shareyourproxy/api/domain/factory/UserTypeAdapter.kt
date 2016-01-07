package com.shareyourproxy.api.domain.factory

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.util.StringUtils.buildFullName

/**
 * The web service actually doesn't return a fullname param, we make/strip it here from the user object.
 */
internal object UserTypeAdapter : TypeAdapter<User>() {
    val gson = Gson()
    val delegate = gson.getAdapter(User::class.java)
    val elementAdapter = gson.getAdapter(JsonElement::class.java)
    override fun write(out: JsonWriter, value: User) {
        val tree = delegate.toJsonTree(value)
        beforeWrite(tree)
        elementAdapter.write(out, tree)
    }

    override fun read(input: JsonReader): User {
        val tree = elementAdapter.read(input)
        afterRead(tree)
        return delegate.fromJsonTree(tree)
    }

    fun beforeWrite(toSerialize: JsonElement) {
        if (toSerialize.isJsonObject) {
            removeFullName(toSerialize)
        } else if (toSerialize.isJsonObject) {
            val custom = toSerialize.asJsonArray
            custom.forEach { removeFullName(it) }
        }
    }

    fun afterRead(deserialized: JsonElement) {
        if (deserialized.isJsonObject) {
            addFullName(deserialized)
        } else if (deserialized.isJsonArray) {
            val users = deserialized.asJsonArray
            for (user in users) {
                afterRead(user)
            }
        }
    }

    private fun removeFullName(toSerialize: JsonElement) {
        val custom = toSerialize.asJsonObject
        custom.remove("fullName")
    }

    private fun addFullName(deserialized: JsonElement) {
        val obj = deserialized.asJsonObject
        val firstName = obj.get("first")
        val lastName = obj.get("last")
        val first = if (firstName == null) "" else firstName.asString
        val last = if (lastName == null) "" else lastName.asString
        obj.add("fullName", JsonPrimitive(buildFullName(first, last)))
    }
}
