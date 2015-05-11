package com.proxy.api.rx.event;

/**
 * Notification that a group adapter item has been dismissed.
 */
public class GroupDismissedEvent {
    public final int position;

    /**
     * Constructor.
     *
     * @param position of dismissed item
     */
    public GroupDismissedEvent(int position) {
        this.position = position;
    }
}
