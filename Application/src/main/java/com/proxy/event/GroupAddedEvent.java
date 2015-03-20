package com.proxy.event;

import android.support.annotation.NonNull;

import com.proxy.model.Group;

import timber.log.Timber;

import static com.proxy.util.DebugUtils.getDebugTAG;


/**
 * Pass around a group.
 */
public class GroupAddedEvent {
    private static final String TAG = getDebugTAG(UserAddedEvent.class);
    public final Group group;

    /**
     * Public constructor.
     *
     * @param group this events group
     */
    public GroupAddedEvent(@NonNull Group group) {
        Timber.v(TAG + group);
        this.group = group;
    }
}
