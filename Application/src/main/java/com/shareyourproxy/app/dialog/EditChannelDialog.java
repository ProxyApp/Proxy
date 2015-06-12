package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand;
import com.shareyourproxy.util.DebugUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Created by Evan on 5/21/15.
 */
public class EditChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL = "EditChannelDialog.Channel";
    private static final String TAG = DebugUtils.getSimpleName(AddChannelDialog.class);
    @InjectView(R.id.dialog_channel_action_address_edittext)
    protected EditText editTextActionAddress;
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                dialogInterface.dismiss();
            }
        };
    @InjectView(R.id.dialog_channel_label_edittext)
    protected EditText editTextLabel;
    @InjectView(R.id.dialog_channel_label_floatlabel)
    protected TextInputLayout floatLabelChannelLabel;
    @InjectView(R.id.dialog_channel_action_address_floatlabel)
    protected TextInputLayout floatLabelAddress;
    private int _gray;
    private int _blue;
    private Channel _channel;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is
     * pressed.
     */
    private final OnEditorActionListener _onEditorActionListener =
        new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
                // FixedDecimalEditText's inputType is Decimal
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    dispatchAddChannel();
                    getDialog().dismiss();
                    return true;
                }
                return false;
            }
        };
    private final OnClickListener _positiveClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchAddChannel();
                dialogInterface.dismiss();
            }
        };

    private final OnClickListener _deleteClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchDeleteChannel();
                dialogInterface.dismiss();
            }
        };

    public EditChannelDialog() {
    }

    /**
     * Create a new instance of a {@link AddGroupDialog}.
     *
     * @return A {@link AddGroupDialog}
     */
    public static EditChannelDialog newInstance(
        Channel channel) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CHANNEL, channel);
        //copy dialog instance
        EditChannelDialog dialog = new EditChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private void dispatchDeleteChannel() {
        getRxBus().post(new DeleteUserChannelCommand(getLoggedInUser(), _channel));
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void dispatchAddChannel() {
        String actionContent = editTextActionAddress.getText().toString().trim();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            Channel channel =
                createModelInstance(_channel.id().value(), labelContent,
                    _channel.channelType(), _channel.channelSection(),
                    actionContent);
            getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel));
        }
    }

    /**
     * If text is entered into the dialog {@link EditText}, change the background underline of the
     * widget.
     *
     * @param editable the string entered in the {@link EditText}
     */
    @OnTextChanged(value = R.id.dialog_channel_action_address_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterActionAddressChanged(Editable editable) {
        editTextActionAddress.getBackground().setColorFilter(
            TextUtils.isEmpty(editable) ? _gray : _blue, PorterDuff.Mode.SRC_IN);
    }

    @OnTextChanged(value = R.id.dialog_channel_label_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterLabelChanged(Editable editable) {
        editTextLabel.getBackground().setColorFilter(
            TextUtils.isEmpty(editable) ? _gray : _blue, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channel = getArguments().getParcelable(ARG_CHANNEL);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_channel, null, false);
        ButterKnife.inject(this, view);
        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(R.string.dialog_editchannel_title)
            .setView(view)
            .setPositiveButton(R.string.common_save, _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .setNeutralButton(R.string.common_delete, _deleteClicked)
            .create();
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setTextColorResource(dialog.getButton(Dialog.BUTTON_POSITIVE), R.color.common_green);
        setTextColorResource(dialog.getButton(Dialog.BUTTON_NEGATIVE), R.color.common_text);
        setTextColorResource(dialog.getButton(Dialog.BUTTON_NEUTRAL), R.color.common_text);
        initializeEditTextColors();

    }

    /**
     * Initialize values for EditText to switch color on in {@link AddGroupDialog#afterTextChanged}
     */
    private void initializeEditTextColors() {
        Context context = editTextActionAddress.getContext();
        _gray = context.getResources().getColor(R.color.common_divider);
        _blue = context.getResources().getColor(R.color.common_blue);

        editTextActionAddress.getBackground().setColorFilter(_blue, PorterDuff.Mode.SRC_IN);
        editTextActionAddress.setText(_channel.actionAddress());

        editTextLabel.getBackground().setColorFilter(_blue, PorterDuff.Mode.SRC_IN);
        editTextLabel.setText(_channel.label());

        floatLabelAddress.setHint(context.getString(R.string.edit_channel_address));
        floatLabelChannelLabel.setHint(context.getString(R.string.edit_channel_description));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public EditChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
