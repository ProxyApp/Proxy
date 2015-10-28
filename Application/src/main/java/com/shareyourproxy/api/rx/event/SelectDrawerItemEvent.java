package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;
import android.view.View;

import com.shareyourproxy.app.adapter.DrawerAdapter.DrawerItem;

/**
 * When a Drawer item is clicked dispatch this event.
 */
public class SelectDrawerItemEvent {

    public final View view;
    public final int position;
    public final String message;
    public final DrawerItem drawerItem;

    /**
     * Constructor.
     *
     * @param view     clicked
     * @param position of item in list
     * @param message  item message
     */
    public SelectDrawerItemEvent(
        @NonNull DrawerItem drawerItem, @NonNull View view, int position, String message) {
        this.drawerItem = drawerItem;
        this.view = view;
        this.message = message;
        this.position = position;
    }

}
