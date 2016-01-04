package com.shareyourproxy.app.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.dimen.common_svg_null_screen_mini
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_ghost_doge
import com.shareyourproxy.R.raw.ic_ghost_sloth
import com.shareyourproxy.R.string.*
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body2
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.RxQuery.queryPermissionedChannels
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.EditChannelDialog
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.ViewState.EMPTY
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * A User's channels
 */
class UserChannelsFragment(contact: User) : BaseFragment(), ItemLongClickListener {
    init {
        arguments.putParcelable(ARG_USER_SELECTED_PROFILE, contact)
    }

    private val recyclerView: BaseRecyclerView by bindView(fragment_user_channel_recyclerview)
    private val emptyViewContainer: LinearLayout by bindView(fragment_user_channel_empty_view_container)
    private val addChannelButton: Button by bindView(fragment_user_channel_empty_button)
    private val emptyTextView: TextView by bindView(fragment_user_channel_empty_textview)
    private val loggedInNullTitle: String by bindString(fragment_userchannels_empty_title)
    private val loggedInNullMessage: String by bindString(fragment_userchannels_empty_message)
    private val contactNullTitle: String by bindString(fragment_userprofile_contact_empty_title)
    private val marginNullScreen: Int by bindDimen(common_svg_null_screen_mini)
    private val colorBlue: Int by bindColor(R.color.common_blue)
    private val userContact: User = arguments.getParcelable<User>(ARG_USER_SELECTED_PROFILE)
    private val isLoggedInUser: Boolean = isLoggedInUser(userContact)
    private val adapter: ViewChannelAdapter = ViewChannelAdapter(recyclerView, sharedPreferences, isShowHeader(if (isLoggedInUser) loggedInUser.channels else null), this)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val drawableDoge: Drawable = svgToBitmapDrawable(activity, ic_ghost_doge, marginNullScreen)
    private val drawableSloth: Drawable = svgToBitmapDrawable(activity, ic_ghost_sloth, marginNullScreen)
    private val onClickAddChannel: View.OnClickListener = OnClickListener {
        launchChannelListActivity(activity)
    }
    private val onNextEvent = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is UserChannelAddedEventCallback) {
                addUserChannel(event)
            } else if (event is UserChannelDeletedEventCallback) {
                deleteUserChannel(event)
            } else if (event is SyncAllContactsSuccessEvent) {
                if (isLoggedInUser) {
                    syncUsersContacts()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(rxBusObservable().subscribe(onNextEvent))
        if (isLoggedInUser) {
            syncUsersContacts()
        } else {
            getSharedChannels()
        }
        recyclerView.scrollToPosition(0)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    override fun onItemClick(view: View, position: Int) {
        val channel = adapter.getItemData(position)
        post(SelectUserChannelEvent(channel))
    }

    override fun onItemLongClick(view: View, position: Int) {
        val channel = adapter.getItemData(position)
        if (isLoggedInUser) {
            EditChannelDialog(channel, position).show(fragmentManager)
        }
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        if (isLoggedInUser) {
            initializeRecyclerView()
        } else {
            addChannelButton.visibility = GONE
            initializeRecyclerView()
        }
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        initializeEmptyView()
        recyclerView.setEmptyView(emptyViewContainer)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        addChannelButton.setOnClickListener(onClickAddChannel)
    }

    private fun isShowHeader(channels: HashMap<String, Channel>?): Boolean {
        return channels != null && isLoggedInUser && channels.size > 0 && !sharedPreferences.getBoolean(SHARE_PROFILE.key, false)
    }

    private fun initializeEmptyView() {
        val context = context
        if (isLoggedInUser) {
            val sb = SpannableStringBuilder(loggedInNullTitle).append("\n").append(loggedInNullMessage)

            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body2), 0, loggedInNullTitle.length, SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body), loggedInNullTitle.length + 1, sb.length, SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawableDoge, null, null)


        } else {
            val contactNullMessage = getString(fragment_userprofile_contact_empty_message, userContact.first)
            val sb = SpannableStringBuilder(contactNullTitle).append("\n").append(contactNullMessage)

            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body2), 0, contactNullTitle.length, SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body), contactNullTitle.length + 1, sb.length, SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawableSloth, null, null)
        }
    }

    private fun getSharedChannels() {
        subscriptions.add(queryPermissionedChannels(userContact, loggedInUser.id)
                .subscribe(permissionedObserver()))
    }

    private fun permissionedObserver(): JustObserver<HashMap<String, Channel>> {
        return object : JustObserver<HashMap<String, Channel>>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(channels: HashMap<String, Channel>) {
                adapter.updateChannels(channels)
            }

        }
    }

    private fun syncUsersContacts() {
        val channels = loggedInUser.channels
        if (channels.size > 0) {
            adapter.updateChannels(channels)
        } else {
            recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
        }
    }

    private fun addUserChannel(event: UserChannelAddedEventCallback) {
        if (event.oldChannel != null) {
            adapter.updateItem(event.oldChannel, event.newChannel)
        } else {
            adapter.addItem(event.newChannel)
        }
    }

    private fun deleteUserChannel(event: UserChannelDeletedEventCallback) {
        adapter.removeItem(event.position)
    }

}
