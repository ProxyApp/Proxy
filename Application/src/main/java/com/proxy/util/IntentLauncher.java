package com.proxy.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.proxy.app.LoginActivity;
import com.proxy.app.MainActivity;

import static com.proxy.util.Intents.ACTION_LOGIN;
import static com.proxy.util.Intents.ACTION_BASE_ACTIVITY;


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
     * @param context The context used to start this intent
     */
    @SuppressWarnings("unused")
    public static void launchBaseActivity(Activity activity) {
        Intent intent = new Intent(ACTION_BASE_ACTIVITY).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link LoginActivity}.
     *
     * @param context The context used to start this intent
     */
    @SuppressWarnings("unused")
    public static void launchLoginActivity(Activity activity, boolean logoutClicked) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(LoginActivity.LOGOUT_CLICKED, logoutClicked);
        Intent intent = new Intent(ACTION_LOGIN).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

}
