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
import android.support.v4.content.ContextCompat
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
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxBusDriver.rxBusObservable
import com.shareyourproxy.api.rx.command.AddUserGroupCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType.*
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.GroupAdapter
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.MAIN_GROUPS
import org.jetbrains.anko.onClick
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Displaying a list of [User] [Group]s.
 */
class MainGroupFragment : BaseFragment(), ItemClickListener {
    private val coordinatorLayout: CoordinatorLayout by bindView(R.id.fragment_group_main_coordinator)
    private val recyclerView: BaseRecyclerView by bindView(fragment_group_main_recyclerview)
    private val floatingActionButton: FloatingActionButton by bindView(fragment_group_main_fab)
    private val swipeRefreshLayout: SwipeRefreshLayout by bindView(fragment_group_main_swipe_refresh)
    private val emptyTextView: TextView by bindView(R.id.fragment_group_main_empty_textview)
    internal var colorBlue: Int = ContextCompat.getColor(context, common_blue)
    internal var colorWhite: Int = ContextCompat.getColor(context, white)
    internal var marginSVGLarge: Int = resources.getDimensionPixelSize(common_svg_large)
    internal val refreshListener: SwipeRefreshLayout.OnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        post(SyncContactsCommand(loggedInUser))
    }
    private val showHeader = !sharedPreferences.getBoolean(MAIN_GROUPS.key, false)
    private var adapter: GroupAdapter = GroupAdapter.newInstance(recyclerView, sharedPreferences, showHeader, this)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    /**
     * Prompt user with a [EditGroupChannelsFragment] to add a new [Group].
     */
    private val onClickFab :View.OnClickListener = View.OnClickListener {
        launchEditGroupChannelsActivity(activity, GroupFactory.createBlankGroup(), ADD_GROUP)
    }

    /**
     * Check if there was a group deleted from the [(View, int)][EditGroupChannelsFragment.onItemClick])}

     * @param activity to get intent data from
     */
    fun checkGroupDeleted(activity: Activity) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(fragment_main_group, container, false)
        initialize()
        return rootView
    }

    /**
     * Initialize this fragments UI.
     */
    fun initialize() {
        initializeFab()
        initializeRecyclerView()
        initializeSwipeRefresh(swipeRefreshLayout, refreshListener)
        checkGroupDeleted(activity)
    }

    /**
     * Set the content image of this fragment's [FloatingActionButton]
     */
    private fun initializeFab() {
        val drawable = svgToBitmapDrawable(activity, R.raw.ic_add, marginSVGLarge, colorWhite)
        floatingActionButton.setImageDrawable(drawable)
        floatingActionButton.onClick{onClickFab}
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(rxBusObservable().subscribe(busObserver))
        adapter.refreshGroupData(loggedInUser.groups)
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is UserGroupAddedEventCallback) {
                    addGroups(event)
                } else if (event is GroupChannelsUpdatedEventCallback) {
                    updateGroup(event)
                } else if (event is LoggedInUserUpdatedEventCallback) {
                    updateGroups(event.user.groups)
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
        addGroup(event.group)
    }

    /**
     * Add a new group.

     * @param group to add
     */
    fun addGroup(group: Group) {
        adapter.addItem(group)
        showAddedGroupSnackBar()
    }

    /**
     * Add a new group.

     * @param event group to add
     */
    fun updateGroup(event: GroupChannelsUpdatedEventCallback) {
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
    fun updateGroups(groups: HashMap<String, Group>) {
        adapter.refreshGroupData(groups)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.isRefreshing = false
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

    override fun onItemClick(view: View, position: Int) {
        val group = adapter!!.getItemData(position)
        if (group.id.equals(PUBLIC)) {
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
