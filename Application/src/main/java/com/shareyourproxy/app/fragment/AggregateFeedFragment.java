package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.event.SearchClickedEvent;
import com.shareyourproxy.app.AggregateFeedActivity;
import com.shareyourproxy.util.ViewUtils;
import com.shareyourproxy.widget.ContactSearchLayout;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchSearchActivity;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static java.util.Arrays.asList;

/**
 * Add a {@link MainContactsFragment} and {@link MainGroupFragment} to this fragment's {@link AggregateFeedFragment#slidingTabLayout}.
 */
public class AggregateFeedFragment extends BaseFragment {
    public static final int ARG_SELECT_PROFILE_TAB = 0;
    public static final int ARG_SELECT_CONTACTS_TAB = 1;
    public static final int ARG_SELECT_GROUP_TAB = 2;
    @Bind(R.id.include_toolbar)
    Toolbar toolbar;
    @Bind(R.id.activity_main_drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.fragment_main_viewpager)
    ViewPager viewPager;
    @Bind(R.id.fragment_main_sliding_tabs)
    TabLayout slidingTabLayout;
    @Bind(R.id.fragment_main_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindColor(R.color.common_blue)
    int _selectedColor;
    @BindColor(R.color.common_proxy_dark_disabled)
    int _unselectedColor;
    @BindDimen(R.dimen.common_rect_small)
    int marginSVGLarge;
    private CompositeSubscription _subscriptions;
    private ContactSearchLayout _contactSearchLayout;

    /**
     * Constructor.
     */
    public AggregateFeedFragment() {
    }

    /**
     * Create a new instance of this fragment for the parent {@link AggregateFeedActivity}.
     *
     * @return main fragment
     */
    public static AggregateFeedFragment newInstance() {
        return new AggregateFeedFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable().subscribe(getObserver()));

    }

    public JustObserver<Object> getObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof SearchClickedEvent) {
                    launchSearchActivity(getActivity(),
                        _contactSearchLayout.getContainerView(),
                        _contactSearchLayout.getSearchTextView(),
                        _contactSearchLayout.getMenuImageView());
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Initialize this fragments data and {@link TabLayout}.
     */
    private void initialize() {
        _contactSearchLayout = new ContactSearchLayout(getActivity(), getRxBus(), drawerLayout);
        initializeDrawerFragment();
        buildCustomToolbar(toolbar, _contactSearchLayout);
        initializeFragments();
        initializeTabs();
    }

    private void initializeDrawerFragment() {
        MainDrawerFragment drawerFragment = MainDrawerFragment.newInstance();
        getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.activity_main_drawer_fragment_container, drawerFragment)
            .commit();
    }

    /**
     * Initialize this fragments tabs and their icons. Select the default tab based input intent data from user action.
     */
    private void initializeTabs() {

        ContentDescriptionDrawable userDrawable = getUserDrawable();
        String userDescription = userDrawable.getContentDescription();

        ContentDescriptionDrawable contactDrawable = getContactDrawable();
        String contactDescription = contactDrawable.getContentDescription();

        ContentDescriptionDrawable groupDrawable = getGroupDrawable();
        String groupDescription = groupDrawable.getContentDescription();


        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setIcon(userDrawable)
                .setContentDescription(userDescription));
        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setIcon(contactDrawable)
                .setContentDescription(contactDescription));
        slidingTabLayout.addTab(
            slidingTabLayout.newTab()
                .setIcon(getGroupDrawable())
                .setContentDescription(groupDescription));


        slidingTabLayout.setTabMode(TabLayout.MODE_FIXED);
        slidingTabLayout.setOnTabSelectedListener(getOnTabSelectedListener());
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(slidingTabLayout));
        //set the default selected tab
        TabLayout.Tab tab = slidingTabLayout.getTabAt(getActivity().getIntent().getExtras()
            .getInt(Constants.ARG_MAINFRAGMENT_SELECTED_TAB));
        ViewUtils.tintDrawableCompat(tab.getIcon(), _selectedColor);
        tab.select();
    }

    /**
     * Get a tab selection listener that tints tab drawables correctly.
     *
     * @return OnTabSelectedListener
     */
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
     * Add fragments to the List backing the {@link AggregateFeedFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
        User user = getLoggedInUser();
        List<BaseFragment> fragmentArray = asList(
            MainUserProfileFragment.newInstance(user, user.id()),
            MainContactsFragment.newInstance(),
            MainGroupFragment.newInstance());
        viewPager.setAdapter(
            BasePagerAdapter.newInstance(fragmentArray, getChildFragmentManager()));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getUserDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_account_circle, marginSVGLarge,
            _unselectedColor).setContentDescription(getString(R.string.profile));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getContactDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_group, marginSVGLarge, _unselectedColor)
            .setContentDescription(getString(R.string.contacts));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable}.
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getGroupDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_groups, marginSVGLarge, _unselectedColor)
            .setContentDescription(getString(R.string.groups));
    }

}
