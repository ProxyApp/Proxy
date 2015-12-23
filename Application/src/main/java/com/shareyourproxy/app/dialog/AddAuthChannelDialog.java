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

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ObjectUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add a channel that requires OAuth.
 */
public class AddAuthChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL = "AddAuthChannelDialog.Channel";
    private static final String TAG = Companion.getSimpleName(AddAuthChannelDialog.class);
    private final DialogInterface.OnClickListener _helpClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                IntentLauncher.INSTANCE.launchFacebookHelpIntent(getActivity());
            }
        };
    @Bind(R.id.dialog_channel_auth_action_address_edittext)
    EditText editTextActionAddress;
    private final DialogInterface.OnClickListener _negativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                dialogInterface.dismiss();
            }
        };
    @Bind(R.id.dialog_channel_auth_action_address_floatlabel)
    TextInputLayout floatLabelAddress;
    @BindColor(R.color.common_text)
    int colorText;
    @BindColor(R.color.common_blue)
    int colorBlue;
    private Channel _channel;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private final TextView.OnEditorActionListener _onEditorActionListener =
        new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    addUserChannel();
                    dismiss();
                    return true;
                }
                return false;
            }
        };
    private final DialogInterface.OnClickListener _positiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addUserChannel();
            }
        };

    /**
     * Constructor.
     */
    public AddAuthChannelDialog() {
    }

    /**
     * Create a new instance of a {@link AddAuthChannelDialog}.
     *
     * @return A {@link AddAuthChannelDialog
     */
    public static AddAuthChannelDialog newInstance(Channel channel) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CHANNEL, channel);
        //create dialog instance
        AddAuthChannelDialog dialog = new AddAuthChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void addUserChannel() {
        String actionContent = editTextActionAddress.getText().toString();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            Channel channel =
                createModelInstance(_channel, actionContent);
            getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel));
        }
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
            .inflate(R.layout.dialog_auth_channel, null, false);
        ButterKnife.bind(this, view);
        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Widget_Proxy_App_Dialog)
            .setTitle(getString(
                R.string.dialog_addchannel_title_add_blank, _channel.channelType().getLabel()))
            .setView(view)
            .setPositiveButton(getString(R.string.save), _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .setNeutralButton(R.string.common_help, _helpClicked)
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
        // Setup Button Colors
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText);
        // Set TextInput hint
        floatLabelAddress.setHint(getString(
            R.string.dialog_channel_hint_address_blank_handle,
            _channel.channelType().getLabel()));
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
    public AddAuthChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
