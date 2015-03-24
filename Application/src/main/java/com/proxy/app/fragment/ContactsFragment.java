package com.proxy.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.app.adapter.ImagePagerAdapter;
import com.proxy.event.OttoBusDriver;
import com.proxy.widget.ContentDescriptionDrawable;
import com.proxy.widget.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.proxy.util.ViewUtils.getLargeIconDimen;
import static com.proxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * {@link Fragment} to handle adding a {@link UserFragment} and {@link GroupFragment} to this {@link
 * ContactsFragment#mSlidingTabLayout}.
 */
public class ContactsFragment extends BaseFragment {

    @InjectView(R.id.fragment_contacts_viewpager)
    ViewPager mViewPager;
    @InjectView(R.id.fragment_contacts_sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    private List<Pair<ContentDescriptionDrawable, Fragment>> mFragmentArray;


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
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Add fragments to the List backing the {@link ContactsFragment#mSlidingTabLayout}.
     */
    private void addTabFragments() {
        if (mFragmentArray == null) {
            mFragmentArray = new ArrayList<>();
            mFragmentArray.add(getFavoritesTab());
            mFragmentArray.add(getGroupsTab());
        }
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link ContactsFragmentPagerAdapter}.
     *
     * @return {@link UserFragment} and Drawable combo
     */
    private Pair<ContentDescriptionDrawable, Fragment> getFavoritesTab() {
        return new Pair<ContentDescriptionDrawable, Fragment>(
            getFavoritesDrawable(), UserFragment.newInstance());
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link ContactsFragmentPagerAdapter}.
     *
     * @return {@link GroupFragment} and Drawable combo
     */
    private Pair<ContentDescriptionDrawable, Fragment> getGroupsTab() {
        return new Pair<ContentDescriptionDrawable, Fragment>(
            getGroupDrawable(), GroupFragment.newInstance());
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getFavoritesDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.star,
            getLargeIconDimen(getActivity()), Color.WHITE).setContentDescription(getString(R
            .string.Favorites));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable}.
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getGroupDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.groups,
            getLargeIconDimen(getActivity()), Color.WHITE).setContentDescription(getString(R
            .string.Groups));
    }

    /**
     * Initialize this fragments data and {@link SlidingTabLayout}.
     */
    private void initialize() {
        addTabFragments();
        mViewPager.setAdapter(new ContactsFragmentPagerAdapter(getChildFragmentManager()));
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
    final class ContactsFragmentPagerAdapter extends ImagePagerAdapter {

        /**
         * Constructor.
         *
         * @param fragmentManager Manager of fragments.
         */
        ContactsFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentArray.get(i).second;
        }

        @Override
        public int getCount() {
            return mFragmentArray.size();
        }

        @Override
        public Drawable getPageImage(int position) {
            return mFragmentArray.get(position).first;
        }

        @Override
        public String getImageDescription(int position) {
            return mFragmentArray.get(position).first.getContentDescription();
        }

    }

}
