package com.shareyourproxy.util;

import android.app.Activity;
import android.widget.Toast;

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

    public static void showBroToast(Activity activity, String message){
        Toast.makeText(activity, message + " Bro", Toast.LENGTH_SHORT).show();
    }
}
