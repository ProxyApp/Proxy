package com.proxy;

import android.content.Intent;

/**
 * Contains constants for launching {@link Intent}s with {@link IntentLauncher}.
 */
public class Intents {

    public static final String ACTION_DISPATCH = "com.proxy.intent.action.DISPATCH";
    public static final String ACTION_LOGIN = "com.proxy.intent.action.LOGIN";
    public static final String ACTION_CONTACT_LIST = "com.proxy.intent.action.CONTACT_LIST";

    /**
     * Private constructor.
     */
    private Intents() {
    }
}
