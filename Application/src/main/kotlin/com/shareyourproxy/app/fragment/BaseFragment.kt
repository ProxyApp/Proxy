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
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Base Fragment abstraction.
 */
abstract class BaseFragment : Fragment() {

    /**
     * Get the logged in user.
     * @return Logged in user
     */
    var loggedInUser: User
        get() = (activity as BaseActivity).loggedInUser
        set(user) {
            (activity as BaseActivity).loggedInUser = user
        }

    /**
     * Get currently logged in [User] in this [ProxyApplication].
     * @return logged in user
     */
    val sharedPreferences: SharedPreferences
        get() = (activity as BaseActivity).sharedPreferences

    /**
     * Initialize the color sequence of the swipe refresh view.
     */
    fun initializeSwipeRefresh(
            swipe: SwipeRefreshLayout, listener: SwipeRefreshLayout.OnRefreshListener) {
        swipe.setOnRefreshListener(listener)
        swipe.setColorSchemeResources(
                R.color.common_text, R.color.common_blue, R.color.common_green)
    }

    /**
     * Get the logged in user.
     * @return Logged in user
     */
    fun isLoggedInUser(user: User): Boolean {
        return (activity as BaseActivity).isLoggedInUser(user)
    }

    val supportActionBar: ActionBar
        get() = (activity as BaseActivity).supportActionBar

    fun buildToolbar(toolbar: Toolbar, title: String, icon: Drawable?) {
        (activity as BaseActivity).buildToolbar(toolbar, title, icon)
    }

    fun buildCustomToolbar(toolbar: Toolbar, customView: View) {
        (activity as BaseActivity).buildCustomToolbar(toolbar, customView)
    }

    val sharedPrefJsonUser: User? get() = (activity as BaseActivity).sharedPrefJsonUser

    /**
     * Get a scroll listener that dismisses the software keyboard on scroll.

     * @return dismissible scroll listener.
     */
    protected val dismissScrollListener: RecyclerView.OnScrollListener
        get() = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                hideSoftwareKeyboard(recyclerView)
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
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
    }

    /**
     * The [FragmentPagerAdapter] used to display base fragments.
     */
    internal class BasePagerAdapter
    /**
     * Constructor.
     * @param fragmentManager Manager of fragments.
     */
    private constructor(private val fragmentArray: List<BaseFragment>, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(i: Int): Fragment {
            return fragmentArray[i]
        }

        override fun getCount(): Int {
            return fragmentArray.size
        }

        companion object {

            fun newInstance(
                    fragmentArray: List<BaseFragment>, fragmentManager: FragmentManager): BasePagerAdapter {
                return BasePagerAdapter(fragmentArray, fragmentManager)
            }
        }
    }
}
