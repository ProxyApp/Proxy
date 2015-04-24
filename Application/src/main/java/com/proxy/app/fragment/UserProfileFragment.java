package com.proxy.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxy.R;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.app.adapter.BaseViewHolder;
import com.proxy.app.adapter.ChannelGridRecyclerAdapter;
import com.proxy.event.ChannelSelectedEvent;
import com.proxy.widget.BaseRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.proxy.Constants.ARG_USER_SELECTED_PROFILE;

/**
 * Display a User or Contacts Profile.
 */
public class UserProfileFragment extends BaseFragment implements BaseViewHolder.ItemClickListener {

    public static final int SPAN_COUNT = 4;
    @InjectView(R.id.common_recyclerview)
    BaseRecyclerView mRecyclerView;
    private ChannelGridRecyclerAdapter mAdapter;
    private User mUser;

    /**
     * Constructor.
     */
    public UserProfileFragment() {
    }

    /**
     * Return new {@link UserProfileFragment} instance.
     *
     * @return fragment
     */
    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUser = activity.getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
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
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.isHeaderOrSection(position) ? manager.getSpanCount() : 1;
            }
        });
        mRecyclerView.setLayoutManager(manager);
        mAdapter = ChannelGridRecyclerAdapter.newInstance(mUser, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public final void onItemClick(View view, int position) {
        if (!mAdapter.isHeaderOrSection(position)) {
            position = position - 2;
            RealmChannel channel = mAdapter.getItemData(position);
            getRxBus().post(new ChannelSelectedEvent(channel));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mUser = getActivity().getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
//        mAdapter.updateChannels(mUser);
//        mAdapter.notifyDataSetChanged();
//    }

}
