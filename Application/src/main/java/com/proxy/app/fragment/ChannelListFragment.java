package com.proxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.ChannelListRecyclerAdapter;
import com.proxy.event.ChannelDialogRequestEvent;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelListFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {

    @InjectView(R.id.common_recyclerview)
    protected BaseRecyclerView mRecyclerView;
    private ChannelListRecyclerAdapter mAdapter;

    /**
     * Constructor.
     */
    public ChannelListFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return fragment
     */
    public static ChannelListFragment newInstance() {
        return new ChannelListFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_base_recyclerview, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
    }

    /**
     * Initialize a RecyclerView with User data.
     */
    private void initializeRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = ChannelListRecyclerAdapter.newInstance(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (!mAdapter.isSectionHeader(position)) {
            //section offset
            position = position - 1;
            getRxBus().post(new ChannelDialogRequestEvent((RealmChannel)mAdapter.getItemData(position)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
