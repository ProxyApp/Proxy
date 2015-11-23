package com.shareyourproxy;

import android.content.ComponentName;
import android.content.Intent;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.UserContactActivity;

import static com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;

/**
 * Contains constants for launching {@link Intent}s with {@link IntentLauncher}.
 */
public class Intents {

    public static final String ACTION_DISPATCH = "com.shareyourproxy.intent.action.DISPATCH";
    public static final String ACTION_INTRODUCTION =
        "com.shareyourproxy.intent.action.INTRODUCTION";
    public static final String ACTION_LOGIN = "com.shareyourproxy.intent.action.LOGIN";
    public static final String ACTION_MAIN_VIEW = "com.shareyourproxy.intent.action.MAIN_VIEW";
    public static final String ACTION_SEARCH_VIEW = "com.shareyourproxy.intent.action.SEARCH";
    public static final String ACTION_ADD_CHANNEL_LIST_VIEW =
        "com.shareyourproxy.intent.action.ADD_CHANNEL_LIST";
    public static final String ACTION_USER_PROFILE = "com.shareyourproxy.intent.action" +
        ".USER_PROFILE";
    public static final String ACTION_EDIT_GROUP_CHANNEL = "com.shareyourproxy.intent.action" +
        ".EDIT_GROUP";
    public static final String ACTION_VIEW_GROUP_USERS = "com.shareyourproxy.intent.action" +
        ".VIEW_GROUP_USERS";
    public static final String ACTION_VIEW_ABOUT = "com.shareyourproxy.intent.action.VIEW_ABOUT";

    /**
     * Private constructor.
     */
    private Intents() {
    }

    /**
     * Get intent to launch {@link UserContactActivity}.
     *
     * @param user           profile to view
     * @param loggedInUserId logged in user id
     * @return user profile intent
     */
    public static Intent getUserProfileIntent(User user, String loggedInUserId) {
        Intent intent = new Intent(ACTION_USER_PROFILE);
        intent.putExtra(ARG_USER_SELECTED_PROFILE, user);
        intent.putExtra(ARG_LOGGEDIN_USER_ID, loggedInUserId);
        return intent;
    }

    /**
     * Get an Intent for plain text http link.
     *
     * @param message http link
     * @return share link intent
     */
    public static Intent getShareLinkIntent(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    public static Intent getClipboardIntent(String message) {
        Intent clipboardIntent = new Intent();
        clipboardIntent.setComponent(new ComponentName("com.google.android.apps.docs",
            "com.google.android.apps.docs.app.SendTextToClipboardActivity"));
        clipboardIntent.setAction(Intent.ACTION_SEND);
        clipboardIntent.setType("text/plain");
        clipboardIntent.putExtra(Intent.EXTRA_TEXT, message);
        return clipboardIntent;
    }

}
