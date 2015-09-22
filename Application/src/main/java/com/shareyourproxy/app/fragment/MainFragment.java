package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.event.SearchClickedEvent;
import com.shareyourproxy.app.MainActivity;
import com.shareyourproxy.util.ViewUtils;
import com.shareyourproxy.widget.ContactSearchLayout;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.IntentLauncher.launchSearchActivity;
import static com.shareyourproxy.util.ViewUtils.getLargeIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static java.util.Arrays.asList;

/**
 * Add a {@link MainContactsFragment} and {@link MainGroupFragment} to this fragment's {@link
 * MainFragment#slidingTabLayout}.
 */
public class MainFragment extends BaseFragment {

    public static final int ARG_SELECT_CONTACTS_TAB = 0;
    public static final int ARG_SELECT_GROUP_TAB = 1;
    @Bind(R.id.include_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.activity_main_drawer_layout)
    protected DrawerLayout drawerLayout;
    @Bind(R.id.fragment_main_viewpager)
    protected ViewPager viewPager;
    @Bind(R.id.fragment_main_sliding_tabs)
    protected TabLayout slidingTabLayout;
    @Bind(R.id.fragment_main_coordinator_layout)
    protected CoordinatorLayout coordinatorLayout;
    @BindColor(R.color.common_proxy_dark_selected)
    protected int _selectedColor;
    @BindColor(R.color.common_proxy_dark_disabled)
    protected int _unselectedColor;
    private CompositeSubscription _subscriptions;
    private ContactSearchLayout _contactSearchLayout;

    /**
     * Constructor.
     */
    public MainFragment() {
    }

    /**
     * Create a new instance of this fragment for the parent {@link MainActivity}.
     *
     * @return main fragment
     */
    public static MainFragment newInstance() {
        return new MainFragment();
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
            public void success(Object event) {
                if (event instanceof SearchClickedEvent) {
                    launchSearchActivity(getActivity(),
                        _contactSearchLayout.getContainerView(),
                        _contactSearchLayout.getSearchTextView(),
                        _contactSearchLayout.getMenuImageView());
                }
            }

            @Override
            public void error(Throwable e) {

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
        buildCustomToolbar(toolbar, _contactSearchLayout);
        initializeFragments();
        initializeTabs();
    }

    /**
     * Initialize this fragments tabs and their icons. Select the default tab based input intent
     * data from user action.
     */
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
        //set the defualt selected tab
        slidingTabLayout.getTabAt(getActivity().getIntent().getExtras()
            .getInt(Constants.ARG_MAINFRAGMENT_SELECTED_TAB)).select();
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
     * Add fragments to the List backing the {@link MainFragment#slidingTabLayout}.
     */
    private void initializeFragments() {
        List<Fragment> fragmentArray = asList(getFavoritesTab(), getGroupsTab());
        viewPager.setAdapter(
            MainFragmentPagerAdapter.newInstance(fragmentArray, getChildFragmentManager()));
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link MainFragmentPagerAdapter}.
     *
     * @return {@link MainContactsFragment} and Drawable combo
     */
    private Fragment getFavoritesTab() {
        return MainContactsFragment.newInstance();
    }

    /**
     * Pair an Image to a Fragment to simplify our {@link MainFragmentPagerAdapter}.
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
            .setContentDescription(getString(R.string.contacts));
    }

    /**
     * Parse a svg and return a Large sized {@link ContentDescriptionDrawable}.
     *
     * @return Drawable with a contentDescription
     */
    private ContentDescriptionDrawable getGroupDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_groups,
            getLargeIconDimen(getActivity()), _unselectedColor)
            .setContentDescription(getString(R.string.groups));
    }

    /**
     * The {@link FragmentPagerAdapter} used to display Groups and Users.
     */
    private static class MainFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> _fragmentArray;

        /**
         * Constructor.
         *
         * @param fragmentManager Manager of fragments.
         */
        private MainFragmentPagerAdapter(
            List<Fragment> fragmentArray, FragmentManager fragmentManager) {
            super(fragmentManager);
            _fragmentArray = fragmentArray;
        }

        public static MainFragmentPagerAdapter newInstance(
            List<Fragment> fragmentArray, FragmentManager fragmentManager){
            return new MainFragmentPagerAdapter(fragmentArray, fragmentManager);
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
