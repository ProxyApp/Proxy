package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.design.widget.TabLayout.OnTabSelectedListener
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.shareyourproxy.Constants
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.event.SearchClickedEvent
import com.shareyourproxy.app.AggregateFeedActivity
import com.shareyourproxy.util.ViewUtils
import com.shareyourproxy.widget.ContactSearchLayout
import com.shareyourproxy.widget.ContentDescriptionDrawable

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.ButterKnife
import rx.subscriptions.CompositeSubscription

import com.shareyourproxy.IntentLauncher.launchSearchActivity
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import java.util.Arrays.asList

/**
 * Add a [MainContactsFragment] and [MainGroupFragment] to this fragment's [AggregateFeedFragment.slidingTabLayout].
 */
class AggregateFeedFragment : BaseFragment() {
    @Bind(R.id.include_toolbar)
    internal var toolbar: Toolbar
    @Bind(R.id.activity_main_drawer_layout)
    internal var drawerLayout: DrawerLayout
    @Bind(R.id.fragment_main_viewpager)
    internal var viewPager: ViewPager
    @Bind(R.id.fragment_main_sliding_tabs)
    internal var slidingTabLayout: TabLayout
    @Bind(R.id.fragment_main_coordinator_layout)
    internal var coordinatorLayout: CoordinatorLayout
    @BindColor(R.color.common_blue)
    internal var _selectedColor: Int = 0
    @BindColor(R.color.common_proxy_dark_disabled)
    internal var _unselectedColor: Int = 0
    @BindDimen(R.dimen.common_rect_small)
    internal var marginSVGLarge: Int = 0
    private var _subscriptions: CompositeSubscription? = null
    private var _contactSearchLayout: ContactSearchLayout? = null

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = CompositeSubscription()
        _subscriptions!!.add(rxBus.toObservable().subscribe(observer))

    }

    val observer: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is SearchClickedEvent) {
                    launchSearchActivity(activity,
                            _contactSearchLayout!!.containerView,
                            _contactSearchLayout!!.searchTextView,
                            _contactSearchLayout!!.menuImageView)
                }
            }
        }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    /**
     * Initialize this fragments data and [TabLayout].
     */
    private fun initialize() {
        _contactSearchLayout = ContactSearchLayout(activity, rxBus, drawerLayout)
        initializeDrawerFragment()
        buildCustomToolbar(toolbar, _contactSearchLayout)
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
        ViewUtils.tintDrawableCompat(tab.icon, _selectedColor)
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
                ViewUtils.tintDrawableCompat(tab.icon, _selectedColor)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                ViewUtils.tintDrawableCompat(tab.icon, _unselectedColor)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        }

    /**
     * Add fragments to the List backing the [AggregateFeedFragment.slidingTabLayout].
     */
    private fun initializeFragments() {
        val user = loggedInUser
        val fragmentArray = asList(
                MainUserProfileFragment.newInstance(user, user.id()),
                MainContactsFragment.newInstance(),
                MainGroupFragment.newInstance())
        viewPager.adapter = BaseFragment.BasePagerAdapter.newInstance(fragmentArray, childFragmentManager)
    }

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private val userDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_account_circle, marginSVGLarge,
                _unselectedColor).setContentDescription(getString(R.string.profile))

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private val contactDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_group, marginSVGLarge, _unselectedColor).setContentDescription(getString(R.string.contacts))

    /**
     * Parse a svg and return a Large sized [ContentDescriptionDrawable].

     * @return Drawable with a contentDescription
     */
    private val groupDrawable: ContentDescriptionDrawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_groups, marginSVGLarge, _unselectedColor).setContentDescription(getString(R.string.groups))

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
/**
 * Constructor.
 */
