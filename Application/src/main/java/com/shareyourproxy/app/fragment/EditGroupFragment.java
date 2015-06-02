package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.DeleteUserGroupCommand;
import com.shareyourproxy.api.rx.event.GroupChannelToggledEvent;
import com.shareyourproxy.app.adapter.EditGroupAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

public class EditGroupFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.fragment_group_edit_recyclerview)
    protected RecyclerView recyclerView;
    private EditGroupAdapter adapter;

    public EditGroupFragment() {
    }

    public static EditGroupFragment newInstance() {
        return new EditGroupFragment();
    }

    @OnClick(R.id.fragment_group_edit_delete)
    public void onClick() {
        getRxBus().post(new DeleteUserGroupCommand(getLoggedInUser(), getSelectedGroup()));
        Timber.i("Deleted group");
    }

    private Group getSelectedGroup() {
        return getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle state) {
        View rootView = inflater.inflate(R.layout.fragment_edit_group, container, false);
        ButterKnife.inject(this, rootView);
        initializeRecyclerView();
        return rootView;
    }

    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = EditGroupAdapter.newInstance(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(View view, int position) {
        int viewType = recyclerView.getChildViewHolder(view).getItemViewType();
        if (viewType == EditGroupAdapter.TYPE_LIST_ITEM) {
            Switch channelSwitch = ((EditGroupAdapter.ItemViewHolder)
                recyclerView.getChildViewHolder(view)).itemSwitch;
            channelSwitch.setChecked(!channelSwitch.isChecked());
            RxBusDriver.getInstance().post(
                new GroupChannelToggledEvent(adapter.getItemData(position).channel().id().value()));
            //todo send a message to the bus indicating the channel was changed
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
