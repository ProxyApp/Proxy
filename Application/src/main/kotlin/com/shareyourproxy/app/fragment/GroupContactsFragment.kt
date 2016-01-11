package com.shareyourproxy.app.fragment

import android.R.color.white
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.dimen.common_svg_null_screen_small
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_fish
import com.shareyourproxy.R.string.fragment_contact_group_empty_message
import com.shareyourproxy.R.string.fragment_contact_group_empty_title
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body2
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxQuery.queryUserContacts
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupContactsAdapter
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.StringUtils.capitalize
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import rx.subscriptions.CompositeSubscription

/**
 * Display the [User] contacts added to the selected [Group].
 */
internal final class GroupContactsFragment() : BaseFragment(), ItemClickListener {
    /**
     * Get the group selected and bundled in this activities [IntentLauncher.launchEditGroupContactsActivity] call.
     * @return selected group
     */
    private val groupArg: Group = activity.intent.extras.getParcelable<Parcelable>(ARG_SELECTED_GROUP) as Group
    private val toolbar: Toolbar by bindView(fragment_contacts_group_toolbar)
    private val recyclerView: BaseRecyclerView by bindView(fragment_contacts_group_recyclerview)
    private val emptyTextView: TextView by bindView(fragment_contacts_group_empty_textview)
    private val emptyTextTitle: String by bindString(fragment_contact_group_empty_title)
    private val emptyTextMessage: String by bindString(fragment_contact_group_empty_message)
    private val marginNullScreen: Int by bindDimen(common_svg_null_screen_small)
    private val colorWhite: Int by bindColor(white)
    private val adapter: GroupContactsAdapter =GroupContactsAdapter(recyclerView, this)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val fishDrawable: Drawable = svgToBitmapDrawable(activity, ic_fish, marginNullScreen)
    private val busObserver: JustObserver<Any> = object : JustObserver<Any>(GroupContactsFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is UserSelectedEvent) {
                onUserSelected(event)
            } else if (event is GroupChannelsUpdatedEventCallback) {
                channelsUpdated(event)
            } else if (event is RecyclerViewDatasetChangedEvent) {
                recyclerView.updateViewState(event)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_group_users, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(busObserver))
        adapter.refreshData(queryUserContacts(activity, groupArg.contacts).values)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        post(UserSelectedEvent(holder.userImage, holder.userName, adapter.getItemData(position)))
    }

    /**
     * A Group has been edited in [EditGroupChannelsFragment]. Update this fragments intent data and title.
     * @param event group data
     */
    private fun channelsUpdated(event: GroupChannelsUpdatedEventCallback) {
        activity.intent.putExtra(ARG_SELECTED_GROUP, event.group)
        supportActionBar.title = capitalize(groupArg.label)
    }

    /**
     * User selected from this groups contacts. Open that Users profile.

     * @param event data
     */
    private fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id, event.imageView, event.textView)
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        initializeRecyclerView()
        buildToolbar(toolbar, capitalize(groupArg.label), null)
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        initializeEmptyView()
        recyclerView.setEmptyView(emptyTextView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun initializeEmptyView() {
        val context = context
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, fishDrawable, null, null)
        val sb = SpannableStringBuilder(emptyTextTitle).append("\n").append(emptyTextMessage)

        sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body2), 0, emptyTextTitle.length, SPAN_INCLUSIVE_INCLUSIVE)
        sb.setSpan(TextAppearanceSpan(context, Proxy_TextAppearance_Body), emptyTextTitle.length + 1, sb.length, SPAN_INCLUSIVE_INCLUSIVE)
        emptyTextView.text = sb
    }
}
