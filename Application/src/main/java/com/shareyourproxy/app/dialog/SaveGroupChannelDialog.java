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
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddGroupsChannelCommand;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserGroupsAdapter;
import com.shareyourproxy.util.ObjectUtils;
import com.shareyourproxy.util.ViewUtils;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;


/**
 * Save a new channel to selected groups after creating it.
 */
public class SaveGroupChannelDialog extends BaseDialogFragment {
    private static final String ARG_CHANNEL =
        "com.shareyourproxy.savegroupchanneldialog.arg.channel";
    private static final String ARG_USER = "com.shareyourproxy.savegroupchanneldialog.arg.user";
    private static final String TAG = ObjectUtils.getSimpleName(SaveGroupChannelDialog.class);
    @Bind(R.id.dialog_user_groups_recyclerview)
    protected BaseRecyclerView recyclerView;
    @BindColor(R.color.common_text)
    protected int _textColor;
    @BindColor(R.color.common_blue)
    protected int _blue;
    private Channel _channel;
    private User _user;
    private UserGroupsAdapter _adapter;

    private final DialogInterface.OnClickListener _positiveClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchUpdatedUserGroups();
            }
        };

    private void dispatchUpdatedUserGroups() {
        getRxBus().post(new AddGroupsChannelCommand(getRxBus(),_user,
            _adapter.getDataArray(), _channel));
    }

    private final DialogInterface.OnClickListener _negativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
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
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(R.string.select_groups_for_channel)
            .setView(view)
            .setPositiveButton(getString(R.string.save), _positiveClicked)
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
        AlertDialog dialog = (AlertDialog) getDialog();
        // Setup Button Colors
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _textColor);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), _textColor);
        initializeRecyclerView();
    }

    /**
     * Setup the group list UI.
     */
    private void initializeRecyclerView() {
        _adapter = UserGroupsAdapter.newInstance(_user.groups());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(_adapter);
        recyclerView.hasFixedSize();
        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        lp.height = (int) ViewUtils.dpToPx(getResources(), R.dimen.user_groups_dialog_height);
        recyclerView.setLayoutParams(lp);
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
    public SaveGroupChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
