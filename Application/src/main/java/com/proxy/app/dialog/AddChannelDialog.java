package com.proxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.factory.ChannelFactory;
import com.proxy.api.domain.model.ChannelSection;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.event.ChannelAddedEvent;
import com.proxy.util.DebugUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

import static com.proxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Created by Evan on 5/5/15.
 */
public class AddChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType";
    private static final String ARG_CHANNEL_SECTION = "AddChannelDialog.ChannelSection";
    private static final String TAG = DebugUtils.getSimpleName(AddChannelDialog.class);
    @InjectView(R.id.dialog_addchannel_edittext)
    EditText mEditText;
    private final DialogInterface.OnClickListener mNegativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(mEditText);
                dialogInterface.dismiss();
            }
        };
    private int mGray;
    private int mGreen;
    private ChannelType mChannelType;
    private ChannelSection mChannelSection;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is
     * pressed.
     */
    private final TextView.OnEditorActionListener onEditorActionListener =
        new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
                // FixedDecimalEditText's inputType is Decimal
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    dispatchChannelEvent();
                    getDialog().dismiss();
                    return true;
                }
                return false;
            }
        };
    private final DialogInterface.OnClickListener mPositiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchChannelEvent();
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
        //create dialog instance
        AddChannelDialog dialog = new AddChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void dispatchChannelEvent() {
        String actionContent = mEditText.getText().toString();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            RealmChannel channel =
                ChannelFactory.createRealmInstance(mChannelType, mChannelSection, actionContent);
            getRxBus().post(new ChannelAddedEvent(channel));
        }
    }

    /**
     * If text is entered into the dialog {@link EditText}, change the background underline of the
     * widget.
     *
     * @param editable the string entered in the {@link EditText}
     */
    @OnTextChanged(value = R.id.dialog_addchannel_edittext,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    @SuppressWarnings("unused")
    public void afterTextChanged(Editable editable) {
        mEditText.getBackground().setColorFilter(
            !TextUtils.isEmpty(editable) ? mGreen : mGray, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mChannelType = ChannelType.valueOf(getArguments().getString(ARG_CHANNEL_TYPE));
        mChannelSection = ChannelSection.valueOf(getArguments().getString(ARG_CHANNEL_SECTION));
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_addchannel, null, false);
        ButterKnife.inject(this, view);
        mEditText.setOnEditorActionListener(onEditorActionListener);
        return new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(R.string.dialog_addchannel_title)
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
        initializeEditTextColors();
        mEditText.getBackground().setColorFilter(mGray, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Initialize values for EditText to switch color on in {@link AddGroupDialog#afterTextChanged}
     */
    private void initializeEditTextColors() {
        mGray = mEditText.getContext().getResources().getColor(R.color.common_divider);
        mGreen = mEditText.getContext().getResources().getColor(R.color.common_green);
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
