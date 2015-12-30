package com.shareyourproxy.app.fragment

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.shareyourproxy.ProxyApplication
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.BaseActivity
import com.shareyourproxy.util.ButterKnife
import com.shareyourproxy.util.ViewUtils

/**
 * Base Fragment abstraction.
 */
abstract class BaseFragment : Fragment() {

    var loggedInUser: User = (activity as BaseActivity).loggedInUser
    val sharedPreferences: SharedPreferences = (activity as BaseActivity).sharedPreferences
    val supportActionBar: ActionBar = (activity as BaseActivity).supportActionBar
    val sharedPrefJsonUser: User? = (activity as BaseActivity).sharedPrefJsonUser

    /**
     * Get a scroll listener that dismisses the software keyboard on scroll.
     * @return dismissible scroll listener.
     */
    protected val dismissScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            ViewUtils.hideSoftwareKeyboard(recyclerView)
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    fun initializeSwipeRefresh(swipe: SwipeRefreshLayout, listener: SwipeRefreshLayout.OnRefreshListener) {
        swipe.setOnRefreshListener(listener)
        swipe.setColorSchemeResources(R.color.common_text, R.color.common_blue, R.color.common_green)
    }

    /**
     * Get the logged in user.
     * @return Logged in user
     */
    fun isLoggedInUser(user: User): Boolean {
        return (activity as BaseActivity).isLoggedInUser(user)
    }

    fun buildToolbar(toolbar: Toolbar, title: String, icon: Drawable?) {
        (activity as BaseActivity).buildToolbar(toolbar, title, icon)
    }

    fun buildCustomToolbar(toolbar: Toolbar, customView: View) {
        (activity as BaseActivity).buildCustomToolbar(toolbar, customView)
    }

    /**
     * Display a snack bar notifying the user that they've updated their information.
     */
    fun showChangesSavedSnackBar(coordinatorLayout: View) {
        Snackbar.make(coordinatorLayout, getString(R.string.changes_saved), Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ProxyApplication.watchForLeak(this)
        ButterKnife.unbind(this)
    }

    /**
     * The [FragmentPagerAdapter] used to display base fragments.
     */
    protected class BasePagerAdapter(private val fragmentArray: List<BaseFragment>, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(i: Int): Fragment {
            return fragmentArray[i]
        }

        override fun getCount(): Int {
            return fragmentArray.size
        }
    }
}
