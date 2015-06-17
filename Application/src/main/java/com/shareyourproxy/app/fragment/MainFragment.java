package com.shareyourproxy.app.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.util.ViewUtils;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;

import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * {@link Fragment} to handle adding a {@link MainContactsFragment} and {@link MainGroupFragment} to
 * this {@link MainFragment#slidingTabLayout}.
 */
public class MainFragment extends BaseFragment {

    @Bind(R.id.include_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.activity_main_drawer_layout)
    protected DrawerLayout drawerLayout;
    @Bind(R.id.fragment_main_viewpager)
    protected ViewPager viewPager;
    @Bind(R.id.fragment_main_sliding_tabs)
    protected TabLayout slidingTabLayout;
    private List<Fragment> _fragmentArray;
    private int _selectedColor;
    private int _unselectedColor;
    public static final int ARG_SELECT_CONTACTS_TAB = 0;
    public static final int ARG_SELECT_GROUP_TAB = 1;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments data and {@link TabLayout}.
     */
    private void initialize() {
        initializeResources();
        initializeToolbar();
        initializeFragments();
        initializeTabs();
        initializeDrawer();
    }

    private void initializeResources() {
        Resources res = getResources();
        _unselectedColor = res.getColor(R.color.common_proxy_dark_disabled);
        _selectedColor = res.getColor(R.color.common_proxy_dark_selected);
    }

    private void initializeToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
    }

    private void initializeTabs() {
        ContentDescriptionDrawable userDrawable = getUserDrawable();
        String userDescription = userDrawable.getContentDescription();
        ContentDescriptionDrawable groupDrawable = getGroupDrawable();
        String groupDescription = groupDrawable.getContentDescription();

        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setIcon(userDrawable)
                .setContentDescription(userDescription));
        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setIcon(getGroupDrawable())
                .setContentDescription(groupDescription));

        slidingTabLayout.setTabMode(TabLayout.MODE_FIXED);
        slidingTabLayout.setOnTabSelectedListener(getOnTabSelectedListener());
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(slidingTabLayout));
        //set the selected tab
        slidingTabLayout.getTabAt(getActivity().getIntent().getExtras()
            .getInt(Constants.ARG_SELECTED_MAINFRAGMENT_TAB)).select();
    }

    private OnTabSelectedListener getOnTabSelectedListener() {
        return new OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                ViewUtils.tintDrawableCompat(tab.getIcon(), _selectedColor);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ViewUtils.tintDrawableCompat(tab.getIcon(), _unselectedColor);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

    /**
     * Initialize this activity's drawer view.
     */
    private void initializeDrawer() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(),
            drawerLayout,
            toolbar, R.string.common_open, R.string.common_closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        ViewCompat.setElevation(drawerLayout, getResources().getDimension(R.dimen
            .common_drawer_elevation));
    }

    /**
     * Add fragments to the List backing the {@link MainFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
        _fragmentArray = new ArrayList<>();
        _fragmentArray.add(getFavoritesTab());
        _fragmentArray.add(getGroupsTab());
        viewPager.setAdapter(new ContactsFragmentPagerAdapter(getChildFragmentManager()));
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link ContactsFragmentPagerAdapter}.
     *
     * @return {@link MainContactsFragment} and Drawable combo
     */
    private Fragment getFavoritesTab() {
        return MainContactsFragment.newInstance();
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link ContactsFragmentPagerAdapter}.
     *
     * @return {@link MainGroupFragment} and Drawable combo
     */
    private Fragment getGroupsTab() {
        return MainGroupFragment.newInstance();
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getUserDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_group,
            getLargeIconDimen(getActivity()), _unselectedColor)
            .setContentDescription(getString(R.string.Contacts));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable}.
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getGroupDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_groups,
            getLargeIconDimen(getActivity()), _unselectedColor)
            .setContentDescription(getString(R.string.Groups));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * The {@link FragmentPagerAdapter} used to display Groups and Users.
     */
    final class ContactsFragmentPagerAdapter extends FragmentPagerAdapter {

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
            return _fragmentArray.get(i);
        }

        @Override
        public int getCount() {
            return _fragmentArray.size();
        }

    }

}
