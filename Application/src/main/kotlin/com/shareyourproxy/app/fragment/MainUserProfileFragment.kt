package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat.getColor
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.id.fragment_user_profile_fab_add_channel
import com.shareyourproxy.R.id.fragment_user_profile_fab_share
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver.rxBusObservable
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.ShareLinkDialog
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import org.jetbrains.anko.onClick

/**
 * Display the logged in users profile and channels.
 */
class MainUserProfileFragment : UserProfileFragment() {
    private val titleTextView: TextView by bindView(R.id.fragment_user_profile_header_title)
    private val floatingActionButtonAddChannel: FloatingActionButton by bindView(fragment_user_profile_fab_add_channel)
    private val floatingActionButtonShare: FloatingActionButton by bindView(fragment_user_profile_fab_share)
    internal var colorWhite: Int = getColor(context, android.R.color.white)
    internal var marginUserHeight: Int = resources.getDimensionPixelSize(R.dimen.fragment_userprofile_header_user_background_size)

    private val onClickAdd: View.OnClickListener = View.OnClickListener {
        launchChannelListActivity(activity)
    }

    private val onClickShare: View.OnClickListener = View.OnClickListener {
        ShareLinkDialog.newInstance(loggedInUser.groups).show(activity.supportFragmentManager)
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
        val plus = svgToBitmapDrawable(activity, R.raw.ic_add, svgLarge, colorWhite)
        floatingActionButtonAddChannel.setImageDrawable(plus)
        floatingActionButtonAddChannel.onClick { onClickAdd }

        val share = svgToBitmapDrawable(activity, R.raw.ic_share, svgLarge, colorBlue)
        floatingActionButtonShare.setImageDrawable(share)
        floatingActionButtonAddChannel.visibility = VISIBLE
        floatingActionButtonShare.visibility = VISIBLE
        floatingActionButtonShare.onClick { onClickShare }
    }

    private fun setToolbarTitle() {
        val title = loggedInUser.fullName
        titleTextView.visibility = VISIBLE
        titleTextView.text = title
        supportActionBar.setDisplayHomeAsUpEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        rxBusObservable().subscribe(onNextEvent())
    }


    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
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
