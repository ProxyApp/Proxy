package com.shareyourproxy.app.fragment;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.app.BaseActivity;

import java.util.List;

import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Base Fragment abstraction.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Get the logged in user.
     *
     * @return Logged in user
     */
    public User getLoggedInUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

    /**
     * Set the logged in user.
     */
    public void setLoggedInUser(User user) {
        ((BaseActivity) getActivity()).setLoggedInUser(user);
    }

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public SharedPreferences getSharedPreferences() {
        return ((BaseActivity) getActivity()).getSharedPreferences();
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    public void initializeSwipeRefresh(
        SwipeRefreshLayout swipe, SwipeRefreshLayout
        .OnRefreshListener listener) {
        swipe.setOnRefreshListener(listener);
        swipe.setColorSchemeResources(
            R.color.common_text, R.color.common_blue, R.color.common_green);
    }

    /**
     * Get the logged in user.
     *
     * @return Logged in user
     */
    public boolean isLoggedInUser(User user) {
        return ((BaseActivity) getActivity()).isLoggedInUser(user);
    }

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }

    public ActionBar getSupportActionBar() {
        return ((BaseActivity) getActivity()).getSupportActionBar();
    }

    public void buildToolbar(Toolbar toolbar, String title, Drawable icon) {
        ((BaseActivity) getActivity()).buildToolbar(toolbar, title, icon);
    }

    public void buildCustomToolbar(Toolbar toolbar, View customView) {
        ((BaseActivity) getActivity()).buildCustomToolbar(toolbar, customView);
    }

    public User getSharedPrefJsonUser() {
        return ((BaseActivity) getActivity()).getSharedPrefJsonUser();
    }

    /**
     * Get a scroll listener that dismisses the software keyboard on scroll.
     *
     * @return dismissible scroll listener.
     */
    protected RecyclerView.OnScrollListener getDismissScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideSoftwareKeyboard(recyclerView);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };
    }

    /**
     * Display a snack bar notifying the user that they've updated their information.
     */
    public void showChangesSavedSnackBar(View coordinatorLayout) {
        Snackbar.make(coordinatorLayout, getString(R.string.changes_saved), Snackbar.LENGTH_LONG)
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProxyApplication.Companion.watchForLeak(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * The {@link FragmentPagerAdapter} used to display base fragments.
     */
    static class BasePagerAdapter extends FragmentPagerAdapter {
        private List<? extends BaseFragment> _fragmentArray;

        /**
         * Constructor.
         *
         * @param fragmentManager Manager of fragments.
         */
        private BasePagerAdapter(
            List<? extends BaseFragment> fragmentArray, FragmentManager fragmentManager) {
            super(fragmentManager);
            _fragmentArray = fragmentArray;
        }

        public static BasePagerAdapter newInstance(
            List<? extends BaseFragment> fragmentArray, FragmentManager fragmentManager) {
            return new BasePagerAdapter(fragmentArray, fragmentManager);
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
