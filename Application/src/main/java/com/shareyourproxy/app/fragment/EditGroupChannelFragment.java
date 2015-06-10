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
import com.shareyourproxy.app.adapter.EditGroupChannelAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

public class EditGroupChannelFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.fragment_group_edit_recyclerview)
    protected RecyclerView recyclerView;
    private EditGroupChannelAdapter adapter;

    public EditGroupChannelFragment() {
    }

    public static EditGroupChannelFragment newInstance() {
        return new EditGroupChannelFragment();
    }

    @OnClick(R.id.fragment_group_edit_delete)
    public void onClick() {
        getRxBus().post(new DeleteUserGroupCommand(getLoggedInUser(), getSelectedGroup()));
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
        adapter = EditGroupChannelAdapter.newInstance(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(View view, int position) {
            Switch channelSwitch = ((EditGroupChannelAdapter.ItemViewHolder)
                recyclerView.getChildViewHolder(view)).itemSwitch;
            channelSwitch.setChecked(!channelSwitch.isChecked());
            RxBusDriver.getInstance().post(
                new GroupChannelToggledEvent(
                    adapter.getItemData(position).getChannel().id().value()));
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
