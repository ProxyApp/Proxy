package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.DispatchFragment;

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the {@link
 * MainActivity} if we have a cached user or send them to {@link LoginActivity} if we need to login
 * and download a current user. Delete cached Realm data on startup.
 */
public class DispatchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_dispatch_container,
                    DispatchFragment.newInstance()).commit();
        }
        deleteRealm();
    }
}
