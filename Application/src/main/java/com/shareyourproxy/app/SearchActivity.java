package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.SearchFragment;

import butterknife.ButterKnife;

/**
 * Activity to handle displaying contacts and searching for new ones.
 */
public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_search_container,
                    SearchFragment.newInstance()).commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }
}
