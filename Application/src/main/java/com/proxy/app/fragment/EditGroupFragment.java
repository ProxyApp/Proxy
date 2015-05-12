package com.proxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.proxy.R;
import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmGroupEditChannel;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.EditGroupRecyclerAdapter;
import com.proxy.event.GroupChannelToggled;
import com.proxy.event.GroupDeleted;
import com.proxy.event.RxBusDriver;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class EditGroupFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {

    @InjectView(R.id.fragment_group_edit_recyclerview)
    protected RecyclerView recyclerView;
    Callback<User> userCallback = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            Timber.i("Changed the group permissions");
        }

        @Override
        public void failure(RetrofitError e) {
            Timber.i("Failed to update group permissions");
        }
    };
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
                new GroupChannelToggled(
                            ((RealmGroupEditChannel) adapter
                                    .getItemData(position)).getChannel().getChannelId()));
            //todo send a message to the bus indicating the channel was changed
            Timber.i("Toggle clicked!!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
