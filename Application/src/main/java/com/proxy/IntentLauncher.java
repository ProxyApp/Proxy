package com.proxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.proxy.app.LoginActivity;
import com.proxy.app.MainActivity;

import static com.proxy.Preferences.LOGOUT_CLICKED;


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
        Bundle bundle = new Bundle();
        bundle.putBoolean(LOGOUT_CLICKED, logoutClicked);
        Intent intent = new Intent(LocalIntents.ACTION_LOGIN).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }


    /**
     * Launch the {@link MainActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchGmail(Activity activity) {

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
