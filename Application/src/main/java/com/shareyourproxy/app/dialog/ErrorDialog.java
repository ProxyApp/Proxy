package com.shareyourproxy.app.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.event.LoginErrorDialogEvent;

import static com.shareyourproxy.api.rx.event.LoginErrorDialogEvent.DialogEvent.DISMISS;
import static com.shareyourproxy.util.DebugUtils.getSimpleName;

/**
 * Dialog to handle onError messaging during login.
 */
public class ErrorDialog extends BaseDialogFragment {
    private static final String TAG = getSimpleName(ErrorDialog.class);
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private DialogInterface.OnClickListener _onOkClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            getRxBus().post(new LoginErrorDialogEvent(DISMISS));
            dialog.dismiss();
        }
    };

    /**
     * Create a new dialog with a custom title and message body.
     *
     * @param title   header of dialog
     * @param message body of dialog
     * @return this dialog
     */
    public static ErrorDialog newInstance(String title, String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("message cannot be blank.");
        }

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);

        ErrorDialog dialog = new ErrorDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
            R.style.Widget_Proxy_App_Dialog)).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.ok, _onOkClicked).create();
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public ErrorDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }

}
