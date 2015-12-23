package com.shareyourproxy.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;

import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.util.ObjectUtils.capitalize;

/**
 * Display a list of groups to broadcast in a shared link intent.
 */
public class ShareLinkAdapter extends SortedRecyclerAdapter<GroupToggle> implements ItemClickListener {

    private final BaseRecyclerView _recyclerView;
    private CheckedTextView _lastCheckedView;

    private ShareLinkAdapter(BaseRecyclerView recyclerView, ArrayList<GroupToggle> groups) {
        super(GroupToggle.class, recyclerView);
        _recyclerView = recyclerView;
        refreshData(groups);
    }

    public static ShareLinkAdapter newInstance(
        BaseRecyclerView recyclerView, HashMap<String, Group> groups) {
        ArrayList<GroupToggle> groupToggles = new ArrayList<>(groups.size());
        for (Group group : groups.values()) {
            groupToggles.add(GroupToggle.Companion.create(group, false));
        }
        return new ShareLinkAdapter(recyclerView, groupToggles);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_share_link, parent, false);
        return ContentViewHolder.newInstance(view, this);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindContentView((ContentViewHolder) holder, position);
    }

    private void bindContentView(ContentViewHolder holder, int position) {
        holder.checkedTextView.setText(Companion.capitalize(getItemData(position).getGroup().label()));
        holder.checkedTextView.setChecked(getItemData(position).isChecked());
    }

    @Override
    public void onItemClick(View view, int position) {
        //set data
        clearGroupState();
        getItemData(position).setChecked(true);

        //set view
        updateViewState(view);
    }

    public void updateViewState(View view) {
        //turn off the last checked view and cache
        if (_lastCheckedView != null && !_lastCheckedView.equals(view)) {
            _lastCheckedView.setChecked(false);
        }
        //set the selected view checked
        CheckedTextView checkedTextView = ((ContentViewHolder)
            _recyclerView.getChildViewHolder(view))
            .checkedTextView;
        checkedTextView.setChecked(true);

        _lastCheckedView = checkedTextView;
    }

    private void clearGroupState() {
        for (int i = 0; i < getItemCount(); i++) {
            getSortedList().get(i).setChecked(false);
        }
    }

    @Override
    protected int compare(GroupToggle item1, GroupToggle item2) {
        Group group1 = item1.getGroup();
        Group group2 = item2.getGroup();
        return group1.label().compareToIgnoreCase(group2.label());
    }

    @Override
    protected boolean areContentsTheSame(GroupToggle item1, GroupToggle item2) {
        Group group1 = item1.getGroup();
        Group group2 = item2.getGroup();
        return (group1.id().equals(group2.id())
            && group1.label().equals(group2.label()));
    }

    @Override
    protected boolean areItemsTheSame(GroupToggle item1, GroupToggle item2) {
        //Sections will have the same ID but different categories
        Group group1 = item1.getGroup();
        Group group2 = item2.getGroup();
        return group1.id().equals(group2.id());
    }

    static class ContentViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_share_link_textview)
        CheckedTextView checkedTextView;

        /**
         * Constructor for the holder.
         *
         * @param view the inflated view
         */
        private ContentViewHolder(View view, ItemClickListener listener) {
            super(view, listener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link User} ViewHolder instance
         */
        public static ContentViewHolder newInstance(View view, ItemClickListener listener) {
            return new ContentViewHolder(view, listener);
        }
    }
}
