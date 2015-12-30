package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.design.widget.TabLayout.*
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.Constants.ARG_MAINFRAGMENT_SELECTED_TAB
import com.shareyourproxy.IntentLauncher.launchSearchActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_proxy_dark_disabled
import com.shareyourproxy.R.dimen.common_rect_small
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.layout.fragment_main
import com.shareyourproxy.R.raw.ic_account_circle
import com.shareyourproxy.R.raw.ic_group
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.event.SearchClickedEvent
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.ContactSearchLayout
import com.shareyourproxy.widget.ContentDescriptionDrawable
import rx.subscriptions.CompositeSubscription
import java.util.Arrays.asList

/**
 * Add a [MainContactsFragment] and [MainGroupFragment] to this fragment's [AggregateFeedFragment.slidingTabLayout].
 */
class AggregateFeedFragment() : BaseFragment() {
    companion object {
        val ARG_SELECT_PROFILE_TAB = 0
        val ARG_SELECT_CONTACTS_TAB = 1
        val ARG_SELECT_GROUP_TAB = 2
    }
    private val toolbar: Toolbar by bindView(R.id.include_toolbar)
    private val drawerLayout: DrawerLayout by bindView(activity_main_drawer_layout)
    private val viewPager: ViewPager by bindView(fragment_main_viewpager)
    private val slidingTabLayout: TabLayout by bindView(fragment_main_sliding_tabs)
    private val selectedColor: Int by bindColor(R.color.common_blue)
    private val unselectedColor: Int by bindColor(common_proxy_dark_disabled)
    private val marginSVGLarge: Int by bindDimen(common_rect_small)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val contactSearchLayout: ContactSearchLayout = ContactSearchLayout(activity, drawerLayout)
    private val userDrawable: ContentDescriptionDrawable = svgToBitmapDrawable(activity, ic_account_circle, marginSVGLarge, unselectedColor).setContentDescription(getString(profile))
    private val contactDrawable: ContentDescriptionDrawable = svgToBitmapDrawable(activity, ic_group, marginSVGLarge, unselectedColor).setContentDescription(getString(contacts))
    private val groupDrawable: ContentDescriptionDrawable = svgToBitmapDrawable(activity, R.raw.ic_groups, marginSVGLarge, unselectedColor).setContentDescription(getString(groups))
    private val observer: JustObserver<Any> = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is SearchClickedEvent) {
                launchSearchActivity(activity,
                        contactSearchLayout.containerView,
                        contactSearchLayout.searchTextView,
                        contactSearchLayout.menuImageView)
            }
        }
    }
    /**
     * Get a tab selection listener that tints tab drawables correctly.
     * @return OnTabSelectedListener
     */
    private val onTabSelectedListener: OnTabSelectedListener = object : OnTabSelectedListener {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(observer))
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
        activity.supportFragmentManager.beginTransaction().replace(R.id.activity_main_drawer_fragment_container, MainDrawerFragment()).commit()
    }

    /**
     * Initialize this fragments tabs and their icons. Select the default tab based input intent data from user action.
     */
    private fun initializeTabs() {

        val userDescription = userDrawable.getContentDescription()
        val contactDescription = contactDrawable.getContentDescription()
        val groupDescription = groupDrawable.getContentDescription()

        slidingTabLayout.addTab(slidingTabLayout.newTab().setIcon(userDrawable).setContentDescription(userDescription))
        slidingTabLayout.addTab(slidingTabLayout.newTab().setIcon(contactDrawable).setContentDescription(contactDescription))
        slidingTabLayout.addTab(slidingTabLayout.newTab().setIcon(groupDrawable).setContentDescription(groupDescription))

        slidingTabLayout.tabMode = MODE_FIXED
        slidingTabLayout.setOnTabSelectedListener(onTabSelectedListener)
        viewPager.offscreenPageLimit = 3
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(slidingTabLayout))
        //set the default selected tab
        val tab = slidingTabLayout.getTabAt(activity.intent.extras.getInt(ARG_MAINFRAGMENT_SELECTED_TAB))
        ViewUtils.tintDrawableCompat(tab.icon, selectedColor)
        tab.select()
    }

    /**
     * Add fragments to the List backing the [AggregateFeedFragment.slidingTabLayout].
     */
    private fun initializeFragments() {
        val user = loggedInUser
        val fragmentArray = asList(MainUserProfileFragment(user, user.id), MainContactsFragment(), MainGroupFragment())
        viewPager.adapter = BasePagerAdapter(fragmentArray, childFragmentManager)
    }
}
