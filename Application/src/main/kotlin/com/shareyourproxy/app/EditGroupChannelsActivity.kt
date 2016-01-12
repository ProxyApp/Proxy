package com.shareyourproxy.app

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.IntentLauncher.launchEditGroupContactsActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.anim.fade_in
import com.shareyourproxy.R.anim.slide_out_bottom
import com.shareyourproxy.R.id.activity_fragment_container
import com.shareyourproxy.R.id.menu_edit_group_channel_save
import com.shareyourproxy.R.layout.common_activity_fragment_container
import com.shareyourproxy.R.raw.ic_clear
import com.shareyourproxy.R.raw.ic_done
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.command.UpdateUserContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback
import com.shareyourproxy.api.rx.event.ViewGroupContactsEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment.Companion.ARG_SELECT_GROUP_TAB
import com.shareyourproxy.app.fragment.EditGroupChannelsFragment
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.GroupEditType
import com.shareyourproxy.util.Enumerations.GroupEditType.*
import com.shareyourproxy.util.ViewUtils.getMenuIconSecondary
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import java.util.*

/**
 * Add and remove newChannel permissions from a group.
 */
private final class EditGroupChannelsActivity : BaseActivity() {
    private val toolbar: Toolbar by bindView(R.id.activity_toolbar)
    private val selectedGroup: Group by LazyVal { intent.extras.getParcelable<Group>(ARG_SELECTED_GROUP) }
    private val addOrEdit: GroupEditType by LazyVal { intent.extras.getSerializable(ARG_EDIT_GROUP_TYPE) as GroupEditType }

    override fun onBackPressed() {
        hideSoftwareKeyboard(toolbar)
        finish()
        overridePendingTransition(fade_in, slide_out_bottom)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(common_activity_fragment_container)
        initialize()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(activity_fragment_container, EditGroupChannelsFragment()).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        rxBusObservable().subscribe(onNextEvent(this))
    }

    private fun initialize() {
        val editType = addOrEdit
        when (editType) {
            ADD_GROUP -> buildToolbar(toolbar, getString(add_group), getMenuIconSecondary(this, ic_clear))
            EDIT_GROUP -> buildToolbar(toolbar, getString(edit_group), getMenuIconSecondary(this, ic_clear))
            PUBLIC_GROUP -> buildToolbar(toolbar, getString(public_group), getMenuIconSecondary(this, ic_clear))
        }
    }

    private fun onNextEvent(activity: EditGroupChannelsActivity): JustObserver<Any> {
        return object : JustObserver<Any>(EditGroupChannelsActivity::class.java) {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any) {
                when (event) {
                    is UserGroupDeletedEventCallback -> userGroupDeleted(event)
                    is ViewGroupContactsEvent -> launchEditGroupContactsActivity(activity, selectedGroup)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_activity_edit_group_channel, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val search = menu.findItem(menu_edit_group_channel_save)
        search.setIcon(getMenuIconSecondary(this, ic_done))
        return super.onPrepareOptionsMenu(menu)
    }

    private fun userGroupDeleted(event: UserGroupDeletedEventCallback) {
        updateUserContacts(event)
        launchMainActivity(this, ARG_SELECT_GROUP_TAB, true, event.group)
        onBackPressed()
    }

    private fun updateUserContacts(event: UserGroupDeletedEventCallback) {
        val contacts = ArrayList<String>()
        event.group.contacts.forEach { contacts.add(it) }
        post(UpdateUserContactsCommand(loggedInUser, contacts, loggedInUser.groups))
    }
}
