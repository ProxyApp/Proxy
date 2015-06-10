package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.shareyourproxy.api.domain.model.ChannelSection;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

import static android.content.DialogInterface.OnClickListener;
import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.DebugUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Created by Evan on 5/5/15.
 */
public class AddChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType";
    private static final String ARG_CHANNEL_SECTION = "AddChannelDialog.ChannelSection";
    private static final String TAG = getSimpleName(AddChannelDialog.class);
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
    private int _green;
    private ChannelType _channelType;
    private ChannelSection _channelSection;
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
                    addUserChannel();
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
                addUserChannel();
                dialogInterface.dismiss();
            }
        };

    /**
     * Create a new instance of a {@link AddGroupDialog}.
     *
     * @return A {@link AddGroupDialog}
     */
    public static AddChannelDialog newInstance(
        ChannelType channelType, ChannelSection channelSection) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CHANNEL_TYPE, channelType.getLabel());
        bundle.putString(ARG_CHANNEL_SECTION, channelSection.getLabel());
        //copy dialog instance
        AddChannelDialog dialog = new AddChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void addUserChannel() {
        String actionContent = editTextActionAddress.getText().toString();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            String id = UUID.randomUUID().toString();
            Channel channel =
                createModelInstance(id, labelContent, _channelType, _channelSection,
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
            TextUtils.isEmpty(editable) ? _gray : _green, PorterDuff.Mode.SRC_IN);
    }

    @OnTextChanged(value = R.id.dialog_channel_label_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterLabelChanged(Editable editable) {
        editTextLabel.getBackground().setColorFilter(
            TextUtils.isEmpty(editable) ? _gray : _green, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channelType = ChannelType.valueOf(getArguments().getString(ARG_CHANNEL_TYPE));
        _channelSection = ChannelSection.valueOf(getArguments().getString(ARG_CHANNEL_SECTION));
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
            .setTitle(R.string.dialog_addchannel_title)
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
        setTextColorResource(dialog.getButton(Dialog.BUTTON_POSITIVE), R.color.common_green);
        setTextColorResource(dialog.getButton(Dialog.BUTTON_NEGATIVE), android.R.color.black);
        initializeEditTextColors();
    }

    /**
     * Initialize values for EditText to switch color on in {@link AddGroupDialog#afterTextChanged}
     */
    private void initializeEditTextColors() {
        Context context = editTextActionAddress.getContext();
        _gray = context.getResources().getColor(R.color.common_divider);
        _green = context.getResources().getColor(R.color.common_green);

        editTextActionAddress.getBackground().setColorFilter(_gray, PorterDuff.Mode.SRC_IN);
        editTextLabel.getBackground().setColorFilter(_gray, PorterDuff.Mode.SRC_IN);

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
    public AddChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
