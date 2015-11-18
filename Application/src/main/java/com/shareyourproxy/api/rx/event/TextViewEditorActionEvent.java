package com.shareyourproxy.api.rx.event;

import android.view.KeyEvent;

/**
 * Created by Evan on 11/2/15.
 */
public class TextViewEditorActionEvent {
    public final KeyEvent keyEvent;

    public TextViewEditorActionEvent(KeyEvent keyEvent) {
        this.keyEvent = keyEvent;
    }
}
