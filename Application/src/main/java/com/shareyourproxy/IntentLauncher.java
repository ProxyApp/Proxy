package com.shareyourproxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.AddChannelListActivity;
import com.shareyourproxy.app.BaseActivity;
import com.shareyourproxy.app.DispatchActivity;
import com.shareyourproxy.app.LoginActivity;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.UserProfileActivity;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.Constants.ARG_USER_LOGGED_IN;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;


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
        Intent intent = new Intent(Intents.ACTION_DISPATCH).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchMainActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_MAIN_VIEW).addFlags(Intent
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
        Intent intent = new Intent(Intents.ACTION_LOGIN).addFlags(
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
        Intent intent = new Intent(Intents.ACTION_USER_PROFILE);
        intent.putExtra(ARG_USER_SELECTED_PROFILE, user);
        intent.putExtra(ARG_USER_LOGGED_IN, isLoggedInUser);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link SearchActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchSearchActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_SEARCH_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link SearchActivity}.
     *
     * @param activity The context used to start this intent
     * @param group
     */
    public static void launchViewGroupUsersActivity(Activity activity, Group group) {
        Intent intent = new Intent(Intents.ACTION_VIEW_GROUP_USERS);
        intent.putExtra(ARG_SELECTED_GROUP, group);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link AddChannelListActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchChannelListActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_ADD_CHANNEL_LIST_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    public static void launchGroupEditChannelActivity(Activity activity, Group group) {
        Intent intent = new Intent(Intents.ACTION_EDIT_GROUP_CHANNEL);
        intent.putExtra(ARG_SELECTED_GROUP, group);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    public static void launchAboutActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_VIEW_ABOUT);
        activity.startActivity(intent);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ address });
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    public static void launchWebIntent(Activity activity, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http:" + address));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "Invalid link", Toast.LENGTH_LONG).show();
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
