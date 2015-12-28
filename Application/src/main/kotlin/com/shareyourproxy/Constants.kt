package com.shareyourproxy


/**
 * Constant values for activity and fragment arguments, and shared preference keys.
 */
object Constants {
    //Key used to retrieve a saved user from shared preferences.
    const val KEY_LOGGED_IN_USER = "com.shareyourproxy.key_logged_in_user"
    //Bundled extra key for value containing the user profile to be opened with {@link
    //IntentLauncher#launchUserProfileActivity(Activity, User, String)}.
    const val ARG_USER_SELECTED_PROFILE = "com.proxy.user_selected_profile"
    //Bundled extra key used to distinguish a selected group.
    const val ARG_SELECTED_GROUP = "com.proxy.selected_group"
    const val ARG_EDIT_GROUP_TYPE = "com.proxy.edit_group_type"
    const val ARG_LOGGEDIN_USER_ID = "com.shareyourproxy.arg_loggedin_user_id"
    const val ARG_SHOW_TOOLBAR = "com.shareyourproxy.arg_show_profile_toolbar"
    const val ARG_MAINFRAGMENT_SELECTED_TAB = "com.shareyourproxy" + ".arg_mainfragment_selected_tab"
    const val ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED = "com.shareyourproxy" + ".arg_was_group_deleted"
    const val ARG_MAINGROUPFRAGMENT_DELETED_GROUP = "com.shareyourproxy" + ".arg_deleted_group"
    //Query param used in FirebaseAuthenticator and FirebaseInterceptor
    const val QUERY_AUTH = "auth"
    const val KEY_GOOGLE_PLUS_AUTH = "com.shareyourproxy.key_google_plus_auth"
    const val PROVIDER_GOOGLE = "google"
    const val MASTER_KEY = "com.shareyourproxy.application.shared_preferences_key"
    const val KEY_PLAY_INTRODUCTION = "com.shareyourproxy.play_introduction"
    const val KEY_DISMISSED_WHOOPS = "com.shareyourproxy.dismissed_whoops"
    const val KEY_DISMISSED_SAFE_INFO = "com.shareyourproxy.dismissed_safe_info"
    const val KEY_DISMISSED_SHARE_PROFILE = "com.shareyourproxy.dismissed_share_profile"
    const val KEY_DISMISSED_CUSTOM_URL = "com.shareyourproxy.dismissed_custom_url"
    const val KEY_DISMISSED_INVITE_FRIENDS = "com.shareyourproxy.dismissed_invite_friends"
    const val KEY_DISMISSED_PUBLIC_GROUP = "com.shareyourproxy.dismissed_public_group"
    const val KEY_DISMISSED_MAIN_GROUP = "com.shareyourproxy.dismissed_main_group"
}
