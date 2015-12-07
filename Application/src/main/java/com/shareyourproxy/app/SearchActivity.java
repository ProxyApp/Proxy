package com.shareyourproxy.app;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.shareyourproxy.app.fragment.SearchFragment;

/**
 * Activity to handle displaying contacts and searching for new ones.
 */
public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preventStatusBarFlash(this);
        if (savedInstanceState == null) {
            SearchFragment searchFragment = SearchFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, searchFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }
}
