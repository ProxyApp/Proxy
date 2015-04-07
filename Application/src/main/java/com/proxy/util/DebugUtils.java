package com.proxy.util;

/**
 * Utilities for Debug Builds.
 */
public class DebugUtils {

    /**
     * Private Constructor.
     */
    private DebugUtils() {

    }

    /**
     * Get a formatted TAG string.
     *
     * @param klass the class to create a TAG for
     * @return return the TAG String
     */
    public static String getSimpleName(Class klass) {
        return klass.getSimpleName();
    }

}
