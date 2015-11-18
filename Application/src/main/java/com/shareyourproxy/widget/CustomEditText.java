package com.shareyourproxy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.TextViewEditorActionEvent;

/**
 * Created by Evan on 11/2/15.
 */
public class CustomEditText extends EditText {
    private RxBusDriver _rxBus;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new CustomInputConnection(_rxBus, super.onCreateInputConnection(outAttrs), true);
    }

    public void setRxBus(RxBusDriver rxBus) {
        _rxBus = rxBus;
    }

    private static class CustomInputConnection extends InputConnectionWrapper {
        private final RxBusDriver _rxBus;

        public CustomInputConnection(RxBusDriver rxBus, InputConnection target, boolean mutable) {
            super(target, mutable);
            _rxBus = rxBus;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (afterLength == 0) {
                _rxBus.post(new TextViewEditorActionEvent(
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }
}
