package com.shareyourproxy.util

import java.lang.Character.toTitleCase
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for formatting objects.
 */
object ObjectUtils {

    fun buildFullName(firstName: String, lastName: String): String {
        return StringBuilder(capitalize(firstName)).append(" ").append(capitalize(lastName)).toString().trim { it <= ' ' }
    }

    fun capitalize(string: String?): String {
        if (string == null || string.length == 0) {
            return ""
        }
        val sb = StringBuilder(toTitleCase(string[0]).toString()).append(string.substring(1))
        return sb.toString()
    }

    val twitterDateFormat: SimpleDateFormat
        get() = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.US)
}

