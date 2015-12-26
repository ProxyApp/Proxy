package com.shareyourproxy.app.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxHelper
import com.shareyourproxy.api.rx.RxQuery
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.EditChannelDialog
import com.shareyourproxy.widget.ContentDescriptionDrawable

import java.util.HashMap

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindString
import butterknife.ButterKnife
import butterknife.OnClick
import rx.subscriptions.CompositeSubscription

import android.view.View.GONE
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE

/**
 * Created by Evan on 10/10/15.
 */
class UserChannelsFragment : BaseFragment(), ItemLongClickListener {
    @Bind(R.id.fragment_user_channel_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_user_channel_empty_view_container)
    internal var emptyViewContainer: LinearLayout
    @Bind(R.id.fragment_user_channel_empty_button)
    internal var addChannelButton: Button
    @Bind(R.id.fragment_user_channel_empty_textview)
    internal var emptyTextView: TextView
    @Bind(R.id.fragment_user_channel_coordinator)
    internal var coordinatorLayout: CoordinatorLayout
    @BindString(R.string.fragment_userchannels_empty_title)
    internal var loggedInNullTitle: String
    @BindString(R.string.fragment_userchannels_empty_message)
    internal var loggedInNullMessage: String
    @BindString(R.string.fragment_userprofile_contact_empty_title)
    internal var contactNullTitle: String
    @BindDimen(R.dimen.common_svg_null_screen_mini)
    internal var marginNullScreen: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    private var _isLoggedInUser: Boolean = false
    private var _userContact: User? = null
    private var _adapter: ViewChannelAdapter? = null
    private var _subscriptions: CompositeSubscription? = null
    private val _rxQuery = RxQuery
    private val _rxHelper = RxHelper

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_channel_empty_button)
    fun onClickAddChannel() {
        launchChannelListActivity(activity)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        _userContact = arguments.getParcelable<User>(ARG_USER_SELECTED_PROFILE)
        _isLoggedInUser = isLoggedInUser(_userContact)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_user_channels, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        if (_isLoggedInUser) {
            initializeRecyclerView(loggedInUser.channels())
        } else {
            addChannelButton.visibility = GONE
            initializeRecyclerView(null)
        }
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView(channels: HashMap<String, Channel>?) {
        _adapter = ViewChannelAdapter.newInstance(
                recyclerView, sharedPreferences, isShowHeader(channels), this)
        initializeEmptyView()

        recyclerView.setEmptyView(emptyViewContainer)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = _adapter
    }

    private fun isShowHeader(channels: HashMap<String, Channel>?): Boolean {
        return channels != null && _isLoggedInUser && channels.size > 0 && !sharedPreferences.getBoolean(SHARE_PROFILE.key, false)
    }

    private fun initializeEmptyView() {
        val context = context
        if (_isLoggedInUser) {
            val sb = SpannableStringBuilder(loggedInNullTitle).append("\n").append(loggedInNullMessage)

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
                    0, contactNullTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                    contactNullTitle.length + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb

            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null, getNullDrawable(R.raw.ic_ghost_sloth), null, null)
        }
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private fun getNullDrawable(resId: Int): Drawable {
        return svgToBitmapDrawable(activity, resId, marginNullScreen)
    }

    fun getSharedChannels() {
        _subscriptions!!.add(_rxQuery.queryPermissionedChannels(_userContact, loggedInUser.id()).subscribe(permissionedObserver()))
    }

    private fun permissionedObserver(): JustObserver<HashMap<String, Channel>> {
        return object : JustObserver<HashMap<String, Channel>>() {
            fun next(channels: HashMap<String, Channel>) {
                _adapter!!.updateChannels(channels)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = _rxHelper.checkCompositeButton(_subscriptions)
        _subscriptions!!.add(rxBus.toObservable().subscribe(onNextEvent()))
        if (_isLoggedInUser) {
            syncUsersContacts()
        } else {
            getSharedChannels()
        }
        recyclerView.scrollToPosition(0)
    }

    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is UserChannelAddedEventCallback) {
                    addUserChannel(event)
                } else if (event is UserChannelDeletedEventCallback) {
                    deleteUserChannel(event)
                } else if (event is SyncAllContactsSuccessEvent) {
                    if (_isLoggedInUser) {
                        syncUsersContacts()
                    }
                }
            }
        }
    }

    fun syncUsersContacts() {
        val channels = loggedInUser.channels()
        if (channels != null && channels!!.size > 0) {
            _adapter!!.updateChannels(channels)
        } else {
            recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(
                    _adapter, BaseRecyclerView.ViewState.EMPTY))
        }
    }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    private fun addUserChannel(event: UserChannelAddedEventCallback) {
        if (event.oldChannel != null) {
            _adapter!!.updateItem(event.oldChannel, event.newChannel)
        } else {
            _adapter!!.addItem(event.newChannel)
        }
    }

    private fun deleteUserChannel(event: UserChannelDeletedEventCallback) {
        _adapter!!.removeItem(event.position)
    }

    override fun onItemClick(view: View, position: Int) {
        val channel = _adapter!!.getItemData(position)
        rxBus.post(SelectUserChannelEvent(channel))
    }

    override fun onItemLongClick(view: View, position: Int) {
        val channel = _adapter!!.getItemData(position)
        if (_isLoggedInUser) {
            EditChannelDialog.newInstance(channel, position).show(fragmentManager)
        }
    }

    companion object {

        /**
         * Create a new user channel fragment.

         * @return user channels fragment.
         */
        fun newInstance(contact: User): UserChannelsFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact)
            val fragment = UserChannelsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
/**
 * Constructor.
 */
