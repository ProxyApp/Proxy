package com.shareyourproxy.app.fragment

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.command.AddUserGroupCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupAdapter

import java.util.HashMap

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.ButterKnife
import butterknife.OnClick
import rx.subscriptions.CompositeSubscription

import android.support.design.widget.Snackbar.LENGTH_LONG
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_DELETED_GROUP
import com.shareyourproxy.Constants.ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED
import com.shareyourproxy.IntentLauncher.launchEditGroupChannelsActivity
import com.shareyourproxy.api.domain.model.Group.PUBLIC
import com.shareyourproxy.api.domain.model.Group.createBlank
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.ADD_GROUP
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.EDIT_GROUP
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.PUBLIC_GROUP
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.MAIN_GROUPS

/**
 * Displaying a list of [User] [Group]s.
 */
class MainGroupFragment : BaseFragment(), ItemClickListener {
    @Bind(R.id.fragment_group_main_coordinator)
    internal var coordinatorLayout: CoordinatorLayout
    @Bind(R.id.fragment_group_main_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.fragment_group_main_fab)
    internal var floatingActionButton: FloatingActionButton
    @Bind(R.id.fragment_group_main_swipe_refresh)
    internal var swipeRefreshLayout: SwipeRefreshLayout
    @Bind(R.id.fragment_group_main_empty_textview)
    internal var emptyTextView: TextView
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    @BindColor(android.R.color.white)
    internal var colorWhite: Int = 0
    @BindDimen(R.dimen.common_svg_large)
    internal var marginSVGLarge: Int = 0
    internal var _refreshListener: SwipeRefreshLayout.OnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val user = loggedInUser
        if (user != null) {
            rxBus.post(SyncContactsCommand(user))
        }
    }
    private var _adapter: GroupAdapter? = null
    private var _subscriptions: CompositeSubscription? = null

    /**
     * Prompt user with a [EditGroupChannelsFragment] to add a new [Group].
     */
    @OnClick(R.id.fragment_group_main_fab)
    fun onClick() {
        launchEditGroupChannelsActivity(activity, Companion.createBlank(), ADD_GROUP)
    }

    /**
     * Check if there was a group deleted from the [(View, int)][EditGroupChannelsFragment.onItemClick])}

     * @param activity to get intent data from
     */
    fun checkGroupDeleted(activity: Activity) {
        val groupDeleted = activity.intent.extras.getBoolean(
                ARG_MAINGROUPFRAGMENT_WAS_GROUP_DELETED, false)
        if (groupDeleted) {
            showUndoDeleteSnackBar(activity.intent.extras.getParcelable<Parcelable>(
                    ARG_MAINGROUPFRAGMENT_DELETED_GROUP) as Group?)
        }
    }

    /**
     * Build a [Snackbar] and show it. This [Snackbar] reverses the action of deleting a [User] [Group].

     * @param group deleted
     */
    private fun showUndoDeleteSnackBar(group: Group) {
        val snackbar = Snackbar.make(coordinatorLayout, getString(R.string.undo_delete),
                Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(getString(R.string.undo), onClickUndoDelete(group))
        snackbar.setActionTextColor(colorBlue)
        snackbar.show()
    }

    /**
     * Snackbar action button event logic.

     * @param group to add
     * *
     * @return click listener
     */
    private fun onClickUndoDelete(group: Group): View.OnClickListener {
        return View.OnClickListener { rxBus.post(AddUserGroupCommand(loggedInUser, group)) }
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_main_group, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    /**
     * Initialize this fragments UI.
     */
    fun initialize() {
        initializeFab()
        initializeRecyclerView()
        initializeSwipeRefresh(swipeRefreshLayout, _refreshListener)
        checkGroupDeleted(activity)
    }

    /**
     * Set the content image of this fragment's [FloatingActionButton]
     */
    private fun initializeFab() {
        val drawable = svgToBitmapDrawable(activity, R.raw.ic_add, marginSVGLarge, colorWhite)
        floatingActionButton.setImageDrawable(drawable)
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = CompositeSubscription()
        _subscriptions!!.add(rxBus.toObservable().subscribe(busObserver))
        _adapter!!.refreshGroupData(loggedInUser.groups())
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is UserGroupAddedEventCallback) {
                    addGroups(event)
                } else if (event is GroupChannelsUpdatedEventCallback) {
                    updateGroup(event)
                } else if (event is LoggedInUserUpdatedEventCallback) {
                    updateGroups(event.user.groups())
                } else if (event is SyncContactsCommand) {
                    swipeRefreshLayout.isRefreshing = true
                } else if (event is SyncAllContactsSuccessEvent) {
                    swipeRefreshLayout.isRefreshing = false
                } else if (event is SyncAllContactsErrorEvent) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

    fun addGroups(event: UserGroupAddedEventCallback) {
        val group = event.group
        if (group.id() != null) {
            addGroup(group)
        }
    }

    /**
     * Add a new group.

     * @param group to add
     */
    fun addGroup(group: Group) {
        _adapter!!.addItem(group)
        showAddedGroupSnackBar()
    }

    /**
     * Add a new group.

     * @param event group to add
     */
    fun updateGroup(event: GroupChannelsUpdatedEventCallback) {
        _adapter!!.updateItem(event.oldGroup, event.group)
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
    fun updateGroups(groups: HashMap<String, Group>) {
        _adapter!!.refreshGroupData(groups)
    }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.isRefreshing = false
    }

    /**
     * Initialize this fragments [Group] data and [BaseRecyclerView].
     */
    private fun initializeRecyclerView() {
        val showHeader = !sharedPreferences.getBoolean(MAIN_GROUPS.key, false)
        _adapter = GroupAdapter.newInstance(recyclerView, sharedPreferences, showHeader, this)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = _adapter
    }

    override fun onItemClick(view: View, position: Int) {
        val group = _adapter!!.getItemData(position)
        if (group.id().equals(PUBLIC)) {
            launchEditGroupChannelsActivity(activity, group, PUBLIC_GROUP)
        } else {
            launchEditGroupChannelsActivity(activity, group, EDIT_GROUP)
        }
    }

    companion object {

        /**
         * Get a new Instance of this [MainGroupFragment].

         * @return [MainGroupFragment]
         */
        fun newInstance(): MainGroupFragment {
            return MainGroupFragment()
        }
    }

}
/**
 * [Fragment] Constructor.
 */
