package com.proxy.api.rx.event;


/**
 * For now this just signals the dismissal of the {@link com.proxy.app.dialog.LoginErrorDialog}
 */
public class LoginErrorDialogEvent {
    public enum DialogEvent {
        DISMISS
    }

    public final DialogEvent action;

    /**
     * Set a {@link DialogEvent} action
     *
     * @param action that occured
     */
    public LoginErrorDialogEvent(DialogEvent action) {
        this.action = action;
    }
}
