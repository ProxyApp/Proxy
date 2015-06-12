package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import com.shareyourproxy.api.domain.factory.ContactFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddUserContactCommand;
import com.shareyourproxy.api.rx.command.DeleteUserContactCommand;
import com.shareyourproxy.api.rx.command.SaveGroupContactsCommand;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserGroupsAdapter;
import com.shareyourproxy.util.DebugUtils;
import com.shareyourproxy.util.ViewUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Group selection dialog for users to sort their contacts.
 */
public class UserGroupsDialog extends BaseDialogFragment {

    private static final String TAG = DebugUtils.getSimpleName(UserGroupsDialog.class);
    private static final String ARG_GROUPS = "com.shareyourproxy.app.dialog.UserGroupsList";
    private static final String ARG_USER = "com.shareyourproxy.app.dialog.User";
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    @InjectView(R.id.fragment_user_groups_recyclerview)
    protected BaseRecyclerView recyclerView;
    private UserGroupsAdapter _adapter;
    private final OnClickListener _positiveClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchUpdatedUserGroups();
                dialogInterface.dismiss();
            }
        };


    public UserGroupsDialog() {
    }

    /**
     * Create a new instance of a {@link AddGroupDialog}.
     *
     * @return A {@link AddGroupDialog}
     */
    public static UserGroupsDialog newInstance(
        @NonNull ArrayList<GroupEditContact> groups, @NonNull User user) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_GROUPS, groups);
        bundle.putParcelable(ARG_USER, user);
        //copy dialog instance
        UserGroupsDialog dialog = new UserGroupsDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private void dispatchUpdatedUserGroups() {
        User user = getUserArg();
        Contact contact = ContactFactory.createModelContact(user);
        if (_adapter.contactInGroup()) {
            getRxBus().post(new AddUserContactCommand(getLoggedInUser(), contact));
        } else {
            getRxBus().post(new DeleteUserContactCommand(getLoggedInUser(), contact));
        }
        getRxBus().post(new SaveGroupContactsCommand(
            getLoggedInUser(), _adapter.getDataArray(), contact));
    }

    private User getUserArg() {
        return getArguments().getParcelable(ARG_USER);
    }

    private ArrayList<GroupEditContact> getCheckedGroups() {
        return getArguments().getParcelableArrayList(ARG_GROUPS);
    }


    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_user_groups, null, false);
        ButterKnife.inject(this, view);
        String title = getString(R.string.dialog_edit_user_groups) + " " + getUserArg().first();
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(R.string.common_save, _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void initializeRecyclerView() {
        _adapter = UserGroupsAdapter.newInstance(getCheckedGroups());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(_adapter);
        recyclerView.hasFixedSize();
        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        lp.height = (int) ViewUtils.dpToPx(getResources(), R.dimen.user_groups_dialog_height);
        recyclerView.setLayoutParams(lp);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setTextColorResource(dialog.getButton(Dialog.BUTTON_POSITIVE), R.color.common_blue);
        setTextColorResource(dialog.getButton(Dialog.BUTTON_NEGATIVE), R.color.common_text);
        initializeRecyclerView();
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public UserGroupsDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }

}
