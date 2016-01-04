package com.shareyourproxy.api.rx.event

import android.view.View
import com.shareyourproxy.app.adapter.DrawerAdapter

/**
 * When a Drawer item is clicked dispatch this event.
 * @param view     clicked
 * @param position of item in list
 * @param message  item message
 */
internal final class SelectDrawerItemEvent(val drawerItem: DrawerAdapter.DrawerItem, val view: View, val position: Int, val message: String)
