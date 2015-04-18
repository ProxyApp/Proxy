package com.proxy.app;

import android.os.Bundle;

import com.proxy.R;
import com.proxy.api.model.User;
import com.proxy.app.fragment.UserProfileFragment;
import com.proxy.event.OttoBusDriver;

import butterknife.ButterKnife;

/**
 * Activity that handles displaying a {@link User} profile.
 */
public class UserProfileActivity extends BaseActivity {


    @Override
    protected void onResume() {
        super.onResume();
        OttoBusDriver.register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_user_profile_container,
                    UserProfileFragment.newInstance()).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        OttoBusDriver.unregister(this);
    }

}
