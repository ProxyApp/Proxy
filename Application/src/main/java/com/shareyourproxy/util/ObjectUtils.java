package com.shareyourproxy.util;

import android.text.TextUtils;

import static java.lang.Character.toTitleCase;
import static java.lang.String.valueOf;

/**
 * Helper class for formatting objects.
 */
public class ObjectUtils {
    /**
     * Private Constructor.
     */
    private ObjectUtils() {
        super();
    }

    /**
     * Join the tokens with a space.
     *
     * @param tokens to join together
     * @return spaced string of tokens
     */
    public static String joinWithSpace(Object[] tokens) {
        return TextUtils.join(" ", tokens);
    }

    /**
     * Compare two ints.
     *
     * @param lhs left item
     * @param rhs right item
     * @return left right or equal
     */
    public static int compare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valueOf(toTitleCase(string.charAt(0))))
            .append(string.substring(1));
        return sb.toString();
    }

    /**
     * Get a formatted TAG string.
     *
     * @param klass the class to copy a TAG for
     * @return return the TAG String
     */
    public static String getSimpleName(Class klass) {
        return klass.getSimpleName();
    }
}
