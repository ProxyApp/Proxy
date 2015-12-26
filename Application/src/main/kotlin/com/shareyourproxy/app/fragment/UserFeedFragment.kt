package com.shareyourproxy.app.fragment

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.shareyourproxy.IntentLauncher
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxHelper
import com.shareyourproxy.api.rx.command.eventcallback.ActivityFeedDownloadedEvent
import com.shareyourproxy.app.adapter.ActivityFeedAdapter
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.dialog.ErrorDialog
import com.shareyourproxy.widget.ContentDescriptionDrawable
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton

import butterknife.Bind
import butterknife.BindDimen
import butterknife.BindString
import butterknife.ButterKnife
import retrofit.Response
import retrofit.Retrofit
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Created by Evan on 10/10/15.
 */
class UserFeedFragment : BaseFragment(), ItemClickListener {
    @Bind(R.id.fragment_user_feed_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_user_feed_empty_textview)
    internal var emptyTextView: TextView
    @BindString(R.string.fragment_userfeed_empty_title)
    internal var loggedInNullTitle: String
    @BindString(R.string.fragment_userfeed_empty_message)
    internal var stringNullMessage: String
    @BindString(R.string.fragment_userprofile_contact_empty_title)
    internal var contactNullTitle: String
    @BindDimen(R.dimen.common_svg_null_screen_small)
    internal var marginNullScreen: Int = 0
    @BindString(R.string.twitter_login_error)
    internal var twitterLoginError: String
    @BindString(R.string.twitter_login_error_message)
    internal var twitterLoginErrorMessage: String
    private var _isLoggedInUser: Boolean = false
    private var _userContact: User? = null
    private var _subscriptions: CompositeSubscription? = null
    private var _adapter: ActivityFeedAdapter? = null
    private var twitterLoginButton: TwitterLoginButton? = null
    private var _lastClickedAuthItem: Int = 0
    private val _rxHelper = RxHelper

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        _userContact = activity.intent.extras.getParcelable<User>(ARG_USER_SELECTED_PROFILE)
        _isLoggedInUser = isLoggedInUser(_userContact)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_user_feed, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    fun ActivityFeedObserver(): JustObserver<ActivityFeedDownloadedEvent> {
        return object : JustObserver<ActivityFeedDownloadedEvent>() {
            fun next(event: ActivityFeedDownloadedEvent) {
                activityFeedDownloaded(event)
            }
        }
    }

    /**
     * Initialize a twitter login button with a callback to handle errors.
     */
    private fun initializeTwitterLogin() {
        twitterLoginButton = TwitterLoginButton(activity)
        twitterLoginButton!!.visibility = View.GONE
        twitterLoginButton!!.callback = object : Callback<TwitterSession>() {
            override fun onResponse(response: Response<TwitterSession>, retrofit: Retrofit) {

            }

            override fun onFailure(t: Throwable) {

            }

            override fun success(result: Result<TwitterSession>) {
                Twitter.getSessionManager().setActiveSession(result.data)
                _adapter!!.removeItem(_lastClickedAuthItem)
            }

            override fun failure(exception: TwitterException) {
                Timber.e(Log.getStackTraceString(exception))
                ErrorDialog.newInstance(twitterLoginError,
                        twitterLoginErrorMessage).show(activity.supportFragmentManager)
            }
        }
    }

    private fun activityFeedDownloaded(event: ActivityFeedDownloadedEvent) {
        _adapter!!.refreshFeedData(event.feedItems)
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = _rxHelper.checkCompositeButton(_subscriptions)
    }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        initializeTwitterLogin()
        initializeRecyclerView()
        val session = Twitter.getSessionManager().activeSession
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        initializeEmptyView()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        _adapter = ActivityFeedAdapter.newInstance(recyclerView, _userContact, this)
        recyclerView.adapter = _adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun initializeEmptyView() {
        val context = context
        if (_isLoggedInUser) {
            val sb = SpannableStringBuilder(loggedInNullTitle).append("\n").append(stringNullMessage)

            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                    0, loggedInNullTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                    loggedInNullTitle.length + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null, getNullDrawable(R.raw.ic_ghost_doge), null, null)
        } else {
            val contactNullMessage = getString(
                    R.string.fragment_userprofile_contact_empty_message, _userContact!!.first())
            val sb = SpannableStringBuilder(contactNullTitle).append("\n").append(contactNullMessage)

            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                    0, loggedInNullTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                    loggedInNullTitle.length + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null, getNullDrawable(R.raw.ic_ghost_sloth), null, null)
        }
        recyclerView.setEmptyView(emptyTextView)
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private fun getNullDrawable(resId: Int): Drawable {
        return svgToBitmapDrawable(activity, resId, marginNullScreen)
    }

    override fun onItemClick(view: View, position: Int) {
        when (_adapter!!.getItemViewType(position)) {
            ActivityFeedAdapter.VIEWTYPE_HEADER -> {
                _lastClickedAuthItem = position
                when (_adapter!!.getItemData(position).channelType()) {
                    Twitter -> twitterLoginButton!!.performClick()
                }
            }
            ActivityFeedAdapter.VIEWTYPE_CONTENT -> {
                val url = _adapter!!.getItemData(position).actionAddress()
                IntentLauncher.launchWebIntent(activity, url)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterLoginButton!!.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        /**
         * Create a new user activity feed fragment.

         * @return user activity feed fragment.
         */
        fun newInstance(): UserFeedFragment {
            return UserFeedFragment()
        }
    }
}
/**
 * Constructor.
 */
