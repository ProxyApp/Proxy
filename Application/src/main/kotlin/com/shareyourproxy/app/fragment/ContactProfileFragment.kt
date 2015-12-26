package com.shareyourproxy.app.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import butterknife.Bind
import butterknife.BindDimen
import butterknife.OnClick
import com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxQuery
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback
import com.shareyourproxy.app.dialog.UserGroupsDialog
import com.shareyourproxy.util.ViewUtils.getMenuIcon
import java.util.*

/**
 * Display a contacts profile and channels.
 */
class ContactProfileFragment : UserProfileFragment() {
    private val _rxQuery = RxQuery
    @Bind(R.id.fragment_user_profile_header_button)
    internal var groupButton: Button
    @BindDimen(R.dimen.fragment_userprofile_header_contact_background_size)
    internal var marginContactHeight: Int = 0
    private val _toggleGroups = ArrayList<GroupToggle>()


    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_header_button)
    internal fun onClickGroup() {
        UserGroupsDialog.newInstance(_toggleGroups, contact).show(fragmentManager)
    }

    override fun onCreateView(rootView: View) {
        super.onCreateView(rootView)
        initialize()
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
        groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getMenuIcon(activity, R.raw.ic_groups), null, null, null)
        updateGroupButtonText(groupEditContacts)
    }


    private val groupEditContacts: List<Group> get() {
        _toggleGroups.clear()
        val list = _rxQuery.queryContactGroups(loggedInUser, contact)
        _toggleGroups.addAll(list)
        val selectedGroupsList = ArrayList<Group>(list.size)
        for (groupToggle in list) {
            if (groupToggle.isChecked) {
                selectedGroupsList.add(groupToggle.group)
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


    override fun onResume() {
        super.onResume()
        rxBus.toObservable().subscribe(onNextEvent())
    }

    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent(event)
                }
            }
        }
    }

    private fun groupContactsUpdatedEvent(event: GroupContactsUpdatedEventCallback) {
        updateGroupButtonText(event.contactGroups)
    }

    companion object {
        /**
         * Return new instance for parent [UserContactActivity].
         * @return layouts.fragment
         */
        fun newInstance(contact: User, loggedInUserId: String): ContactProfileFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact)
            bundle.putString(ARG_LOGGEDIN_USER_ID, loggedInUserId)
            val fragment = ContactProfileFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}