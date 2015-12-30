package com.shareyourproxy.app.fragment

import android.R.id.home
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.R
import com.shareyourproxy.R.id.menu_edit_group_channel_save
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.command.DeleteUserGroupCommand
import com.shareyourproxy.api.rx.command.SaveGroupChannelsCommand
import com.shareyourproxy.api.rx.command.SavePublicGroupChannelsCommand
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter.Companion.TYPE_LIST_DELETE_FOOTER
import com.shareyourproxy.util.bindView
import timber.log.Timber

/**
 * Display a list of [Group] [Channel]s and whether they are in our out of the selected groups permissions.
 */
class EditGroupChannelsFragment : BaseFragment(), ItemClickListener {

    /**
     * Check whether this fragment is Adding a group, Editing a group, or a Public group.
     * @return [GroupEditType] constants.
     */
    private val groupEditType: GroupEditType = activity.intent.extras.getSerializable(ARG_EDIT_GROUP_TYPE) as GroupEditType
    private val recyclerView: BaseRecyclerView by bindView(R.id.fragment_group_edit_channel_recyclerview)
    private val selectedGroup: Group = activity.intent.extras.getParcelable<Group>(ARG_SELECTED_GROUP)
    private val adapter: EditGroupChannelAdapter = EditGroupChannelAdapter(recyclerView, this, selectedGroup.label, loggedInUser.channels, selectedGroup.channels, groupEditType)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_group_channel, container, false)
        setHasOptionsMenu(true)
        initializeRecyclerView()
        return rootView
    }

    override fun onItemClick(view: View, position: Int) {
        val viewType = adapter.getItemViewType(position)
        if (viewType == TYPE_LIST_DELETE_FOOTER) {
            post(DeleteUserGroupCommand(loggedInUser, selectedGroup))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            home -> activity.onBackPressed()
            menu_edit_group_channel_save -> savePressed()
            else -> Timber.e("Option item selected is unknown")
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Save and go back.
     * @param groupLabel updated group name
     */
    private fun saveGroupChannels(groupLabel: String) {
        post(SaveGroupChannelsCommand(loggedInUser, groupLabel, selectedGroup, adapter.selectedChannels, groupEditType))
        activity.onBackPressed()
    }

    /**
     * Save public channels and go back.
     */
    private fun savePublicGroupChannels() {
        post(SavePublicGroupChannelsCommand(loggedInUser, adapter.toggledChannels))
        activity.onBackPressed()
    }

    /**
     * Initialize the channel and group data.
     */
    private fun initializeRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(dismissScrollListener)
    }

    private fun savePressed() {
        if (GroupEditType.PUBLIC_GROUP == groupEditType) {
            savePublicGroupChannels()
        } else {
            if (adapter.groupLabel.trim { it <= ' ' }.isEmpty()) {
                adapter.promptGroupLabelError(activity)
            } else {
                saveGroupChannels(adapter.groupLabel)
            }
        }
    }
}
