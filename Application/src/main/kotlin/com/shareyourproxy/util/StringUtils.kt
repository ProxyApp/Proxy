package com.shareyourproxy.util

import java.lang.Character.toTitleCase

/**
 * Helper class for formatting objects.
 */
internal object StringUtils {

    fun buildFullName(firstName: String, lastName: String): String {
        return StringBuilder(capitalize(firstName)).append(" ").append(capitalize(lastName)).toString().trim { it -> it <= ' ' }
    }

    fun capitalize(string: String): String {
        if (string.length == 0) {
            return ""

        }
        val sb = StringBuilder(toTitleCase(string[0]).toString()).append(string.substring(1))
        return sb.toString()
    }
}

