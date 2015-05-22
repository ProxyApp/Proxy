package com.shareyourproxy.app;

import android.os.Bundle;

import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.DispatchFragment;

import butterknife.ButterKnife;

/**
 * Created by Evan on 3/23/15.
 */
public class DispatchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_dispatch_container,
                    DispatchFragment.newInstance()).commit();
        }
    }

}
