package com.shareyourproxy.app

import android.os.Bundle
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.design.widget.Snackbar.make
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.shareyourproxy.R.anim.fade_in
import com.shareyourproxy.R.anim.slide_out_bottom
import com.shareyourproxy.R.id.activity_fragment_container
import com.shareyourproxy.R.id.activity_toolbar
import com.shareyourproxy.R.layout.common_activity_fragment_container
import com.shareyourproxy.R.raw.ic_clear
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccessEvent
import com.shareyourproxy.api.rx.event.ChannelAddedEvent
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog
import com.shareyourproxy.app.fragment.AddChannelListFragment
import com.shareyourproxy.util.ButterKnife
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.getMenuIcon
import timber.log.Timber

/**
 * Activity that displays a list of Channels for a user to add to their [UserProfileFragment].
 */
internal final class AddChannelListActivity : BaseActivity() {

    private val analytics by ButterKnife.LazyVal { RxGoogleAnalytics(this) }
    private val toolbar: Toolbar by bindView(activity_toolbar)
    private val addChannel: String by bindString(add_channel)
    private val addAnotherChannel: String by bindString(add_another_channel)
    private val activityObserver = object : JustObserver<Any>(AddChannelListActivity::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            when (event) {
                is UserChannelAddedEventCallback -> channelAdded(event)
                is AddChannelDialogSuccessEvent -> showAddGroupChannelDialog(event)
                is ChannelAddedEvent -> ChannelAddedEvent(event)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(common_activity_fragment_container)
        buildToolbar(toolbar, addChannel, getMenuIcon(this, ic_clear))
        if (savedInstanceState == null) {
            val fragment = AddChannelListFragment()
            supportFragmentManager.beginTransaction().replace(activity_fragment_container, fragment).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        RxBusRelay.rxBusObservable().subscribe(activityObserver)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(fade_in, slide_out_bottom)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> Timber.e("Option item selected is unknown")
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showAddGroupChannelDialog(event: AddChannelDialogSuccessEvent) {
        SaveGroupChannelDialog.show(supportFragmentManager,event.channel, event.user)
    }

    private fun ChannelAddedEvent(event: ChannelAddedEvent) {
        showSnackBar(event)
    }

    private fun showSnackBar(event: ChannelAddedEvent) {
        make(toolbar, getString(blank_added, event.channel.channelType), LENGTH_LONG).show()
    }

    private fun channelAdded(event: UserChannelAddedEventCallback) {
        if (event.oldChannel.equals(Channel())) {
            analytics.channelAdded(event.newChannel.channelType)
        } else {
            analytics.channelEdited(event.oldChannel.channelType)
        }
        toolbar.title = addAnotherChannel
    }
}
