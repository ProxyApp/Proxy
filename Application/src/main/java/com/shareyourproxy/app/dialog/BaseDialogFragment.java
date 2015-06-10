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
     * @param button        Button
     * @param colorResource integer value of resource.
     */
    public void setTextColorResource(@NonNull Button button, int colorResource) {
        button.setTextColor(getResources().getColor(colorResource));
    }

    public User getLoggedInUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }
}
