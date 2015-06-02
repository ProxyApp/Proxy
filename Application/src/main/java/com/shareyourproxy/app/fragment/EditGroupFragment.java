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
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.GroupChannelToggled;
import com.shareyourproxy.api.rx.event.GroupDeleted;
import com.shareyourproxy.app.adapter.EditGroupRecyclerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

public class EditGroupFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.fragment_group_edit_recyclerview)
    protected RecyclerView recyclerView;
    private EditGroupRecyclerAdapter adapter;

    public EditGroupFragment() {
    }

    public static EditGroupFragment newInstance() {
        return new EditGroupFragment();
    }

    @OnClick(R.id.fragment_group_edit_delete)
    public void onClick() {
        getRxBus().post(new GroupDeleted());
        Timber.i("Deleted group");
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
        adapter = EditGroupRecyclerAdapter.newInstance(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(View view, int position) {
        int viewType = recyclerView.getChildViewHolder(view).getItemViewType();
        if (viewType == EditGroupRecyclerAdapter.TYPE_LIST_ITEM) {
            Switch channelSwitch = ((EditGroupRecyclerAdapter.ItemViewHolder)
                recyclerView.getChildViewHolder(view)).itemSwitch;
            channelSwitch.setChecked(!channelSwitch.isChecked());
            RxBusDriver.getInstance().post(
                new GroupChannelToggled(adapter.getItemData(position).channel().id().value()));
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
