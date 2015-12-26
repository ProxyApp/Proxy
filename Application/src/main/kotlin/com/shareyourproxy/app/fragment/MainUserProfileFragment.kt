package com.shareyourproxy.app.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.app.UserContactActivity
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.ShareLinkDialog

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.OnClick

import android.view.View.GONE
import android.view.View.VISIBLE
import com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Display the logged in users profile and channels.
 */
class MainUserProfileFragment : UserProfileFragment() {
    @Bind(R.id.fragment_user_profile_header_title)
    internal var titleTextView: TextView
    @Bind(R.id.fragment_user_profile_fab_add_channel)
    internal var floatingActionButtonAddChannel: FloatingActionButton
    @Bind(R.id.fragment_user_profile_fab_share)
    internal var floatingActionButtonShare: FloatingActionButton
    @BindColor(android.R.color.white)
    internal var colorWhite: Int = 0
    @BindDimen(R.dimen.fragment_userprofile_header_user_background_size)
    internal var marginUserHeight: Int = 0

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_fab_add_channel)
    fun onClickAdd() {
        launchChannelListActivity(activity)
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_fab_share)
    fun onClickShare() {
        ShareLinkDialog.newInstance(loggedInUser.groups()).show(activity.supportFragmentManager)
    }

    internal override fun onCreateView(rootView: View) {
        super.onCreateView(rootView)
        initialize()
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        setHeaderHeight()
        initializeFabPlusIcon()
        setToolbarTitle()
        initializeHeader()
    }

    private fun setHeaderHeight() {
        val lp = collapsingToolbarLayout.layoutParams
        lp.height = marginUserHeight
        collapsingToolbarLayout.layoutParams = lp
    }

    /**
     * Set the content image of this [FloatingActionButton]
     */
    private fun initializeFabPlusIcon() {
        val plus = svgToBitmapDrawable(
                activity, R.raw.ic_add, svgLarge, colorWhite)
        floatingActionButtonAddChannel.setImageDrawable(plus)

        val share = svgToBitmapDrawable(
                activity, R.raw.ic_share, svgLarge, colorBlue)
        floatingActionButtonShare.setImageDrawable(share)
        floatingActionButtonAddChannel.visibility = VISIBLE
        floatingActionButtonShare.visibility = VISIBLE
    }

    private fun setToolbarTitle() {
        val title = loggedInUser.fullName()
        titleTextView.visibility = VISIBLE
        titleTextView.setText(title)
        supportActionBar.setDisplayHomeAsUpEnabled(false)
        toolbar.visibility = GONE
    }

    override fun onResume() {
        super.onResume()
        rxBus.toObservable().subscribe(onNextEvent())
    }


    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is RecyclerViewDatasetChangedEvent) {
                    toggleFabVisibility(event)
                }
            }
        }
    }

    fun toggleFabVisibility(event: RecyclerViewDatasetChangedEvent) {
        if (event.adapter is ViewChannelAdapter) {
            if (event.viewState == BaseRecyclerView.ViewState.EMPTY) {
                floatingActionButtonAddChannel.visibility = GONE
                floatingActionButtonShare.visibility = GONE
            } else {
                floatingActionButtonAddChannel.visibility = VISIBLE
                floatingActionButtonShare.visibility = VISIBLE
            }
        }
    }

    companion object {

        /**
         * Return new instance for parent [UserContactActivity].

         * @return layouts.fragment
         */
        fun newInstance(contact: User, loggedInUserId: String): MainUserProfileFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact)
            bundle.putString(ARG_LOGGEDIN_USER_ID, loggedInUserId)
            val fragment = MainUserProfileFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}
/**
 * Empty Fragment Constructor.
 */
