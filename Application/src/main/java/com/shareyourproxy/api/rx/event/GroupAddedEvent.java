package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;

import timber.log.Timber;



/**
 * Pass around a group.
 */
public class GroupAddedEvent {
    public final Group group;

    /**
     * Public constructor.
     *
     * @param group this events group
     */
    public GroupAddedEvent(@NonNull Group group) {
        Timber.v(group.toString());
        this.group = group;
    }
}
