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
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccessEvent;

import java.util.UUID;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static android.content.DialogInterface.OnClickListener;
import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ObjectUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add a new {@link Channel} to a {@link User}.
 */
public class AddChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType";
    private static final String TAG = Companion.getSimpleName(AddChannelDialog.class);
    @Bind(R.id.dialog_channel_action_address_edittext)
    EditText editTextActionAddress;
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                dismiss();
            }
        };
    @Bind(R.id.dialog_channel_label_edittext)
    EditText editTextLabel;
    @Bind(R.id.dialog_channel_label_floatlabel)
    TextInputLayout floatLabelChannelLabel;
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    TextInputLayout floatLabelAddress;
    @BindColor(R.color.common_text)
    int colorText;
    @BindColor(R.color.common_divider)
    int colorGray;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindString(R.string.required)
    String _required;
    private ChannelType _channelType;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
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
     * Constructor.
     */
    public AddChannelDialog() {
    }

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
        //TODO:CLEAN THIS MESS UP
        String actionContent = editTextActionAddress.getText().toString();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            String id = UUID.randomUUID().toString();
            Channel channel =
                createModelInstance(id, labelContent, _channelType, actionContent);
            RxBusDriver rxBus = getRxBus();
            User user = getLoggedInUser();
            rxBus.post(new AddUserChannelCommand(user, channel));
            user.channels().put(channel.id(), channel);
            rxBus.post(new AddChannelDialogSuccessEvent(user, channel));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channelType = ChannelType.Companion.valueOfLabel(getArguments().getString(ARG_CHANNEL_TYPE));
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
            R.style.Widget_Proxy_App_Dialog)
            .setTitle(_dialogTitle)
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();

        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText);
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked);
        //Setup TextInput hints.
        floatLabelAddress.setHint(_channelAddressHint);
        floatLabelChannelLabel.setHint(_channelLabelHint);
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
            case Address:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Custom:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_custom);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_custom);
                break;
            case Ello:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Email:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_email);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_email);
                break;
            case Facebook:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default);
            case FBMessenger:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Github:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Googleplus:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Hangouts:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Instagram:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case LeagueOfLegends:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Linkedin:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Medium:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Meerkat:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case NintendoNetwork:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Periscope:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Phone:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_channel_hint_address_phone);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_phone);
                break;
            case PlaystationNetwork:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Reddit:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default);
            case Skype:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Slack:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case SMS:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_sms);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_sms);
                break;
            case Snapchat:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Soundcloud:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Spotify:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Steam:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Tumblr:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Twitch:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Twitter:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default);
            case Venmo:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Web:
            case URL:
                _dialogTitle = getString(R.string.dialog_addchannel_title_web);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_web);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_web);
                break;
            case Whatsapp:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_phone);
                _channelLabelHint = getString(R.string.label);
                break;
            case XboxLive:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(R.string.gamertag);
                _channelLabelHint = getString(R.string.label);
                break;
            case Yo:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            case Youtube:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, name);
                _channelLabelHint = getString(R.string.label);
                break;
            default:
                _dialogTitle = getString(R.string.dialog_addchannel_title_default);
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
