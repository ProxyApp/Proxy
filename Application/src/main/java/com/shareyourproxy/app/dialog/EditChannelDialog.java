package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand;
import com.shareyourproxy.util.ObjectUtils;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Dialog that handles editing a selected channel.
 */
public class EditChannelDialog extends BaseDialogFragment {
    // Final
    private static final String ARG_CHANNEL = "EditChannelDialog.Channel";
    private static final String ARG_POSITION = "EditChannelDialog.Position";
    private static final String TAG = ObjectUtils.getSimpleName(AddChannelDialog.class);
    // View
    @Bind(R.id.dialog_channel_action_address_edittext)
    EditText editTextActionAddress;
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
            }
        };
    @Bind(R.id.dialog_channel_label_edittext)
    EditText editTextLabel;
    @Bind(R.id.dialog_channel_label_floatlabel)
    TextInputLayout floatLabelChannelLabel;
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    TextInputLayout floatLabelAddress;
    // Color
    @BindColor(R.color.common_text)
    int colorText;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindString(R.string.required)
    String stringRequired;
    // Transient
    private Channel _channel;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private final OnEditorActionListener _onEditorActionListener =
        new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    updateChannelAndExit();
                    return true;
                }
                return false;
            }
        };
    private final View.OnClickListener _positiveClicked =
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChannelAndExit();
            }
        };
    private String _dialogTitle;
    private String _channelAddressHint;
    private String _channelLabelHint;
    private int _position;
    private final OnClickListener _deleteClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getRxBus().post(
                    new DeleteUserChannelCommand(getLoggedInUser(), _channel, _position));
                dialogInterface.dismiss();
            }
        };

    /**
     * Constructor.
     */
    public EditChannelDialog() {
    }

    /**
     * Create a new instance of a {@link EditChannelDialog}.
     *
     * @return A {@link EditChannelDialog}
     */
    public static EditChannelDialog newInstance(
        Channel channel, int position) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CHANNEL, channel);
        bundle.putInt(ARG_POSITION, position);
        //copy dialog instance
        EditChannelDialog dialog = new EditChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void addUserChannel() {
        String actionContent = editTextActionAddress.getText().toString().trim();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            String id = _channel.id();
            ChannelType channelType = _channel.channelType();
            Channel channel = _channel.channelType().equals(ChannelType.Facebook) ?
                createModelInstance(id, _channel.label(), channelType, actionContent) :
                createModelInstance(id, labelContent, channelType, actionContent);
            //post and save
            getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel, _channel));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channel = getArguments().getParcelable(ARG_CHANNEL);
        _position = getArguments().getInt(ARG_POSITION);
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

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Widget_Proxy_App_Dialog)
            .setTitle(_dialogTitle)
            .setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .setNeutralButton(R.string.delete, _deleteClicked)
            .create();
        //Override the dialog wrapping content and cancel dismiss on click outside
        // of the dialog window
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.setCanceledOnTouchOutside(false);
        initializeEditText();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText);
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked);
    }

    public void updateChannelAndExit() {
        boolean addressHasText = editTextActionAddress.getText().toString().trim().length() > 0;
        if (!addressHasText) {
            floatLabelAddress.setError(stringRequired);
        } else {
            floatLabelAddress.setErrorEnabled(false);
            addUserChannel();
            dismiss();
        }
    }

    private void initializeDisplayValues() {
        String label = _channel.channelType().getLabel();
        switch (_channel.channelType()) {
            case Address:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Custom:
                _dialogTitle = getString(R.string.dialog_editchannel_title_custom);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_custom);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_custom);
                break;
            case Ello:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Email:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_email);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_email);
                break;
            case Facebook:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_facebook);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default);
                break;
            case FBMessenger:
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Github:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Googleplus:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Hangouts:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Instagram:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case LeagueOfLegends:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Linkedin:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Medium:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Meerkat:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case NintendoNetwork:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Periscope:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Phone:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_phone);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_phone);
                break;
            case PlaystationNetwork:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Reddit:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default);
                break;
            case Skype:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Slack:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case SMS:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_sms);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_sms);
                break;
            case Snapchat:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Soundcloud:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Spotify:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Steam:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Tumblr:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Twitch:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.username);
                _channelLabelHint = getString(R.string.label);
                break;
            case Twitter:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default);
                break;
            case Venmo:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Web:
            case URL:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_web);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_web);
                break;
            case Whatsapp:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_phone);
                _channelLabelHint = getString(R.string.label);
                break;
            case XboxLive:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.gamertag);
                _channelLabelHint = getString(R.string.label);
                break;
            case Yo:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            case Youtube:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(
                    R.string.dialog_channel_hint_address_blank_handle, label);
                _channelLabelHint = getString(R.string.label);
                break;
            default:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default);
                break;
        }
    }

    /**
     * Initialize values for EditText to switch color.
     */
    private void initializeEditText() {
        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        editTextActionAddress.setText(_channel.actionAddress());
        floatLabelAddress.setHint(_channelAddressHint);

        if (_channel.channelType().equals(ChannelType.Facebook)) {
            editTextLabel.setVisibility(View.GONE);
            floatLabelChannelLabel.setVisibility(View.GONE);
        } else {
            editTextLabel.setText(_channel.label());
            floatLabelChannelLabel.setHint(_channelLabelHint);
        }
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
