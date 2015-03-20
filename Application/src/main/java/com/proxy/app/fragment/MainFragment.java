package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.event.OttoBusDriver;
import com.proxy.widget.SlidingTabLayout;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Main Fragment for the MainActivity.
 */
public class MainFragment extends BaseFragment {

    @InjectView(R.id.fragment_main_viewpager)
    ViewPager mViewPager;
    @InjectView(R.id.fragment_main_sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    private ArrayList<Fragment> mFragmentArray;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OttoBusDriver.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OttoBusDriver.unregister(this);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void setupFragments() {
        if (mFragmentArray == null) {
            mFragmentArray = new ArrayList<>();
            mFragmentArray.add(UserFragment.newInstance());
            mFragmentArray.add(GroupFragment.newInstance());
        }
    }

    private void initialize() {
        setupFragments();
        mViewPager.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager()));
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout.setViewPager(mViewPager);

        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return getActivity().getResources().getColor(R.color.common_text_inverse);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.TRANSPARENT;
            }

        });
        mSlidingTabLayout.setBackgroundColor(getActivity().getResources()
            .getColor(R.color.common_gray));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * The {@link FragmentPagerAdapter} used to display Groups and Users.
     */
    class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        SimpleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the Fragment to be displayed.
         */
        @Override
        public Fragment getItem(int i) {
            return mFragmentArray.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentArray.size();
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p/>
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentArray.get(position).getClass().getSimpleName();
        }

    }

}
