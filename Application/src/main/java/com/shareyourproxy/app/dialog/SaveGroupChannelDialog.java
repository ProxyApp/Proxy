package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.AddGroupChannelAndPublicCommand;
import com.shareyourproxy.api.rx.command.AddGroupsChannelCommand;
import com.shareyourproxy.api.rx.event.ChannelAddedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.SaveGroupChannelAdapter;
import com.shareyourproxy.util.ObjectUtils;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;


/**
 * Save a new channel to selected groups after creating it.
 */
public class SaveGroupChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL =
        "com.shareyourproxy.savegroupchanneldialog.arg.channel";
    private static final String ARG_USER = "com.shareyourproxy.savegroupchanneldialog.arg.user";
    private static final String TAG = ObjectUtils.Companion.getSimpleName(SaveGroupChannelDialog.class);
    @Bind(R.id.dialog_user_groups_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.dialog_user_groups_message)
    TextView message;
    @BindColor(R.color.common_text)
    int colorText;
    @BindColor(R.color.common_blue)
    int colorBlue;
    @BindString(R.string.select_groups_for_channel)
    String stringMessage;
    private Channel _channel;
    private User _user;
    private final DialogInterface.OnClickListener _negativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getRxBus().post(new ChannelAddedEvent(_user, _channel));
                dialogInterface.dismiss();
            }
        };
    private SaveGroupChannelAdapter _adapter;
    private final DialogInterface.OnClickListener _positiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchUpdatedUserGroups();
            }
        };

    /**
     * Constructor.
     */
    public SaveGroupChannelDialog() {
    }

    /**
     * Create a new instance of a {@link SaveGroupChannelDialog}.
     *
     * @return A {@link SaveGroupChannelDialog
     */
    public static SaveGroupChannelDialog newInstance(Channel channel, User user) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_USER, user);
        bundle.putParcelable(ARG_CHANNEL, channel);
        //create dialog instance
        SaveGroupChannelDialog dialog = new SaveGroupChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private void dispatchUpdatedUserGroups() {
        RxBusDriver rxBus = getRxBus();
        rxBus.post(new ChannelAddedEvent(_user, _channel));
        if (_adapter.isAnyItemChecked()) {
            ArrayList<GroupToggle> data = _adapter
                .getDataArray();
            if (_adapter.isPublicChecked()) {
                rxBus.post(new AddGroupChannelAndPublicCommand(
                    _user, data, _channel));
            } else {
                rxBus.post(
                    new AddGroupsChannelCommand(_user, data, _channel));
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channel = getArguments().getParcelable(ARG_CHANNEL);
        _user = getArguments().getParcelable(ARG_USER);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_user_groups, null, false);
        ButterKnife.bind(this, view);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Widget_Proxy_App_Dialog)
            .setTitle(R.string.select_groups)
            .setView(view)
            .setPositiveButton(getString(R.string.save), _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();

        message.setText(stringMessage);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
        initializeRecyclerView();
    }

    /**
     * Setup the group list UI.
     */
    private void initializeRecyclerView() {
        _adapter = SaveGroupChannelAdapter.newInstance(recyclerView, _user.groups());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(_adapter);
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
    public SaveGroupChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
