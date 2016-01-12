package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import com.shareyourproxy.Constants
import com.shareyourproxy.R.dimen.fragment_userprofile_header_contact_background_size
import com.shareyourproxy.R.drawable.selector_button_blue
import com.shareyourproxy.R.drawable.selector_button_grey
import com.shareyourproxy.R.id.fragment_user_profile_header_button
import com.shareyourproxy.R.id.fragment_user_profile_toolbar
import com.shareyourproxy.R.raw.ic_groups
import com.shareyourproxy.R.string.add_to_group
import com.shareyourproxy.R.string.in_blank_groups
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.RxQuery.queryContactGroups
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback
import com.shareyourproxy.app.dialog.UserGroupsDialog
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.getMenuIcon
import java.util.*

/**
 * Display a contacts profile and channels.
 */
internal final class ContactProfileFragment private constructor(user: User, loggedInUserId: String) : UserProfileFragment() {
    companion object {
        fun create(user: User, loggedInUserId: String): ContactProfileFragment {
            val fragment = ContactProfileFragment(user, loggedInUserId)
            val args: Bundle = Bundle()
            args.putParcelable(Constants.ARG_USER_SELECTED_PROFILE, user)
            args.putString(Constants.ARG_LOGGEDIN_USER_ID, loggedInUserId)
            fragment.arguments = args
            return fragment
        }
    }

    private val groupButton: Button by bindView(fragment_user_profile_header_button)
    private val marginContactHeight: Int by bindDimen(fragment_userprofile_header_contact_background_size)
    private val toggleGroups = ArrayList<GroupToggle>()
    private val toolbar: Toolbar by bindView(fragment_user_profile_toolbar)
    private val onClickGroup: View.OnClickListener = View.OnClickListener {
        UserGroupsDialog(toggleGroups, contact).show(fragmentManager)
    }
    private val onNextEvent = object : JustObserver<Any>(ContactProfileFragment::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is GroupContactsUpdatedEventCallback) {
                groupContactsUpdatedEvent(event)
            }
        }
    }
    private val groupEditContacts: List<Group> get() {
        toggleGroups.clear()
        val list = queryContactGroups(loggedInUser, contact)
        toggleGroups.addAll(list)
        val selectedGroupsList = ArrayList<Group>(list.size)
        list.forEach {
            if (it.isChecked) {
                selectedGroupsList.add(it.group)
            }
        }
        return selectedGroupsList
    }

    override fun onCreateView(rootView: View) {
        super.onCreateView(rootView)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        rxBusObservable().subscribe(onNextEvent)
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        setHeaderHeight()
        setToolbarTitle()
        initializeHeader()
        initializeGroupButton()
    }

    private fun setHeaderHeight() {
        val lp = collapsingToolbarLayout.layoutParams
        lp.height = marginContactHeight
        collapsingToolbarLayout.layoutParams = lp
    }

    private fun setToolbarTitle() {
        val title = contact.fullName
        buildToolbar(toolbar, title, null)
    }

    /**
     * Initialize the Header view data and state.
     */
    private fun initializeGroupButton() {
        groupButton.visibility = VISIBLE
        groupButton.setOnClickListener(onClickGroup)
        groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(getMenuIcon(activity, ic_groups), null, null, null)
        updateGroupButtonText(groupEditContacts)
    }

    private fun groupContactsUpdatedEvent(event: GroupContactsUpdatedEventCallback) {
        updateGroupButtonText(event.contactGroups)
    }

    @SuppressWarnings("unchecked")
    private fun updateGroupButtonText(list: List<Group>?) {
        if (list != null) {
            when (list.size) {
                0 -> updateGroupButtonText(getString(add_to_group), selector_button_blue)
                1 -> updateGroupButtonText(list[0].label, selector_button_grey)
                else -> updateGroupButtonText(getString(in_blank_groups, list.size), selector_button_grey)
            }
        } else {
            updateGroupButtonText(getString(add_to_group), selector_button_blue)
        }
    }

    private fun updateGroupButtonText(label: String, res: Int) {
        groupButton.text = label
        groupButton.setBackgroundResource(res)
    }

}