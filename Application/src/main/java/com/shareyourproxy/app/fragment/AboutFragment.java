package com.shareyourproxy.app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Evan on 6/16/15.
 */
public class AboutFragment extends BaseFragment {
    @InjectView(R.id.fragment_about_toolbar)
    protected Toolbar toolbar;


    public static Fragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, null, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    private void initialize() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_about);
    }
}
