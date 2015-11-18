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
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.GenerateShareLinkCommand;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ShareLinkAdapter;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

import static com.shareyourproxy.util.ObjectUtils.getSimpleName;

/**
 * Share links to group channels in your web profile.
 */
public class ShareLinkDialog extends BaseDialogFragment {

    private static final String TAG = getSimpleName(ShareLinkDialog.class);
    private static final String ARG_GROUPS = "com.shareyourproxy.sharelinkdialog.group";
    private final DialogInterface.OnClickListener _negativeClicked =
        getNegOnClickListener();
    @Bind(R.id.dialog_sharelink_message)
    TextView message;
    @Bind(R.id.dialog_sharelink_recyclerview)
    BaseRecyclerView recyclerView;
    @BindColor(R.color.common_text)
    int colorText;
    @BindColor(R.color.common_divider)
    int colorGray;
    @BindColor(R.color.common_blue)
    int colorBlue;
    private ShareLinkAdapter _adapter;
    private final DialogInterface.OnClickListener _positiveClicked =
        getPosOnClickListener();

    /**
     * Constructor.
     */
    public ShareLinkDialog() {
    }

    /**
     * Create a new instance of a {@link AddChannelDialog}.
     *
     * @return A {@link AddChannelDialog}
     */
    public static ShareLinkDialog newInstance(HashMap<String, Group> groups) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_GROUPS, groups);
        //create dialog instance
        ShareLinkDialog dialog = new ShareLinkDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private DialogInterface.OnClickListener getPosOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getRxBus().post(new GenerateShareLinkCommand(getLoggedInUser(), _adapter.getData()));
            }
        };
    }

    private DialogInterface.OnClickListener getNegOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_sharelink, null, false);
        ButterKnife.bind(this, view);

        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Widget_Proxy_App_Dialog)
            .setTitle(getString(R.string.dialog_sharelink_title))
            .setView(view)
            .setPositiveButton(getString(R.string.share), _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();


        message.setText(getString(R.string.dialog_sharelink_message));
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
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        _adapter = ShareLinkAdapter.newInstance(recyclerView, getGroups());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(_adapter);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Group> getGroups() {
        return (HashMap<String, Group>) getArguments().getSerializable(ARG_GROUPS);
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
    public ShareLinkDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }

}
