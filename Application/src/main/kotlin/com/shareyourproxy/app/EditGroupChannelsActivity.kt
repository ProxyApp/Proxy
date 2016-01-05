package com.shareyourproxy.app

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE
import com.shareyourproxy.Constants.ARG_SELECTED_GROUP
import com.shareyourproxy.IntentLauncher.launchEditGroupContactsActivity
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.command.UpdateUserContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupDeletedEventCallback
import com.shareyourproxy.api.rx.event.ViewGroupContactsEvent
import com.shareyourproxy.app.fragment.AggregateFeedFragment
import com.shareyourproxy.app.fragment.EditGroupChannelsFragment
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.Enumerations.GroupEditType
import com.shareyourproxy.util.Enumerations.GroupEditType.*
import com.shareyourproxy.util.ViewUtils.getMenuIconSecondary
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Add and remove newChannel permissions from a group.
 */
private final class EditGroupChannelsActivity : BaseActivity() {
    private val toolbar: Toolbar by bindView(R.id.activity_toolbar)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    override fun onBackPressed() {
        hideSoftwareKeyboard(toolbar)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_activity_fragment_container)
        initialize()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.activity_fragment_container, EditGroupChannelsFragment()).commit()
        }
    }

    private val addOrEdit: GroupEditType = intent.extras.getSerializable(ARG_EDIT_GROUP_TYPE) as GroupEditType

    private fun initialize() {
        val editType = addOrEdit
        if (ADD_GROUP == editType) {
            buildToolbar(toolbar, getString(R.string.add_group),
                    getMenuIconSecondary(this, R.raw.ic_clear))
        } else if (EDIT_GROUP == editType) {
            buildToolbar(toolbar, getString(R.string.edit_group),
                    getMenuIconSecondary(this, R.raw.ic_clear))
        } else if (PUBLIC_GROUP == editType) {
            buildToolbar(toolbar, getString(R.string.public_group),
                    getMenuIconSecondary(this, R.raw.ic_clear))
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptions = CompositeSubscription();
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(onNextEvent(this)))
    }

    private fun onNextEvent(activity: EditGroupChannelsActivity): JustObserver<Any> {
        return object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any) {
                if (event is UserGroupDeletedEventCallback) {
                    userGroupDeleted(event)
                } else if (event is ViewGroupContactsEvent) {
                    launchEditGroupContactsActivity(activity, selectedGroup)
                }
            }
        }
    }

    private val selectedGroup: Group = intent.extras.getParcelable<Group>(ARG_SELECTED_GROUP)

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_activity_edit_group_channel, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val search = menu.findItem(R.id.menu_edit_group_channel_save)
        search.setIcon(getMenuIconSecondary(this, R.raw.ic_done))
        return super.onPrepareOptionsMenu(menu)
    }

    private fun userGroupDeleted(event: UserGroupDeletedEventCallback) {
        updateUserContacts(event)
        launchMainActivity(this, AggregateFeedFragment.ARG_SELECT_GROUP_TAB, true, event.group)
        onBackPressed()
    }

    private fun updateUserContacts(event: UserGroupDeletedEventCallback) {
        val contacts = ArrayList<String>()
        for (contactId in event.group.contacts) {
            contacts.add(contactId)
        }
        post(UpdateUserContactsCommand(loggedInUser, contacts, loggedInUser.groups))
    }
}
