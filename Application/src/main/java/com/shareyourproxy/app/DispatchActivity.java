package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.DispatchFragment;

/**
 * Activity to check if we have a cached user in SharedPreferences.
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
