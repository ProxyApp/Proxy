package com.shareyourproxy.app.fragment

import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import com.shareyourproxy.Constants
import com.shareyourproxy.R
import com.shareyourproxy.R.id.fragment_user_profile_toolbar
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
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Display a contacts profile and channels.
 */
internal final class ContactProfileFragment(contact: User, loggedInUserId: String) : UserProfileFragment() {

    init {
        arguments.putParcelable(Constants.ARG_USER_SELECTED_PROFILE, contact)
        arguments.putString(Constants.ARG_LOGGEDIN_USER_ID, loggedInUserId)
    }

    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val groupButton: Button by bindView(R.id.fragment_user_profile_header_button)
    private val marginContactHeight: Int by bindDimen(R.dimen.fragment_userprofile_header_contact_background_size)
    private val toggleGroups = ArrayList<GroupToggle>()
    private val toolbar: Toolbar by bindView(fragment_user_profile_toolbar)
    private val onClickGroup: View.OnClickListener = View.OnClickListener {
        UserGroupsDialog(toggleGroups, contact).show(fragmentManager)
    }
    private val onNextEvent = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is GroupContactsUpdatedEventCallback) {
                groupContactsUpdatedEvent(event)
            }
        }
    }

    override fun onCreateView(rootView: View) {
        super.onCreateView(rootView)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(rxBusObservable().subscribe(onNextEvent))
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
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
        groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(getMenuIcon(activity, R.raw.ic_groups), null, null, null)
        updateGroupButtonText(groupEditContacts)
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

    @SuppressWarnings("unchecked")
    private fun updateGroupButtonText(list: List<Group>?) {
        if (list != null) {
            val groupSize = list.size
            if (groupSize == 0) {
                groupButton.setText(R.string.add_to_group)
                groupButton.setBackgroundResource(R.drawable.selector_button_blue)
            } else if (groupSize == 1) {
                groupButton.text = list[0].label
                groupButton.setBackgroundResource(R.drawable.selector_button_grey)
            } else if (groupSize > 1) {
                groupButton.text = getString(R.string.in_blank_groups, groupSize)
                groupButton.setBackgroundResource(R.drawable.selector_button_grey)
            }
        } else {
            groupButton.setText(R.string.add_to_group)
            groupButton.setBackgroundResource(R.drawable.selector_button_blue)
        }
    }

    private fun groupContactsUpdatedEvent(event: GroupContactsUpdatedEventCallback) {
        updateGroupButtonText(event.contactGroups)
    }

}