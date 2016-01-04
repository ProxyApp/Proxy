package com.shareyourproxy

import android.content.ComponentName
import android.content.Intent
import com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.api.domain.model.User

/**
 * Contains constants for launching [Intent]s with [IntentLauncher].
 */
internal object Intents {
    private val clipboardComponentName = ComponentName("com.google.android.apps.docs", "com.google.android.apps.docs.app.SendTextToClipboardActivity")
    internal const val ACTION_DISPATCH = "com.shareyourproxy.intent.action.DISPATCH"
    internal const val ACTION_INTRODUCTION = "com.shareyourproxy.intent.action.INTRODUCTION"
    internal const val ACTION_LOGIN = "com.shareyourproxy.intent.action.LOGIN"
    internal const val ACTION_MAIN_VIEW = "com.shareyourproxy.intent.action.MAIN_VIEW"
    internal const val ACTION_SEARCH_VIEW = "com.shareyourproxy.intent.action.SEARCH"
    internal const val ACTION_ADD_CHANNEL_LIST_VIEW = "com.shareyourproxy.intent.action.ADD_CHANNEL_LIST"
    internal const val ACTION_USER_PROFILE = "com.shareyourproxy.intent.action" + ".USER_PROFILE"
    internal const val ACTION_EDIT_GROUP_CHANNEL = "com.shareyourproxy.intent.action" + ".EDIT_GROUP"
    internal const val ACTION_VIEW_GROUP_USERS = "com.shareyourproxy.intent.action" + ".VIEW_GROUP_USERS"
    internal const val ACTION_VIEW_ABOUT = "com.shareyourproxy.intent.action.VIEW_ABOUT"

    /**
     * Get intent to launch [UserContactActivity].
     * @param user           profile to view
     * @param loggedInUserId logged in user id
     * @return user profile intent
     */
    internal fun getUserProfileIntent(user: User, loggedInUserId: String): Intent {
        val intent = Intent(ACTION_USER_PROFILE)
        intent.putExtra(ARG_USER_SELECTED_PROFILE, user)
        intent.putExtra(ARG_LOGGEDIN_USER_ID, loggedInUserId)
        return intent
    }

    /**
     * Get an Intent for plain text http link.
     * @param message http link
     * @return share link intent
     */
    internal fun getShareLinkIntent(message: String): Intent {
        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.setType("text/plain")
        return sendIntent
    }

    internal fun getClipboardIntent(message: String): Intent {
        val clipboardIntent = Intent()
        clipboardIntent.setComponent(clipboardComponentName)
        clipboardIntent.setAction(Intent.ACTION_SEND)
        clipboardIntent.setType("text/plain")
        clipboardIntent.putExtra(Intent.EXTRA_TEXT, message)
        return clipboardIntent
    }
}
