package com.shareyourproxy.app.fragment

import android.R.color.white
import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED
import com.shareyourproxy.IntentLauncher.launchEditGroupChannelsActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.dimen.common_svg_large
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.layout.fragment_main_group
import com.shareyourproxy.R.string.undo_delete
import com.shareyourproxy.api.domain.factory.GroupFactory
import com.shareyourproxy.api.domain.factory.GroupFactory.PUBLIC
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.command.AddUserGroupCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback
import com.shareyourproxy.api.rx.event.SyncContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncContactsSuccessEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupAdapter
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.GroupEditType.*
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.MAIN_GROUPS
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Displaying a list of [User] [Group]s.
 */
internal final class MainGroupFragment() : BaseFragment(), ItemClickListener {
    private val coordinatorLayout: CoordinatorLayout by bindView(R.id.fragment_group_main_coordinator)
    private val recyclerView: BaseRecyclerView by bindView(fragment_group_main_recyclerview)
    private val floatingActionButton: FloatingActionButton by bindView(fragment_group_main_fab)
    private val swipeRefreshLayout: SwipeRefreshLayout by bindView(fragment_group_main_swipe_refresh)
    private val emptyTextView: TextView by bindView(R.id.fragment_group_main_empty_textview)
    private val colorBlue: Int by bindColor(common_blue)
    private val colorWhite: Int by bindColor(white)
    private val marginSVGLarge: Int by bindDimen(common_svg_large)
    private val refreshListener: SwipeRefreshLayout.OnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        post(SyncContactsCommand(loggedInUser))
    }
    private val showHeader = !sharedPreferences.getBoolean(MAIN_GROUPS.key, false)
    private val adapter: GroupAdapter = GroupAdapter(recyclerView, sharedPreferences, showHeader, this)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    /**
     * Prompt user with a [EditGroupChannelsFragment] to add a new [Group].
     */
    private val onClickFab :View.OnClickListener = View.OnClickListener {
        launchEditGroupChannelsActivity(activity, GroupFactory.createBlankGroup(),ADD_GROUP)
    }
    private val busObserver: JustObserver<Any> = object : JustObserver<Any>(MainGroupFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is UserGroupAddedEventCallback) {
                addGroups(event)
            } else if (event is GroupChannelsUpdatedEventCallback) {
                updateGroup(event)
            } else if (event is LoggedInUserUpdatedEventCallback) {
                updateGroups(event.user.groups)
            } else if (event is SyncContactsCommand) {
                swipeRefreshLayout.isRefreshing = true
            } else if (event is SyncContactsSuccessEvent) {
                swipeRefreshLayout.isRefreshing = false
            } else if (event is SyncContactsErrorEvent) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(fragment_main_group, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(busObserver))
        adapter.refreshGroupData(loggedInUser.groups)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onItemClick(view: View, position: Int) {
        val group = adapter.getItemData(position)
        if (group.id.equals(PUBLIC)) {
            launchEditGroupChannelsActivity(activity, group, PUBLIC_GROUP)
        } else {
            launchEditGroupChannelsActivity(activity, group, EDIT_GROUP)
        }
    }

    /**
     * Initialize this fragments UI.
     */
    private fun initialize() {
        initializeFab()
        initializeRecyclerView()
        initializeSwipeRefresh(swipeRefreshLayout, refreshListener)
        checkGroupDeleted(activity)
    }

    /**
     * Check if there was a group deleted from the [(View, int)][EditGroupChannelsFragment.onItemClick])}
     * @param activity to get intent data from
     */
    private fun checkGroupDeleted(activity: Activity) {
        val groupDeleted = activity.intent.extras.getBoolean(ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, false)
        if (groupDeleted) {
            showUndoDeleteSnackBar(activity.intent.extras.getParcelable<Parcelable>(ARG_MAINGROUPFRAGMENT_DELETED_GROUP) as Group)
        }
    }

    /**
     * Build a [Snackbar] and show it. This [Snackbar] reverses the action of deleting a [User] [Group].

     * @param group deleted
     */
    private fun showUndoDeleteSnackBar(group: Group) {
        val snackbar = Snackbar.make(coordinatorLayout, getString(undo_delete), LENGTH_INDEFINITE)
        snackbar.setAction(getString(R.string.undo), onClickUndoDelete(group))
        snackbar.setActionTextColor(colorBlue)
        snackbar.show()
    }

    /**
     * Snackbar action button event logic.
     * @param group to add
     * @return click listener
     */
    private fun onClickUndoDelete(group: Group): View.OnClickListener {
        return View.OnClickListener { post(AddUserGroupCommand(loggedInUser, group)) }
    }

    /**
     * Set the content image of this fragment's [FloatingActionButton]
     */
    private fun initializeFab() {
        val drawable = svgToBitmapDrawable(activity, R.raw.ic_add, marginSVGLarge, colorWhite)
        floatingActionButton.setImageDrawable(drawable)
        floatingActionButton.setOnClickListener(onClickFab)
    }

    private fun addGroups(event: UserGroupAddedEventCallback) {
        addGroup(event.group)
    }

    /**
     * Add a new group.

     * @param group to add
     */
    private fun addGroup(group: Group) {
        adapter.addItem(group)
        showAddedGroupSnackBar()
    }

    /**
     * Add a new group.

     * @param event group to add
     */
    private fun updateGroup(event: GroupChannelsUpdatedEventCallback) {
        adapter.updateItem(event.oldGroup, event.group)
        if (event.groupEditType == ADD_GROUP) {
            showAddedGroupSnackBar()
        } else {
            showChangesSavedSnackBar(coordinatorLayout)
        }
    }

    private fun showAddedGroupSnackBar() {
        Snackbar.make(coordinatorLayout, getString(R.string.group_added), LENGTH_LONG).show()
    }

    /**
     * update all groups.

     * @param groups to add
     */
    private fun updateGroups(groups: HashMap<String, Group>) {
        adapter.refreshGroupData(groups)
    }

    /**
     * Initialize this fragments [Group] data and [BaseRecyclerView].
     */
    private fun initializeRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }
}

