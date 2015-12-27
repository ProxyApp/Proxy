package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.design.widget.TabLayout.OnTabSelectedListener
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener
import android.support.v4.content.ContextCompat.getColor
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.Constants
import com.shareyourproxy.IntentLauncher.launchSearchActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_proxy_dark_disabled
import com.shareyourproxy.R.dimen.common_rect_small
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_account_circle
import com.shareyourproxy.R.raw.ic_group
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.event.SearchClickedEvent
import com.shareyourproxy.util.ViewUtils
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import com.shareyourproxy.widget.ContactSearchLayout
import com.shareyourproxy.widget.ContentDescriptionDrawable
import rx.subscriptions.CompositeSubscription
import java.util.Arrays.asList

/**
 * Add a [MainContactsFragment] and [MainGroupFragment] to this fragment's [AggregateFeedFragment.slidingTabLayout].
 */
class AggregateFeedFragment : BaseFragment() {
    private val toolbar: Toolbar by bindView(R.id.include_toolbar)
    private val drawerLayout: DrawerLayout by bindView(activity_main_drawer_layout)
    private val viewPager: ViewPager by bindView(fragment_main_viewpager)
    private val slidingTabLayout: TabLayout by bindView(fragment_main_sliding_tabs)
    internal var selectedColor: Int = getColor(context, common_blue)
    internal var unselectedColor: Int = getColor(context, common_proxy_dark_disabled)
    internal var marginSVGLarge: Int = resources.getDimensionPixelSize(common_rect_small)
    private var subscriptions: CompositeSubscription = CompositeSubscription()
    private var contactSearchLayout: ContactSearchLayout = ContactSearchLayout(activity, drawerLayout)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        initialize()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(observer))
    }

    val observer: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is SearchClickedEvent) {
                    launchSearchActivity(activity,
                            contactSearchLayout.containerView,
                            contactSearchLayout.searchTextView,
                            contactSearchLayout.menuImageView)
                }
            }
        }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    /**
     * Initialize this fragments data and [TabLayout].
     */
    private fun initialize() {
        initializeDrawerFragment()
        buildCustomToolbar(toolbar, contactSearchLayout)
        initializeFragments()
        initializeTabs()
    }

    private fun initializeDrawerFragment() {
        val drawerFragment = MainDrawerFragment.newInstance()
        activity.supportFragmentManager.beginTransaction().replace(R.id.activity_main_drawer_fragment_container, drawerFragment).commit()
    }

    /**
     * Initialize this fragments tabs and their icons. Select the default tab based input intent data from user action.
     */
    private fun initializeTabs() {

        val userDrawable = userDrawable
        val userDescription = userDrawable.getContentDescription()

        val contactDrawable = contactDrawable
        val contactDescription = contactDrawable.getContentDescription()

        val groupDrawable = groupDrawable
        val groupDescription = groupDrawable.getContentDescription()


        slidingTabLayout.addTab(
                slidingTabLayout.newTab().setIcon(userDrawable).setContentDescription(userDescription))
        slidingTabLayout.addTab(
                slidingTabLayout.newTab().setIcon(contactDrawable).setContentDescription(contactDescription))
        slidingTabLayout.addTab(
                slidingTabLayout.newTab().setIcon(groupDrawable).setContentDescription(groupDescription))


        slidingTabLayout.tabMode = TabLayout.MODE_FIXED
        slidingTabLayout.setOnTabSelectedListener(onTabSelectedListener)
        viewPager.offscreenPageLimit = 3
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(slidingTabLayout))
        //set the default selected tab
        val tab = slidingTabLayout.getTabAt(activity.intent.extras.getInt(Constants.ARG_MAINFRAGMENT_SELECTED_TAB))
        ViewUtils.tintDrawableCompat(tab.icon, selectedColor)
        tab.select()
    }

    /**
     * Get a tab selection listener that tints tab drawables correctly.

     * @return OnTabSelectedListener
     */
    private val onTabSelectedListener: OnTabSelectedListener
        get() = object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                ViewUtils.tintDrawableCompat(tab.icon, selectedColor)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                ViewUtils.tintDrawableCompat(tab.icon, unselectedColor)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        }

    /**
     * Add fragments to the List backing the [AggregateFeedFragment.slidingTabLayout].
     */
    private fun initializeFragments() {
        val user = loggedInUser
        val fragmentArray = asList(MainUserProfileFragment.newInstance(user, user.id), MainContactsFragment.newInstance(), MainGroupFragment.newInstance())
        viewPager.adapter = BaseFragment.BasePagerAdapter.newInstance(fragmentArray, childFragmentManager)
    }

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable] .
     * @return Drawable with a contentDescription
     */
    private val userDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, ic_account_circle, marginSVGLarge, unselectedColor).setContentDescription(getString(profile))

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable] .
     * @return Drawable with a contentDescription
     */
    private val contactDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, ic_group, marginSVGLarge, unselectedColor).setContentDescription(getString(contacts))

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable].
     * @return Drawable with a contentDescription
     */
    private val groupDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_groups, marginSVGLarge, unselectedColor).setContentDescription(getString(groups))

    companion object {
        val ARG_SELECT_PROFILE_TAB = 0
        val ARG_SELECT_CONTACTS_TAB = 1
        val ARG_SELECT_GROUP_TAB = 2

        /**
         * Create a new instance of this fragment for the parent [AggregateFeedActivity].
         * @return main fragment
         */
        fun newInstance(): AggregateFeedFragment {
            return AggregateFeedFragment()
        }
    }

}
