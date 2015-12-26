package com.shareyourproxy.app.fragment

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.shareyourproxy.IntentLauncher
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxQuery
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.api.rx.event.UserSelectedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupContactsAdapter
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder
import com.shareyourproxy.widget.ContentDescriptionDrawable

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindString
import butterknife.ButterKnife
import rx.subscriptions.CompositeSubscription

import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.util.ObjectUtils.capitalize
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Display the [User] contacts added to the selected [Group].
 */
class GroupContactsFragment : BaseFragment(), ItemClickListener {
    private val _rxQuery = RxQuery
    @Bind(R.id.fragment_contacts_group_toolbar)
    internal var toolbar: Toolbar
    @Bind(R.id.fragment_contacts_group_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_contacts_group_empty_textview)
    internal var emptyTextView: TextView
    @BindString(R.string.fragment_contact_group_empty_title)
    internal var emptyTextTitle: String
    @BindString(R.string.fragment_contact_group_empty_message)
    internal var emptyTextMessage: String
    @BindDimen(R.dimen.common_svg_null_screen_small)
    internal var marginNullScreen: Int = 0
    @BindColor(android.R.color.white)
    internal var colorWhite: Int = 0
    @BindDimen(R.dimen.common_svg_large)
    internal var marginSVGLarge: Int = 0
    private var _adapter: GroupContactsAdapter? = null
    private var _subscriptions: CompositeSubscription? = null

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_view_group_users, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = CompositeSubscription()
        _subscriptions!!.add(rxBus.toObservable().subscribe(busObserver))
        _adapter!!.refreshData(_rxQuery.queryUserContacts(
                activity, groupArg.contacts()).values)
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            fun next(event: Any) {
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
        supportActionBar.setTitle(Companion.capitalize(groupArg.label()))
    }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    /**
     * User selected from this groups contacts. Open that Users profile.

     * @param event data
     */
    fun onUserSelected(event: UserSelectedEvent) {
        launchUserProfileActivity(activity, event.user,
                loggedInUser.id(), event.imageView, event.textView)
    }

    /**
     * Get the group selected and bundled in this activities [IntentLauncher.launchEditGroupContactsActivity] call.

     * @return selected group
     */
    private val groupArg: Group
        get() = activity.intent.extras.getParcelable<Parcelable>(ARG_SELECTED_GROUP)

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        initializeRecyclerView()
        buildToolbar(toolbar, Companion.capitalize(groupArg.label()), null)
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private fun initializeRecyclerView() {
        initializeEmptyView()
        _adapter = GroupContactsAdapter.newInstance(recyclerView, this)

        recyclerView.setEmptyView(emptyTextView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = _adapter
    }

    private fun initializeEmptyView() {
        val context = context
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, fishDrawable, null, null)
        val sb = SpannableStringBuilder(emptyTextTitle).append("\n").append(emptyTextMessage)

        sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
                0, emptyTextTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
                emptyTextTitle.length + 1, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        emptyTextView.text = sb
    }

    /**
     * Parse a svg and return a null screen sized [ContentDescriptionDrawable] .

     * @return Drawable with a contentDescription
     */
    private val fishDrawable: Drawable
        get() = svgToBitmapDrawable(activity, R.raw.ic_fish, marginNullScreen)

    override fun onItemClick(view: View, position: Int) {
        val holder = recyclerView.getChildViewHolder(view) as UserViewHolder
        rxBus.post(UserSelectedEvent(
                holder.userImage, holder.userName, _adapter!!.getItemData(position)))
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
/**
 * Constructor.
 */
