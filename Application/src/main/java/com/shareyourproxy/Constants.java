package com.shareyourproxy;


import android.app.Activity;

import com.shareyourproxy.api.domain.model.User;

/**
 * Constant Values.
 */
public class Constants {

    /**
     * Bundled extra containing the user profile to be opened with {@link
     * IntentLauncher#launchUserProfileActivity(Activity, User)}.
     */
    public static final String ARG_USER_CREATED_CHANNEL = "com.proxy.user_created_channel";
    public static final String ARG_USER_SELECTED_PROFILE = "com.proxy.user_selected_profile";
    public static final String ARG_USER_LOGGED_IN = "com.proxy.user_is_currently_logged_in";
    public static final String ARG_SELECTED_GROUP = "com.proxy.selected_group";
    public static final String KEY_USER_ID_VALUE = "com.shareyourproxy.key_user_id_value";


    /**
     * Private Constants constructor.
     */
    private Constants() {

    }

}
