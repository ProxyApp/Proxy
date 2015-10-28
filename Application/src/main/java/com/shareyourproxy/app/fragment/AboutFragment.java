package com.shareyourproxy.app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.app.AboutActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Show an Apache II License for this project.
 */
public class AboutFragment extends BaseFragment {
    @Bind(R.id.fragment_about_toolbar)
    Toolbar toolbar;

    /**
     * Return a new instance of this fragment for the parent {@link AboutActivity}.
     *
     * @return AboutFragment
     */
    public static Fragment newInstance() {
        return new AboutFragment();
    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, null, false);
        ButterKnife.bind(this, rootView);
        buildToolbar(toolbar, getString(R.string.about), null);
        return rootView;
    }

}
