package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * When a Drawer item is clicked dispatch this event.
 */
public class SelectDrawerItemEvent {

    public final View view;
    public final int position;
    public final String message;

    /**
     * Constructor.
     *
     * @param view     clicked
     * @param position of item in list
     * @param message  item message
     */
    public SelectDrawerItemEvent(@NonNull View view, int position, String message) {
        this.view = view;
        this.message = message;
        this.position = position;
    }

}
