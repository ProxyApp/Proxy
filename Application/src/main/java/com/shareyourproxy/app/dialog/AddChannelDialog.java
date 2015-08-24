package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;

import java.util.UUID;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static android.content.DialogInterface.OnClickListener;
import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.DebugUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add a new {@link Channel} to a {@link User}.
 */
public class AddChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType";
    private static final String TAG = getSimpleName(AddChannelDialog.class);
    @Bind(R.id.dialog_channel_action_address_edittext)
    protected EditText editTextActionAddress;
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                dismiss();
            }
        };
    @Bind(R.id.dialog_channel_label_edittext)
    protected EditText editTextLabel;
    @Bind(R.id.dialog_channel_label_floatlabel)
    protected TextInputLayout floatLabelChannelLabel;
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    protected TextInputLayout floatLabelAddress;
    @BindColor(R.color.common_text)
    protected int _textColor;
    @BindColor(R.color.common_divider)
    protected int _gray;
    @BindColor(R.color.common_blue)
    protected int _blue;
    @BindString(R.string.required)
    protected String _required;
    private ChannelType _channelType;
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
                    saveChannelAndExit();
                    return true;
                }
                return false;
            }
        };
    private final View.OnClickListener _positiveClicked =
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChannelAndExit();
            }
        };
    private String _dialogTitle;
    private String _channelAddressHint;
    private String _channelLabelHint;

    /**
     * Create a new instance of a {@link AddChannelDialog}.
     *
     * @return A {@link AddChannelDialog}
     */
    public static AddChannelDialog newInstance(ChannelType channelType) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CHANNEL_TYPE, channelType.getLabel());
        //create dialog instance
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
                createModelInstance(id, labelContent, _channelType, actionContent);
            getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channelType = ChannelType.valueOfLabel(getArguments().getString(ARG_CHANNEL_TYPE));
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_add_channel, null, false);
        ButterKnife.bind(this, view);
        initializeDisplayValues();

        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(_dialogTitle)
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();

        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;

        // Setup Button Colors
        initializeEditTextColors();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _textColor);
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked);
    }

    public void saveChannelAndExit() {
        boolean addressHasText = editTextActionAddress.getText().toString().trim().length() > 0;
        if (!addressHasText) {
            floatLabelAddress.setError(_required);
        } else {
            floatLabelAddress.setErrorEnabled(false);
            addUserChannel();
            dismiss();
        }
    }

    private void initializeDisplayValues() {
        String name = _channelType.getLabel();
        switch (_channelType) {
            case Phone:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_phone);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_phone);
                break;
            case SMS:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_sms);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_sms);
                break;
            case Email:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_email);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_email);
                break;
            case Web:
                _dialogTitle = getString(R.string.dialog_addchannel_title_web);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_web);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_web);
                break;
            case Custom:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_custom);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_custom);
                break;
            case Meerkat:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Snapchat:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Spotify:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Linkedin:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case FBMessenger:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Hangouts:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Whatsapp:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_phone);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Yo:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Googleplus:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Github:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Address:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Slack:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Youtube:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Instagram:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Tumblr:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Ello:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Venmo:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Periscope:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Medium:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Soundcloud:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Skype:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_addchannel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
                break;
            case Reddit:
                /**
                 * Use {@link AddRedditChannelDialog}.
                 */
                break;
            default:
                _dialogTitle = getString(R.string.dialog_addchannel_title_default);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default);
                break;
        }
    }

    /**
     * Initialize color and hints for edit text.
     */
    private void initializeEditTextColors() {
        floatLabelAddress.setHint(_channelAddressHint);
        floatLabelChannelLabel.setHint(_channelLabelHint);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
