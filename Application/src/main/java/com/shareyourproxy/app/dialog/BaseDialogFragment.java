package com.shareyourproxy.app.dialog;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Button;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.app.BaseActivity;

/**
 * Base Dialog Abstraction.
 */
public class BaseDialogFragment extends DialogFragment {
    /**
     * Sets the color of button input.
     *
     * @param button Button
     * @param color  integer value of color.
     */
    public void setButtonTint(@NonNull Button button, int color) {
        button.setTextColor(color);
    }

    public User getLoggedInUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }
}
