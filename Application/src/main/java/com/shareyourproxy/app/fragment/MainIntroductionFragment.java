package com.shareyourproxy.app.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.shareyourproxy.Constants.KEY_PLAYED_INTRODUCTION;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static java.util.Arrays.asList;

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
    private IntroductionFragmentPagerAdapter _adapter;

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
            getSharedPreferences().edit().putBoolean(KEY_PLAYED_INTRODUCTION, true).apply();
            launchMainActivity(getActivity(), MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
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
     * Add fragments to the List backing the {@link MainFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
        List<BaseIntroductionFragment> fragmentArray = asList(
            FirstIntroductionFragment.newInstance(),
            SecondIntroductionFragment.newInstance(),
            ThirdIntroductionFragment.newInstance());
        _adapter =
            IntroductionFragmentPagerAdapter.newInstance(fragmentArray, getChildFragmentManager());
        viewPager.setAdapter(_adapter);
        viewPager.addOnPageChangeListener(getFabDrawableListener());
        pageIndicator.setViewPager(viewPager);
        drawNextButton();
    }

    private ViewPager.OnPageChangeListener getFabDrawableListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                int position, float positionOffset, int
                positionOffsetPixels) {

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

    private void drawDoneButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(getActivity(), R.raw.ic_done,
            marginSVGLarge, colorWhite));
        floatingActionButton.setBackgroundTintList(colorSelected);
        ViewCompat.setAlpha(floatingActionButton, 1f);
    }

    private void drawNextButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(getActivity(),
            R.raw.ic_chevron_right, marginSVGLarge, colorWhite));
        floatingActionButton.setBackgroundTintList(colorTransparent);
        ViewCompat.setAlpha(floatingActionButton, .3f);
    }

    /**
     * The {@link FragmentPagerAdapter} used to display Groups and Users.
     */
    private static class IntroductionFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<BaseIntroductionFragment> _fragmentArray;

        /**
         * Constructor.
         *
         * @param fragmentManager Manager of fragments.
         */
        private IntroductionFragmentPagerAdapter(
            List<BaseIntroductionFragment> fragmentArray, FragmentManager fragmentManager) {
            super(fragmentManager);
            _fragmentArray = fragmentArray;
        }

        public static IntroductionFragmentPagerAdapter newInstance(
            List<BaseIntroductionFragment> fragmentArray, FragmentManager fragmentManager) {
            return new IntroductionFragmentPagerAdapter(fragmentArray, fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            return _fragmentArray.get(i);
        }

        @Override
        public int getCount() {
            return _fragmentArray.size();
        }

    }
}
