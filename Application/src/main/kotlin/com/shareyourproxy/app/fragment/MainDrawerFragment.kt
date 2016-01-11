package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.shareyourproxy.R
import com.shareyourproxy.R.id.fragment_drawer_recyclerview
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.app.adapter.DrawerAdapter
import com.shareyourproxy.app.adapter.DrawerAdapter.DrawerItem
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindView


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
internal final class MainDrawerFragment() : BaseFragment(), ItemLongClickListener {
    private val clazz = MainDrawerFragment::class.java;
    private val drawerRecyclerView: BaseRecyclerView by bindView(fragment_drawer_recyclerview)
    private val adapter: DrawerAdapter by LazyVal { DrawerAdapter(loggedInUser, this) }
    private val busObserver: JustObserver<Any> = object : JustObserver<Any>(clazz) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is LoggedInUserUpdatedEventCallback) {
                adapter.updateUser(event.user)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawer, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        rxBusObservable().subscribe(busObserver)
    }

    override fun onItemClick(view: View, position: Int) {
        val drawerItem = adapter.getSettingValue(position)
        post(SelectDrawerItemEvent(drawerItem, view, position, getString(drawerItem.labelRes)))
    }

    override fun onItemLongClick(view: View, position: Int) {
        val item = adapter.getSettingValue(position)
        if (item != DrawerItem.HEADER) {
            Toast.makeText(activity, getString(item.labelRes), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initialize a recyclerView with User data and menu options.
     */
    private fun initializeRecyclerView() {
        drawerRecyclerView.layoutManager = LinearLayoutManager(activity)
        drawerRecyclerView.setHasFixedSize(true)
        drawerRecyclerView.itemAnimator = DefaultItemAnimator()
        drawerRecyclerView.adapter = adapter
    }
}
