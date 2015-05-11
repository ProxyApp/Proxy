package com.proxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;
import com.proxy.app.BaseActivity;
import com.proxy.app.ChannelListActivity;
import com.proxy.app.DispatchActivity;
import com.proxy.app.LoginActivity;
import com.proxy.app.MainActivity;
import com.proxy.app.SearchActivity;
import com.proxy.app.UserProfileActivity;

import static com.proxy.Constants.ARG_SELECTED_GROUP;
import static com.proxy.Constants.ARG_USER_LOGGED_IN;
import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;


/**
 * Utility for launching Activities.
 */
@SuppressWarnings("unused")
public final class IntentLauncher {

    /**
     * Private constructor.
     */
    private IntentLauncher() {
    }

    /**
     * Launch the {@link DispatchActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchDispatchActivity(Activity activity) {
        Intent intent = new Intent(LocalIntents.ACTION_DISPATCH).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchMainActivity(Activity activity) {
        Intent intent = new Intent(LocalIntents.ACTION_MAIN_VIEW).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link LoginActivity}.
     *
     * @param activity      The context used to start this intent
     * @param logoutClicked boolean value to communicate if the use is logging out
     */
    public static void launchLoginActivity(Activity activity, boolean logoutClicked) {
        Intent intent = new Intent(LocalIntents.ACTION_LOGIN).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link UserProfileActivity}.
     *
     * @param activity The context used to start this intent
     * @param user     that was selected
     */
    public static void launchUserProfileActivity(Activity activity, User user) {
        Bundle bundle = new Bundle();
        User loggedInUser = ((BaseActivity) activity).getLoggedInUser();
        boolean isLoggedInUser = ((BaseActivity) activity).isLoggedInUser(user);
        Intent intent = new Intent(LocalIntents.ACTION_USER_PROFILE);
        intent.putExtra(ARG_USER_SELECTED_PROFILE, user);
        intent.putExtra(ARG_USER_LOGGED_IN, isLoggedInUser);
        activity.startActivityForResult(intent, 0);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link SearchActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchSearchActivity(Activity activity) {
        Intent intent = new Intent(LocalIntents.ACTION_SEARCH_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link ChannelListActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchChannelListActivity(Activity activity) {
        Intent intent = new Intent(LocalIntents.ACTION_ADD_CHANNEL_LIST_VIEW);
        activity.startActivityForResult(intent, 0);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    public static void launchEditGroupActivity(Activity activity, Group group) {
        Intent intent = new Intent(LocalIntents.ACTION_EDIT_GROUP);
        intent.putExtra(ARG_SELECTED_GROUP, group);
        activity.startActivityForResult(intent, 0);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the Dialer App.
     *
     * @param activity    context
     * @param phoneNumber to dial
     */
    public static void launchPhoneIntent(Activity activity, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Launch Email Intent.
     *
     * @param activity context
     * @param address  to send to
     */
    public static void launchEmailIntent(Activity activity, String address) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ address });
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * Send SMS to phone number.
     *
     * @param activity    context
     * @param phoneNumber to sms
     */
    public static void launchSMSIntent(Activity activity, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
