package com.shareyourproxy;


import android.app.Activity;

import com.shareyourproxy.api.domain.model.User;

/**
 * Constant values for activity and layouts.fragment arguments, and shared preference keys.
 */
public class Constants {

    /**
     * Bundled extra containing the user profile to be opened with {@link
     * IntentLauncher#launchUserProfileActivity(Activity, User, String)}.
     */
    public static final String ARG_USER_SELECTED_PROFILE = "com.proxy.user_selected_profile";
    public static final String ARG_SELECTED_GROUP = "com.proxy.selected_group";
    public static final String ARG_ADD_OR_EDIT = "com.proxy.add_or_edit";
    public static final String KEY_LOGGED_IN_USER = "com.shareyourproxy.key_logged_in_user";
    public static final String ARG_LOGGEDIN_USER_ID = "com.shareyourproxy.arg_logged_in_user_id";
    public static final String ARG_SELECTED_MAINFRAGMENT_TAB = "com.shareyourproxy" +
        ".arg_selected_mainactivity_tab";
    public static final String ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED = "com.shareyourproxy" +
        ".arg_was_group_deleted";
    public static final String ARG_MAINGROUPFRAGMENT_DELETED_GROUP = "com.shareyourproxy" +
        ".arg_deleted_group";

    /**
     * Private Constants constructor.
     */
    private Constants() {

    }
}
