package com.shareyourproxy.app.fragment


import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.RxQuery
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.event.NotificationCardActionEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.UserContactsAdapter
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder
import com.shareyourproxy.widget.ContentDescriptionDrawable

import java.util.HashSet

import butterknife.Bind
import butterknife.BindDimen
import butterknife.BindString
import butterknife.ButterKnife
import butterknife.OnClick
import rx.subscriptions.CompositeSubscription

import com.shareyourproxy.IntentLauncher.launchInviteFriendIntent
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.INVITE_FRIENDS

/**
 * A recyclerView of Favorite [User]s.
 */
class MainContactsFragment : BaseFragment(), ItemClickListener {
    @Bind(R.id.fragment_contact_main_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_contact_main_swipe_refresh)
    internal var swipeRefreshLayout: SwipeRefreshLayout
    @Bind(R.id.fragment_contact_main_empty_textview)
    internal var emptyTextView: TextView
    @Bind(R.id.fragment_contact_main_empty_view)
    internal var emptyView: ScrollView
    @BindDimen(R.dimen.common_margin_medium)
    internal var catPadding: Int = 0
    @BindDimen(R.dimen.common_svg_null_screen_small)
    internal var marginNullScreen: Int = 0
    @BindString(R.string.fragment_contact_main_empty_title)
    internal var nullTitle: String
    @BindString(R.string.fragment_contact_main_empty_message)
    internal var nullMessage: String
    internal var _refreshListener: OnRefreshListener = OnRefreshListener {
        val user = loggedInUser
        if (user != null) {
            rxBus.post(SyncContactsCommand(loggedInUser))
        }
    }
    private var _adapter: UserContactsAdapter? = null
    private var _subscriptions: CompositeSubscription? = null
    private val _rxQuery = RxQuery

    @OnClick(R.id.fragment_contact_main_empty_button)
    fun onClickInviteFriend() {
        launchInviteFriendIntent(activity)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_contacts_main, container, false)
        ButterKnife.bind(this, rootView)
        initializeRecyclerView()
        initializeSwipeRefresh(swipeRefreshLayout, _refreshListener)
        return rootView
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        val showHeader = !sharedPreferences.getBoolean(INVITE_FRIENDS.key, false)
        _adapter = UserContactsAdapter.newInstance(recyclerView, sharedPreferences,
                showHeader, this)
        initializeEmptyView()

        recyclerView.setEmptyView(emptyView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = _adapter
    }

    private fun initializeEmptyView() {
        val context = context
        val draw = catDrawable
        draw.setBounds(-catPadding, 0, draw.intrinsicWidth, draw.intrinsicHeight)
        emptyTextView.setPadding(catPadding, 0, catPadding, 0)
        emptyTextView.setCompoundDrawables(null, draw, null, null)

        val sb = SpannableStringBuilder(nullTitle).append("\n").append(nullMessage)

        sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                0, nullTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                nullTitle.length + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        emptyTextView.text = sb
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private val catDrawable: Drawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_gato, marginNullScreen)

    override fun onResume() {
        super.onResume()
        _subscriptions = CompositeSubscription()
        _subscriptions!!.add(rxBus.toObservable().subscribe(busObserver))
        checkRefresh(loggedInUser)
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is UserSelectedEvent) {
                    onUserSelected(event)
                } else if (event is LoggedInUserUpdatedEventCallback) {
                    userUpdated(event)
                } else if (event is SyncContactsCommand) {
                    swipeRefreshLayout.isRefreshing = true
                } else if (event is SyncAllContactsSuccessEvent) {
                    swipeRefreshLayout.isRefreshing = false
                } else if (event is SyncAllContactsErrorEvent) {
                    swipeRefreshLayout.isRefreshing = false
                } else if (event is NotificationCardActionEvent) {
                    launchInviteFriendIntent(activity)
                }
            }
        }

    /**
     * Refresh user data.

     * @param user contacts to refresh
     */
    fun checkRefresh(user: User) {
        val contacts = user.contacts()
        if (contacts != null) {
            _adapter!!.refreshUserList(_rxQuery.queryUserContacts(activity, contacts))
        }
    }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.isRefreshing = false
    }

    private fun userUpdated(event: LoggedInUserUpdatedEventCallback) {
        checkRefresh(event.user)
    }

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        val user = _adapter!!.getItemData(position)
        RxGoogleAnalytics(activity).contactProfileViewed(user)
        rxBus.post(UserSelectedEvent(holder.userImage, holder.userName, user))
    }

    /**
     * User selected, launch that contacts profile.

     * @param event data
     */
    fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id(),
                event.imageView, event.textView)
    }

    companion object {

        /**
         * Create a new layouts.fragment with favorite contacts.

         * @return user layouts.fragment
         */
        fun newInstance(): MainContactsFragment {
            return MainContactsFragment()
        }
    }
}
/**
 * Constructor.
 */
