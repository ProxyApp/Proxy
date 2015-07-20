package com.shareyourproxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.AddChannelListActivity;
import com.shareyourproxy.app.LoginActivity;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.app.SearchActivity;
import com.shareyourproxy.app.UserProfileActivity;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.Intents.getUserProfileIntent;


/**
 * Utility for launching Activities.
 */
public final class IntentLauncher {

    /**
     * Private constructor.
     */
    private IntentLauncher() {
    }

    /**
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchMainActivity(
        Activity activity, int selectTab, boolean groupDeleted, Group group) {
        Intent intent = new Intent(Intents.ACTION_MAIN_VIEW).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.ARG_SELECTED_MAINFRAGMENT_TAB, selectTab);
        intent.putExtra(Constants.ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, groupDeleted);
        intent.putExtra(Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP, group);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the {@link LoginActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchLoginActivity(Activity activity) {
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
    public static void launchUserProfileActivity(
        Activity activity, User user, String loggedInUserId) {
        Intent intent = getUserProfileIntent(user, loggedInUserId);
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
     * @param group    group data
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

    /**
     * View facebook user page.
     *
     * @param activity context
     * @param userId   user profile ID
     */
    public static void launchFacebookIntent(Activity activity, String userId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String mobileURI = "https://www.facebook.com/app_scoped_user_id/"+userId;
        intent.setData(Uri.parse("https://www.facebook.com/" + userId));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * View facebook help page.
     *
     * @param activity context
     */
    public static void launchFacebookHelpIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http:www.facebook.com/help/211813265517027"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
