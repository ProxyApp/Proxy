package com.shareyourproxy.app.fragment

import android.R.color.white
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_add
import com.shareyourproxy.R.raw.ic_share
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.ShareLinkDialog
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.ViewState.EMPTY
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import rx.subscriptions.CompositeSubscription

/**
 * Display the logged in users profile and channels.
 */
internal final class MainUserProfileFragment private constructor(contact: User, loggedInUserId: String) : UserProfileFragment() {
    companion object {
        fun create(contact: User, loggedInUserId: String): MainUserProfileFragment {
            val fragment = MainUserProfileFragment(contact, loggedInUserId)
            val args: Bundle = Bundle()
            args.putParcelable(ARG_USER_SELECTED_PROFILE, contact)
            args.putString(ARG_LOGGEDIN_USER_ID, loggedInUserId)
            fragment.arguments = args
            return fragment
        }
    }

    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val titleTextView: TextView by bindView(fragment_user_profile_header_title)
    private val floatingActionButtonAddChannel: FloatingActionButton by bindView(fragment_user_profile_fab_add_channel)
    private val floatingActionButtonShare: FloatingActionButton by bindView(fragment_user_profile_fab_share)
    private val colorWhite: Int by bindColor(white)
    private val marginUserHeight: Int by bindDimen(R.dimen.fragment_userprofile_header_user_background_size)
    private val onClickAdd: View.OnClickListener = View.OnClickListener {
        launchChannelListActivity(activity)
    }
    private val onClickShare: View.OnClickListener = View.OnClickListener {
        ShareLinkDialog(loggedInUser.groups).show(activity.supportFragmentManager)
    }
    private val onNextEvent = object : JustObserver<Any>(MainUserProfileFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is RecyclerViewDatasetChangedEvent) {
                toggleFabVisibility(event)
            }
        }
    }

    override fun onCreateView(rootView: View) {
        super.onCreateView(rootView)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(rxBusObservable().subscribe(onNextEvent))
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
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
        val plus = svgToBitmapDrawable(activity, ic_add, svgLarge, colorWhite)
        floatingActionButtonAddChannel.setImageDrawable(plus)
        floatingActionButtonAddChannel.setOnClickListener(onClickAdd)

        val share = svgToBitmapDrawable(activity, ic_share, svgLarge, colorBlue)
        floatingActionButtonShare.setImageDrawable(share)
        floatingActionButtonAddChannel.visibility = VISIBLE
        floatingActionButtonShare.visibility = VISIBLE
        floatingActionButtonShare.setOnClickListener(onClickShare)
    }

    private fun setToolbarTitle() {
        val title = loggedInUser.fullName
        titleTextView.visibility = VISIBLE
        titleTextView.text = title
        supportActionBar.setDisplayHomeAsUpEnabled(false)
    }

    private fun toggleFabVisibility(event: RecyclerViewDatasetChangedEvent) {
        if (event.adapter is ViewChannelAdapter) {
            if (event.viewState == EMPTY) {
                floatingActionButtonAddChannel.visibility = GONE
                floatingActionButtonShare.visibility = GONE
            } else {
                floatingActionButtonAddChannel.visibility = VISIBLE
                floatingActionButtonShare.visibility = VISIBLE
            }
        }
    }
}
