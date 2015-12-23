package com.shareyourproxy


/**
 * Constant values for activity and fragment arguments, and shared preference keys.
 */
object Constants {
    //Key used to retrieve a saved user from shared preferences.
    val KEY_LOGGED_IN_USER = "com.shareyourproxy.key_logged_in_user"
    //Bundled extra key for value containing the user profile to be opened with {@link
    //IntentLauncher#launchUserProfileActivity(Activity, User, String)}.
    val ARG_USER_SELECTED_PROFILE = "com.proxy.user_selected_profile"
    //Bundled extra key used to distinguish a selected group.
    val ARG_SELECTED_GROUP = "com.proxy.selected_group"
    val ARG_EDIT_GROUP_TYPE = "com.proxy.edit_group_type"
    val ARG_LOGGEDIN_USER_ID = "com.shareyourproxy.arg_loggedin_user_id"
    val ARG_SHOW_TOOLBAR = "com.shareyourproxy.arg_show_profile_toolbar"
    val ARG_MAINFRAGMENT_SELECTED_TAB = "com.shareyourproxy" + ".arg_mainfragment_selected_tab"
    val ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED = "com.shareyourproxy" + ".arg_was_group_deleted"
    val ARG_MAINGROUPFRAGMENT_DELETED_GROUP = "com.shareyourproxy" + ".arg_deleted_group"
    //Query param used in FirebaseAuthenticator and FirebaseInterceptor
    val QUERY_AUTH = "auth"
    val KEY_GOOGLE_PLUS_AUTH = "com.shareyourproxy.key_google_plus_auth"
    val PROVIDER_GOOGLE = "google"
    val MASTER_KEY = "com.shareyourproxy.application.shared_preferences_key"
    val KEY_PLAY_INTRODUCTION = "com.shareyourproxy.play_introduction"
    val KEY_DISMISSED_WHOOPS = "com.shareyourproxy.dismissed_whoops"
    val KEY_DISMISSED_SAFE_INFO = "com.shareyourproxy.dismissed_safe_info"
    val KEY_DISMISSED_SHARE_PROFILE = "com.shareyourproxy.dismissed_share_profile"
    val KEY_DISMISSED_CUSTOM_URL = "com.shareyourproxy.dismissed_custom_url"
    val KEY_DISMISSED_INVITE_FRIENDS = "com.shareyourproxy.dismissed_invite_friends"
    val KEY_DISMISSED_PUBLIC_GROUP = "com.shareyourproxy.dismissed_public_group"
    val KEY_DISMISSED_MAIN_GROUP = "com.shareyourproxy.dismissed_main_group"
}
