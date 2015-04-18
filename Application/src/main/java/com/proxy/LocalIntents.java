package com.proxy;

import android.content.Intent;

/**
 * Contains constants for launching {@link Intent}s with {@link IntentLauncher}.
 */
public class LocalIntents {

    public static final String ACTION_DISPATCH = "com.proxy.intent.action.DISPATCH";
    public static final String ACTION_LOGIN = "com.proxy.intent.action.LOGIN";
    public static final String ACTION_MAIN_VIEW = "com.proxy.intent.action.MAIN_VIEW";
    public static final String ACTION_USER_PROFILE = "com.proxy.intent.action.USER_PROFILE";

    /**
     * Private constructor.
     */
    private LocalIntents() {
    }
}
