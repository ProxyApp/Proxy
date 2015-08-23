package com.shareyourproxy.app.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.rx.command.DeleteUserGroupCommand;
import com.shareyourproxy.api.rx.command.SaveGroupChannelsCommand;
import com.shareyourproxy.app.adapter.GroupEditChannelAdapter;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

public class GroupEditChannelFragment extends BaseFragment implements ItemClickListener {

    @Bind(R.id.fragment_group_edit_channel_recyclerview)
    protected RecyclerView recyclerView;
    @Bind(R.id.fragment_group_edit_channel_edittext)
    protected EditText editText;
    @BindColor(R.color.common_text_disabled)
    protected int _gray;
    private GroupEditChannelAdapter _adapter;

    public GroupEditChannelFragment() {
    }

    public static GroupEditChannelFragment newInstance() {
        return new GroupEditChannelFragment();
    }

    @OnClick(R.id.fragment_group_edit_channel_delete)
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
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        initializeRecyclerView();
        initializeEditTextInput();
        return rootView;
    }

    private void saveGroupChannels() {
        getRxBus().post(new SaveGroupChannelsCommand(
            getLoggedInUser(), editText.getText().toString(), getSelectedGroup(),
            _adapter.getSelectedChannels()));
        getActivity().onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initializeEditTextInput() {
        editText.getBackground().setColorFilter(_gray, PorterDuff.Mode.SRC_IN);
        editText.setText(getSelectedGroup().label());
    }

    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = GroupEditChannelAdapter.newInstance(
            this, getLoggedInUser().channels(), getSelectedGroup().channels());
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(getScrollListener());
    }

    private RecyclerView.OnScrollListener getScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideSoftwareKeyboard(getView());
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };
    }

    @Override
    public void onItemClick(View view, int position) {
        Switch channelSwitch = ((GroupEditChannelAdapter.ItemViewHolder)
            recyclerView.getChildViewHolder(view)).itemSwitch;
        boolean toggle = !channelSwitch.isChecked();
        channelSwitch.setChecked(toggle);
        _adapter.getItemData(position).setInGroup(channelSwitch.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.menu_edit_group_channel_save:
                saveGroupChannels();
                break;
            default:
                Timber.e("Option item selected is unknown");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemLongClick(View view, int position) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
