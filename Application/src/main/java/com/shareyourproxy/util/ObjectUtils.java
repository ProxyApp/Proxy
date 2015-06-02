package com.shareyourproxy.util;

import android.text.TextUtils;

/**
 * Helper class for formatting text.
 */
public class ObjectUtils {
    /**
     * Constructor
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
        int stringLength;
        if (string == null || (stringLength = string.length()) == 0) {
            return string;
        }
        return new StringBuffer(stringLength)
            .append(Character.toTitleCase(string.charAt(0)))
            .append(string.substring(1))
            .toString();
    }
}
