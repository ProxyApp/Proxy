package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shareyourproxy.R;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Dialog that pops up when you click add a group.
 */
public class AddGroupDialog extends BaseDialogFragment {

    @Bind(R.id.dialog_addgroup_edittext)
    protected EditText editText;
    private final DialogInterface.OnClickListener _negativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editText);
                dialogInterface.dismiss();
            }
        };
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is
     * pressed.
     */
    private final TextView.OnEditorActionListener _onEditorActionListener =
        new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
                // FixedDecimalEditText's inputType is Decimal
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    getDialog().dismiss();
                    return true;
                }
                return false;
            }
        };
    private final DialogInterface.OnClickListener _positiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    @BindColor(R.color.common_text)
    protected int _textColor;
    @BindColor(R.color.common_divider)
    protected int _gray;
    @BindColor(R.color.common_blue)
    protected int _blue;


    /**
     * Create a new instance of a {@link AddGroupDialog}.
     *
     * @return A {@link AddGroupDialog}
     */
    public static AddGroupDialog newInstance() {
        return new AddGroupDialog();
    }



    /**
     * If text is entered into the dialog {@link EditText}, change the background underline of the
     * widget.
     *
     * @param editable the string entered in the {@link EditText}
     */
    @OnTextChanged(value = R.id.dialog_addgroup_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        editText.getBackground().setColorFilter(!TextUtils.isEmpty(editable)
            ? _blue : _gray, PorterDuff.Mode.SRC_IN);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_addgroup, null, false);
        ButterKnife.bind(this, view);
        editText.setOnEditorActionListener(_onEditorActionListener);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(R.string.dialog_addgroup_title)
            .setView(view)
            .setPositiveButton(getString(R.string.common_save), _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();

        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Setup Button Colors
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _textColor);
        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        editText.getBackground().setColorFilter(_gray, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
