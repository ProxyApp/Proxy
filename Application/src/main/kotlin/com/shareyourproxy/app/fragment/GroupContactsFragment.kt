package com.shareyourproxy.app.fragment

import android.R.color.white
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat.getColor
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
import com.shareyourproxy.R.id.fragment_contacts_group_toolbar
import com.shareyourproxy.R.raw.ic_fish
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body2
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxQuery.queryUserContacts
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupContactsAdapter
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder
import com.shareyourproxy.util.ObjectUtils.capitalize
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import rx.subscriptions.CompositeSubscription

/**
 * Display the [User] contacts added to the selected [Group].
 */
class GroupContactsFragment : BaseFragment(), ItemClickListener {
    private val toolbar: Toolbar by bindView(fragment_contacts_group_toolbar)
    private val recyclerView: BaseRecyclerView by bindView(R.id.fragment_contacts_group_recyclerview)
    private val emptyTextView: TextView by bindView(R.id.fragment_contacts_group_empty_textview)
    internal var emptyTextTitle: String = getString(R.string.fragment_contact_group_empty_title)
    internal var emptyTextMessage: String = getString(R.string.fragment_contact_group_empty_message)
    internal var marginNullScreen: Int = resources.getDimensionPixelSize(common_svg_null_screen_small)
    internal var colorWhite: Int = getColor(context, white)
    private var adapter: GroupContactsAdapter =GroupContactsAdapter.newInstance(recyclerView, this)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_view_group_users, container, false)
        initialize()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusDriver.rxBusObservable().subscribe(busObserver))
        adapter.refreshData(queryUserContacts(activity, groupArg.contacts).values)
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is UserSelectedEvent) {
                    onUserSelected(event)
                } else if (event is GroupChannelsUpdatedEventCallback) {
                    channelsUpdated(event)
                } else if (event is RecyclerViewDatasetChangedEvent) {
                    recyclerView.updateViewState(event)
                }
            }
        }

    /**
     * A Group has been edited in [EditGroupChannelsFragment]. Update this fragments intent data and title.

     * @param event group data
     */
    private fun channelsUpdated(event: GroupChannelsUpdatedEventCallback) {
        activity.intent.putExtra(ARG_SELECTED_GROUP, event.group)
        supportActionBar.title = capitalize(groupArg.label)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    /**
     * User selected from this groups contacts. Open that Users profile.

     * @param event data
     */
    fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user, loggedInUser.id, event.imageView, event.textView)
    }

    /**
     * Get the group selected and bundled in this activities [IntentLauncher.launchEditGroupContactsActivity] call.
     * @return selected group
     */
    private val groupArg: Group get() = activity.intent.extras.getParcelable<Parcelable>(ARG_SELECTED_GROUP) as Group

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

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .
     * @return Drawable with a contentDescription
     */
    private val fishDrawable: Drawable get() = svgToBitmapDrawable(activity, ic_fish, marginNullScreen)

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        post(UserSelectedEvent(holder.userImage, holder.userName, adapter.getItemData(position)))
    }

    companion object {

        /**
         * Return new Fragment instance.
         * @return GroupContactsFragment
         */
        fun newInstance(): GroupContactsFragment {
            return GroupContactsFragment()
        }
    }

}
