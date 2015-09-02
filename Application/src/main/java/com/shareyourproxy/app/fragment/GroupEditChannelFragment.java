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
import com.shareyourproxy.app.GroupEditChannelActivity;
import com.shareyourproxy.app.adapter.GroupEditChannelAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_ADD_OR_EDIT;
import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.app.adapter.GroupEditChannelAdapter.TYPE_LIST_DELETE;

/**
 * Display a {@link Group}s {@link Channel}s and whether they are in our out of the groups
 * permissions.
 */
public class GroupEditChannelFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_group_edit_channel_recyclerview)
    protected RecyclerView recyclerView;

    private GroupEditChannelAdapter _adapter;

    /**
     * Constructor.
     */
    public GroupEditChannelFragment() {
    }

    /**
     * Create a new instance of this fragment for the parent {@link GroupEditChannelActivity}.
     *
     * @return GroupEditChannelFragment
     */
    public static GroupEditChannelFragment newInstance() {
        return new GroupEditChannelFragment();
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
        getRxBus().post(new SaveGroupChannelsCommand(
            getLoggedInUser(), groupLabel, getSelectedGroup(),
            _adapter.getSelectedChannels()));
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
        _adapter = GroupEditChannelAdapter.newInstance(this, getSelectedGroup().label(),
            getLoggedInUser().channels(), getSelectedGroup().channels(), getAddOrEdit());
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getDismissScrollListener());
    }

    /**
     * Check whether this fragment is adding a new group(0) or editing a saved group(1).
     *
     * @return {@link GroupEditChannelActivity#ADD_GROUP} {@link
     * GroupEditChannelActivity#EDIT_GROUP}
     * constants.
     */
    private int getAddOrEdit() {
        return getActivity().getIntent().getExtras().getInt(ARG_ADD_OR_EDIT, 0);
    }

    @Override
    public void onItemClick(View view, int position) {
        int viewType = _adapter.getItemViewType(position);
        if (viewType == TYPE_LIST_DELETE) {
            getRxBus().post(new DeleteUserGroupCommand(getLoggedInUser(), getSelectedGroup()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.menu_edit_group_channel_save:
                saveGroupChannels(_adapter.getGroupLabel());
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
