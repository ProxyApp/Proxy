package com.proxy.app.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.rx.event.ChannelDialogRequestEvent;
import com.proxy.app.adapter.ChannelListRecyclerAdapter;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.proxy.app.adapter.BaseViewHolder.ItemClickListener;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelListFragment extends BaseFragment implements ItemClickListener {

    @InjectView(R.id.common_recyclerview)
    protected BaseRecyclerView recyclerView;
    private ChannelListRecyclerAdapter _adapter;

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
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = ChannelListRecyclerAdapter.newInstance(this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (!_adapter.isSectionHeader(position)) {
            //section offset
            position = position - 1;
            getRxBus().post(new ChannelDialogRequestEvent(_adapter.getItemData(position)));
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
