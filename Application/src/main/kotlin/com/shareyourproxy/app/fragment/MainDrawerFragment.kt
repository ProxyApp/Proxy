package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.shareyourproxy.R
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.command.eventcallback.LoggedInUserUpdatedEventCallback
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent
import com.shareyourproxy.app.AggregateFeedActivity
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.app.adapter.DrawerAdapter
import com.shareyourproxy.app.adapter.DrawerAdapter.DrawerItem

import butterknife.Bind
import butterknife.ButterKnife
import rx.subscriptions.CompositeSubscription


/**
 * Drawer Fragment to handle displaying a user profile with options.
 */
class MainDrawerFragment : BaseFragment(), ItemLongClickListener {

    @Bind(R.id.fragment_drawer_recyclerview)
    internal var drawerRecyclerView: BaseRecyclerView
    private var _adapter: DrawerAdapter? = null
    private var _subscriptions: CompositeSubscription? = null

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_drawer, container, false)
        ButterKnife.bind(this, view)
        initializeRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        _subscriptions = CompositeSubscription()
        _subscriptions!!.add(rxBus.toObservable().subscribe(busObserver))
    }

    val busObserver: JustObserver<Any>
        get() = object : JustObserver<Any>() {
            fun next(event: Any) {
                if (event is LoggedInUserUpdatedEventCallback) {
                    _adapter!!.updateUser(event.user)
                }
            }
        }

    override fun onPause() {
        super.onPause()
        _subscriptions!!.unsubscribe()
        _subscriptions = null
    }

    /**
     * Initialize a recyclerView with User data and menu options.
     */
    private fun initializeRecyclerView() {
        _adapter = DrawerAdapter.newInstance(loggedInUser, this)

        drawerRecyclerView.layoutManager = LinearLayoutManager(activity)
        drawerRecyclerView.setHasFixedSize(true)
        drawerRecyclerView.itemAnimator = DefaultItemAnimator()
        drawerRecyclerView.adapter = _adapter
    }

    override fun onItemClick(view: View, position: Int) {
        val drawerItem = _adapter!!.getSettingValue(position)
        rxBus.post(SelectDrawerItemEvent(
                drawerItem, view, position, getString(drawerItem.labelRes)))
    }

    override fun onItemLongClick(view: View, position: Int) {
        val item = _adapter!!.getSettingValue(position)
        if (item != DrawerItem.HEADER) {
            Toast.makeText(activity, getString(item.labelRes), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        /**
         * Create a new instance of this fragment for parent [AggregateFeedActivity].

         * @return drawer fragment
         */
        fun newInstance(): MainDrawerFragment {
            return MainDrawerFragment()
        }
    }
}
/**
 * Constructor.
 */
