package com.shareyourproxy.api.rx.event;


import com.shareyourproxy.app.dialog.ErrorDialog;

/**
 * For now this just signals the dismissal of the {@link ErrorDialog}
 */
public class LoginErrorDialogEvent {
    public final DialogEvent action;

    /**
     * Set a {@link DialogEvent} action
     *
     * @param action that occured
     */
    public LoginErrorDialogEvent(DialogEvent action) {
        this.action = action;
    }

    public enum DialogEvent {
        DISMISS
    }
}
