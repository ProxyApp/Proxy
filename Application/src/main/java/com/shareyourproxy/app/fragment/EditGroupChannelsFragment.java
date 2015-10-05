package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.DeleteUserGroupCommand;
import com.shareyourproxy.api.rx.command.SaveGroupChannelsCommand;
import com.shareyourproxy.api.rx.command.SavePublicGroupChannelsCommand;
import com.shareyourproxy.app.EditGroupChannelsActivity;
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType;
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_EDIT_GROUP_TYPE;
import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.app.adapter.EditGroupChannelAdapter.TYPE_LIST_DELETE_FOOTER;

/**
 * Display a list of {@link Group} {@link Channel}s and whether they are in our out of the selected
 * groups permissions.
 */
public class EditGroupChannelsFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_group_edit_channel_recyclerview)
    protected RecyclerView recyclerView;

    private EditGroupChannelAdapter _adapter;

    /**
     * Constructor.
     */
    public EditGroupChannelsFragment() {
    }

    /**
     * Create a new instance of this fragment for the parent {@link EditGroupChannelsActivity}.
     *
     * @return EditGroupChannelsFragment
     */
    public static EditGroupChannelsFragment newInstance() {
        return new EditGroupChannelsFragment();
    }

    private Group getSelectedGroup() {
        return getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle state) {
        View rootView = inflater.inflate(R.layout.fragment_edit_group_channel, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        initializeRecyclerView();
        return rootView;
    }

    /**
     * Save and go back.
     *
     * @param groupLabel updated group name
     */
    private void saveGroupChannels(String groupLabel) {
        getRxBus().post(new SaveGroupChannelsCommand(getRxBus(),
            getLoggedInUser(), groupLabel, getSelectedGroup(),
            _adapter.getSelectedChannels(), getGroupEditType()));
        getActivity().onBackPressed();
    }

    /**
     * Save public channels and go back.
     */
    private void savePublicGroupChannels() {
        getRxBus().post(new SavePublicGroupChannelsCommand(getRxBus(), getLoggedInUser(),
            _adapter.getToggledChannels()));
        getActivity().onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Initialize the channel and group data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = EditGroupChannelAdapter.newInstance(this, getSelectedGroup().label(),
            getLoggedInUser().channels(), getSelectedGroup().channels(), getGroupEditType());
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getDismissScrollListener());
    }

    /**
     * Check whether this fragment is Adding a group, Editing a group, or a Public group.
     *
     * @return {@link GroupEditType} constants.
     */
    private GroupEditType getGroupEditType() {
        return (GroupEditType) getActivity().getIntent()
            .getExtras().getSerializable(ARG_EDIT_GROUP_TYPE);
    }

    @Override
    public void onItemClick(View view, int position) {
        int viewType = _adapter.getItemViewType(position);
        if (viewType == TYPE_LIST_DELETE_FOOTER) {
            getRxBus().post(new DeleteUserGroupCommand(getRxBus(),
                getLoggedInUser(), getSelectedGroup()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.menu_edit_group_channel_save:
                if (GroupEditType.PUBLIC_GROUP.equals(getGroupEditType())) {
                    savePublicGroupChannels();
                } else {
                    if(_adapter.getGroupLabel().trim().isEmpty()){
                        _adapter.promptGroupLabelError(getActivity());
                    }else {
                        saveGroupChannels(_adapter.getGroupLabel());
                    }
                }

                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

}
