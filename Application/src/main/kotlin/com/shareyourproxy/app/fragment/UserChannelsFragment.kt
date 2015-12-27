package com.shareyourproxy.app.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat.getColor
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
import butterknife.bindView
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchChannelListActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.dimen.common_svg_null_screen_mini
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_ghost_doge
import com.shareyourproxy.R.string.*
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body2
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.RxBusDriver.rxBusObservable
import com.shareyourproxy.api.rx.RxQuery.queryPermissionedChannels
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.app.adapter.ViewChannelAdapter
import com.shareyourproxy.app.dialog.EditChannelDialog
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * A User's channels
 */
class UserChannelsFragment : BaseFragment(), ItemLongClickListener {
    private val recyclerView: BaseRecyclerView by bindView(fragment_user_channel_recyclerview)
    private val emptyViewContainer: LinearLayout by bindView(fragment_user_channel_empty_view_container)
    private val addChannelButton: Button by bindView(fragment_user_channel_empty_button)
    private val emptyTextView: TextView by bindView(fragment_user_channel_empty_textview)
    internal var loggedInNullTitle: String = resources.getString(fragment_userchannels_empty_title)
    internal var loggedInNullMessage: String = resources.getString(fragment_userchannels_empty_message)
    internal var contactNullTitle: String = resources.getString(fragment_userprofile_contact_empty_title)
    internal var marginNullScreen: Int = resources.getDimensionPixelSize(common_svg_null_screen_mini)
    internal var colorBlue: Int = getColor(context, R.color.common_blue)
    private var userContact: User = arguments.getParcelable<User>(ARG_USER_SELECTED_PROFILE)
    private var isLoggedInUser: Boolean = isLoggedInUser(userContact)
    private var adapter: ViewChannelAdapter = ViewChannelAdapter.newInstance(recyclerView, sharedPreferences, isShowHeader(if (isLoggedInUser) loggedInUser.channels else null), this)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    private val onClickAddChannel: View.OnClickListener = OnClickListener {
        launchChannelListActivity(activity)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_channels, container, false)
        initialize()
        return rootView
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
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, getNullDrawable(ic_ghost_doge), null, null)


        } else {
            val contactNullMessage = getString(fragment_userprofile_contact_empty_message, userContact.first)
            val sb = SpannableStringBuilder(contactNullTitle).append("\n").append(contactNullMessage)

            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body2), 0, contactNullTitle.length, SPAN_INCLUSIVE_INCLUSIVE)
            sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body), contactNullTitle.length + 1, sb.length, SPAN_INCLUSIVE_INCLUSIVE)

            emptyTextView.text = sb
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, getNullDrawable(R.raw.ic_ghost_sloth), null, null)
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
        subscriptions.add(queryPermissionedChannels(userContact, loggedInUser.id)
                .subscribe(permissionedObserver()))
    }

    private fun permissionedObserver(): JustObserver<HashMap<String, Channel>> {
        return object : JustObserver<HashMap<String, Channel>>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(channels: HashMap<String, Channel>?) {
                adapter.updateChannels(channels)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        subscriptions = CompositeSubscription()
        subscriptions.add(rxBusObservable().subscribe(onNextEvent()))
        if (isLoggedInUser) {
            syncUsersContacts()
        } else {
            getSharedChannels()
        }
        recyclerView.scrollToPosition(0)
    }

    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
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
    }

    fun syncUsersContacts() {
        val channels = loggedInUser.channels
        if (channels.size > 0) {
            adapter.updateChannels(channels)
        } else {
            recyclerView.updateViewState(RecyclerViewDatasetChangedEvent(adapter, EMPTY))
        }
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
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

    override fun onItemClick(view: View, position: Int) {
        val channel = adapter.getItemData(position)
        RxBusDriver.post(SelectUserChannelEvent(channel))
    }

    override fun onItemLongClick(view: View, position: Int) {
        val channel = adapter.getItemData(position)
        if (isLoggedInUser) {
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
