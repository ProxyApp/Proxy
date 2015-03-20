package com.proxy.app.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.proxy.R;
import com.proxy.event.GroupAddedEvent;
import com.proxy.event.OttoBusDriver;
import com.proxy.model.Group;
import com.proxy.widget.FloatLabelLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

import static com.proxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Dialog that pops up when you click add a group.
 */
public class AddGroupDialog extends BaseDialogFragment {

    @InjectView(R.id.dialog_addgroup_floatlabel)
    FloatLabelLayout mFloatLabel;
    @InjectView(R.id.dialog_addgroup_edittext)
    EditText mEditText;
    private final DialogInterface.OnClickListener mPositiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                OttoBusDriver.post(new GroupAddedEvent(
                    Group.create(mEditText.getText().toString())));
                dialogInterface.dismiss();
            }
        };
    private final DialogInterface.OnClickListener mNegativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(mEditText);
                dialogInterface.dismiss();
            }
        };

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
    @SuppressWarnings("unused")
    @OnTextChanged(value = R.id.dialog_addgroup_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        mEditText.setSelected(!TextUtils.isEmpty(editable));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_addgroup, null, false);
        ButterKnife.inject(this, view);

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog))
            .setTitle(R.string.dialog_addgroup_title)
            .setView(view)
            .setPositiveButton(getString(R.string.save), mPositiveClicked)
            .setNegativeButton(android.R.string.cancel, mNegativeClicked)
            .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Setup Button Colors
        AlertDialog dialog = (AlertDialog) getDialog();
        setTextColorResource(dialog.getButton(Dialog.BUTTON_POSITIVE), R.color.common_green);
        setTextColorResource(dialog.getButton(Dialog.BUTTON_NEGATIVE), android.R.color.black);
        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
