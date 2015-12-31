package com.shareyourproxy.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_DEL
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent

/**
 * Custom Edit Text for listening to delete key actions.
 */
class CustomEditText : EditText {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return CustomInputConnection(super.onCreateInputConnection(outAttrs), true)
    }

    private class CustomInputConnection(target: InputConnection, mutable: Boolean) : InputConnectionWrapper(target, mutable) {
        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            if (afterLength == 0) {
                post(TextViewEditorActionEvent(KeyEvent(ACTION_DOWN, KEYCODE_DEL)))
            }
            return super.deleteSurroundingText(beforeLength, afterLength)
        }
    }
}
