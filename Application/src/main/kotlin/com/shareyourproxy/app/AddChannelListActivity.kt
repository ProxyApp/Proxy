package com.shareyourproxy.app

import android.os.Bundle
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.design.widget.Snackbar.make
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.shareyourproxy.R
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccessEvent
import com.shareyourproxy.api.rx.event.ChannelAddedEvent
import com.shareyourproxy.app.dialog.SaveGroupChannelDialog
import com.shareyourproxy.app.fragment.AddChannelListFragment
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.getMenuIcon
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * Activity that displays a list of Channels for a user to add to their [UserProfileFragment].
 */
private final class AddChannelListActivity : BaseActivity() {

    private val analytics = RxGoogleAnalytics(this)
    private val toolbar: Toolbar by bindView(R.id.activity_toolbar)
    private val addChannel: String by bindString(R.string.add_channel)
    private val addAnotherChannel: String by bindString(R.string.add_another_channel)
    private var subscriptions: CompositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_activity_fragment_container)
        initialize()
        if (savedInstanceState == null) {
            val fragment = AddChannelListFragment()
            supportFragmentManager.beginTransaction().replace(R.id.activity_fragment_container, fragment).commit()
        }
    }

    /**
     * Initialize this view.
     */
    private fun initialize() {
        buildToolbar(toolbar, addChannel, getMenuIcon(this, R.raw.ic_clear))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> Timber.e("Option item selected is unknown")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom)
    }

    public override fun onResume() {
        super.onResume()
        initializeSubscriptions()
    }

    /**
     * Create a composite subscription field to handle unsubscribing in onPause.
     */
    fun initializeSubscriptions() {
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any) {
                if (event is UserChannelAddedEventCallback) {
                    channelAdded(event)
                } else if (event is AddChannelDialogSuccessEvent) {
                    showAddGroupChannelDialog(event)
                } else if (event is ChannelAddedEvent) {
                    ChannelAddedEvent(event)
                }
            }
        }))
    }

    fun showAddGroupChannelDialog(event: AddChannelDialogSuccessEvent) {
        SaveGroupChannelDialog(event.channel, event.user).show(supportFragmentManager)
    }

    fun ChannelAddedEvent(event: ChannelAddedEvent) {
        showSnackBar(event)
    }

    private fun showSnackBar(event: ChannelAddedEvent) {
        make(toolbar, getString(R.string.blank_added, event.channel.channelType), LENGTH_LONG).show()
    }

    fun channelAdded(event: UserChannelAddedEventCallback) {
        if (event.oldChannel == null) {
            analytics.channelAdded(event.newChannel.channelType)
        } else {
            analytics.channelEdited(event.oldChannel.channelType)
        }
        toolbar.title = addAnotherChannel
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }
}
