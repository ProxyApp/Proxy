package com.shareyourproxy;

import android.content.Intent;

/**
 * Contains constants for launching {@link Intent}s with {@link IntentLauncher}.
 */
public class Intents {

    public static final String ACTION_DISPATCH = "com.proxy.intent.action.DISPATCH";
    public static final String ACTION_LOGIN = "com.proxy.intent.action.LOGIN";
    public static final String ACTION_MAIN_VIEW = "com.proxy.intent.action.MAIN_VIEW";
    public static final String ACTION_SEARCH_VIEW = "com.proxy.intent.action.SEARCH";
    public static final String ACTION_ADD_CHANNEL_LIST_VIEW =
        "com.proxy.intent.action.ADD_CHANNEL_LIST";
    public static final String ACTION_USER_PROFILE = "com.proxy.intent.action.USER_PROFILE";
    public static final String ACTION_EDIT_GROUP = "com.proxy.intent.action.EDIT_GROUP";
    public static final String ACTION_VIEW_GROUP_USERS = "com.proxy.intent.action.VIEW_GROUP_USERS";

    /**
     * Private constructor.
     */
    private Intents() {
    }
}
