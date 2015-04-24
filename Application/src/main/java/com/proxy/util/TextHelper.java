package com.proxy.util;

import android.text.TextUtils;

/**
 * Helper class for formatting text.
 */
public class TextHelper {
    /**
     * Constructor
     */
    private TextHelper() {
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
}
