package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.command.DeleteUserGroupCommand
import com.shareyourproxy.api.rx.command.SaveGroupChannelsCommand
import com.shareyourproxy.api.rx.command.SavePublicGroupChannelsCommand
import com.shareyourproxy.app.EditGroupChannelsActivity
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter

import butterknife.Bind
import butterknife.ButterKnife
import timber.log.Timber

import com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter.TYPE_LIST_DELETE_FOOTER

/**
 * Display a list of [Group] [Channel]s and whether they are in our out of the selected groups permissions.
 */
class EditGroupChannelsFragment : BaseFragment(), ItemClickListener {

    @Bind(R.id.fragment_group_edit_channel_recyclerview)
    internal var recyclerView: RecyclerView

    private var _adapter: EditGroupChannelAdapter? = null

    private val selectedGroup: Group
        get() = activity.intent.extras.getParcelable<Group>(ARG_SELECTED_GROUP)

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, state: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_edit_group_channel, container, false)
        ButterKnife.bind(this, rootView)
        setHasOptionsMenu(true)
        initializeRecyclerView()
        return rootView
    }

    /**
     * Save and go back.

     * @param groupLabel updated group name
     */
    private fun saveGroupChannels(groupLabel: String) {
        rxBus.post(SaveGroupChannelsCommand(loggedInUser, groupLabel,
                selectedGroup, _adapter!!.selectedChannels, groupEditType))
        activity.onBackPressed()
    }

    /**
     * Save public channels and go back.
     */
    private fun savePublicGroupChannels() {
        rxBus.post(SavePublicGroupChannelsCommand(
                loggedInUser, _adapter!!.toggledChannels))
        activity.onBackPressed()
    }

    /**
     * Initialize the channel and group data.
     */
    private fun initializeRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        _adapter = EditGroupChannelAdapter.newInstance(this, selectedGroup.label(),
                loggedInUser.channels(), selectedGroup.channels(), groupEditType)
        recyclerView.adapter = _adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(dismissScrollListener)
    }

    /**
     * Check whether this fragment is Adding a group, Editing a group, or a Public group.

     * @return [GroupEditType] constants.
     */
    private val groupEditType: GroupEditType
        get() = activity.intent.extras.getSerializable(ARG_EDIT_GROUP_TYPE) as GroupEditType?

    override fun onItemClick(view: View, position: Int) {
        val viewType = _adapter!!.getItemViewType(position)
        if (viewType == TYPE_LIST_DELETE_FOOTER) {
            rxBus.post(DeleteUserGroupCommand(loggedInUser, selectedGroup))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> activity.onBackPressed()
            R.id.menu_edit_group_channel_save -> savePressed()
            else -> Timber.e("Option item selected is unknown")
        }
        return super.onOptionsItemSelected(item)
    }

    fun savePressed() {
        if (GroupEditType.PUBLIC_GROUP == groupEditType) {
            savePublicGroupChannels()
        } else {
            if (_adapter!!.groupLabel.trim { it <= ' ' }.isEmpty()) {
                _adapter!!.promptGroupLabelError(activity)
            } else {
                saveGroupChannels(_adapter!!.groupLabel)
            }
        }
    }

    companion object {

        /**
         * Create a new instance of this fragment for the parent [EditGroupChannelsActivity].

         * @return EditGroupChannelsFragment
         */
        fun newInstance(): EditGroupChannelsFragment {
            return EditGroupChannelsFragment()
        }
    }

}
/**
 * Constructor.
 */
