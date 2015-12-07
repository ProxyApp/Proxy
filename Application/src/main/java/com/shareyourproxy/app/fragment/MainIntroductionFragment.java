package com.shareyourproxy.app.fragment;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.app.fragment.AggregateFeedFragment.ARG_SELECT_PROFILE_TAB;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Created by Evan on 9/21/15.
 */
public class MainIntroductionFragment extends BaseFragment {
    @Bind(R.id.fragment_introduction_main_viewpager)
    ViewPager viewPager;
    @Bind(R.id.fragment_introduction_main_pageindicator)
    CirclePageIndicator pageIndicator;
    @Bind(R.id.fragment_introduction_main_fab)
    FloatingActionButton floatingActionButton;
    @BindColor(android.R.color.black)
    ColorStateList colorTransparent;
    @BindColor(R.color.common_proxy_zoidberg)
    ColorStateList colorSelected;
    @BindColor(android.R.color.white)
    int colorWhite;
    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    private int _selectedPage = 0;
    private BasePagerAdapter _adapter;

    /**
     * Default Constructor.
     */
    public MainIntroductionFragment() {
    }

    public static MainIntroductionFragment newInstance() {
        return new MainIntroductionFragment();
    }

    @OnClick(R.id.fragment_introduction_main_fab)
    public void onClickFab() {
        if (_selectedPage == (_adapter.getCount() - 1)) {
            launchMainActivity(getActivity(), ARG_SELECT_PROFILE_TAB, false, null);
            getSharedPreferences().edit().putBoolean(KEY_PLAY_INTRODUCTION, false).commit();
            getActivity().finish();
        } else {
            viewPager.setCurrentItem(++_selectedPage, true);
        }
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_introduction_main, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    private void initialize() {
        initializeFragments();
    }

    /**
     * Add fragments to the List backing the {@link AggregateFeedFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
        List<BaseFragment> fragmentArray = Arrays.<BaseFragment>asList(
            FirstIntroductionFragment.newInstance(),
            SecondIntroductionFragment.newInstance(),
            ThirdIntroductionFragment.newInstance());

        _adapter =
            BasePagerAdapter.newInstance(fragmentArray, getChildFragmentManager());
        viewPager.setAdapter(_adapter);
        viewPager.addOnPageChangeListener(getFabDrawableListener());
        pageIndicator.setViewPager(viewPager);
        drawNextButton();
    }

    private ViewPager.OnPageChangeListener getFabDrawableListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                _selectedPage = position;
                if (position == (_adapter.getCount() - 1)) {
                    drawDoneButton();
                } else {
                    drawNextButton();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    @TargetApi(LOLLIPOP)
    private void drawDoneButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(getActivity(), R.raw.ic_done,
            marginSVGLarge, colorWhite));
        ViewCompat.setAlpha(floatingActionButton, 1f);
        if (SDK_INT >= LOLLIPOP) {
            floatingActionButton.setBackgroundTintList(colorSelected);
        }
    }

    @TargetApi(LOLLIPOP)
    private void drawNextButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(getActivity(),
            R.raw.ic_chevron_right, marginSVGLarge, colorWhite));
        ViewCompat.setAlpha(floatingActionButton, .3f);
        if (SDK_INT >= LOLLIPOP) {
            floatingActionButton.setBackgroundTintList(colorTransparent);
        }
    }
}
