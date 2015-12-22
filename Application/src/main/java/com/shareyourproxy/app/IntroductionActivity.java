package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.app.fragment.MainIntroductionFragment;

/**
 * Introduce a user with a view pager flow.
 */
public class IntroductionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            MainIntroductionFragment mainFragment = MainIntroductionFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mainFragment)
                .commit();
        }
    }
}
