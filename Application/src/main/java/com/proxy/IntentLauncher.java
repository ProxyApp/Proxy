package com.proxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.proxy.app.ContactsActivity;
import com.proxy.app.LoginActivity;

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
     * Launch the {@link ContactsActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchDispatchActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_DISPATCH).addFlags(Intent
            .FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Launch the {@link ContactsActivity}.
     *
     * @param activity The context used to start this intent
     */
    public static void launchContentActivity(Activity activity) {
        Intent intent = new Intent(Intents.ACTION_CONTACT_LIST).addFlags(Intent
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
        Intent intent = new Intent(Intents.ACTION_LOGIN).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

}
