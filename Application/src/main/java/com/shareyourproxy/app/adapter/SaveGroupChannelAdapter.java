package com.shareyourproxy.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.shareyourproxy.api.domain.model.Group.createPublicGroup;
import static com.shareyourproxy.util.ObjectUtils.capitalize;

/**
 * Add a new channel to groups after its made.
 */
public class SaveGroupChannelAdapter extends SortedRecyclerAdapter<GroupToggle> implements
    ItemClickListener {
    private GroupToggle _publicGroup = new GroupToggle(createPublicGroup(), false);

    private SaveGroupChannelAdapter(
        BaseRecyclerView recyclerView, ArrayList<GroupToggle> groupToggles) {
        super(GroupToggle.class, recyclerView);
        groupToggles.add(_publicGroup);
        refreshGroupToggleData(groupToggles);

    }

    public static SaveGroupChannelAdapter newInstance
        (BaseRecyclerView recyclerView, HashMap<String, Group> groups) {
        ArrayList<GroupToggle> groupToggles = new ArrayList<>(groups.size());
        for (Map.Entry<String, Group> group : groups.entrySet()) {
            GroupToggle newEntry = new GroupToggle(group.getValue(), false);
            groupToggles.add(newEntry);
        }
        return new SaveGroupChannelAdapter(recyclerView, groupToggles);
    }

    private void refreshGroupToggleData(ArrayList<GroupToggle> groupToggles) {
        refreshData(groupToggles);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_groups_checklist, parent, false);
        return ContentViewHolder.newInstance(view, this);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindContentView((ContentViewHolder) holder, position);
    }

    private void bindContentView(ContentViewHolder holder, int position) {
        holder.checkedTextView.setText(capitalize(getItemData(position).getGroup().label()));
        holder.checkedTextView.setChecked(getItemData(position).isChecked());
    }

    @Override
    protected int compare(GroupToggle item1, GroupToggle item2) {
        if (item1.equals(_publicGroup)) {
            return 1;
        } else if (item2.equals(_publicGroup)) {
            return -1;
        }
        String label1 = item1.getGroup().label();
        String label2 = item2.getGroup().label();
        return label1.compareToIgnoreCase(label2);
    }

    @Override
    protected boolean areContentsTheSame(GroupToggle item1, GroupToggle item2) {
        return item1.getGroup().equals(item2.getGroup())
            && item1.isChecked() == item2.isChecked();
    }

    @Override
    protected boolean areItemsTheSame(GroupToggle item1, GroupToggle item2) {
        return item1.getGroup().equals(item2.getGroup());
    }

    @Override
    public void onItemClick(View view, int position) {
        CheckedTextView text = ButterKnife.findById(view, R.id.adapter_user_groups_textview);
        text.setChecked(!text.isChecked());
        GroupToggle group = getItemData(position);
        group.setChecked(text.isChecked());
    }

    public ArrayList<GroupToggle> getDataArray() {
        ArrayList<GroupToggle> groups = getData();
        if (!groups.isEmpty()) {
            groups.remove(_publicGroup);
        }
        return groups;
    }

    //public should always be the last item
    public boolean isPublicChecked() {
        return getLastItem().isChecked();
    }

    /**
     * ViewHolder for the entered {@link Group} data.
     */
    static class ContentViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_user_groups_textview)
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
