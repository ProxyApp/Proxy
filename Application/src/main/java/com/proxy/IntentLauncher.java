package com.proxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.proxy.app.LoginActivity;
import com.proxy.app.MainActivity;


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
     * Launch the {@link MainActivity}.
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
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchSearch(Activity activity) {
        Intent intent = new Intent(LocalIntents.ACTION_SEARCH_VIEW);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /**
     * Launch the Dialer App.
     *
     * @param activity    context
     * @param phoneNumber to dial
     */
    public static void dialPhoneNumber(Activity activity, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
