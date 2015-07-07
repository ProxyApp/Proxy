package com.shareyourproxy.util;

import android.text.TextUtils;

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
        return String.valueOf(Character.toTitleCase(string.charAt(0))) + string.substring(1);
    }
}
