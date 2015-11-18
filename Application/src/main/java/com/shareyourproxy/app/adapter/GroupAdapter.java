package com.shareyourproxy.app.adapter;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import java.util.HashMap;

import butterknife.Bind;

import static com.shareyourproxy.api.domain.model.Group.createPublicGroup;
import static com.shareyourproxy.util.ObjectUtils.capitalize;
import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.MAIN_GROUPS;

/**
 * An Adapter to handle displaying {@link Group}s.
 */
public class GroupAdapter extends NotificationRecyclerAdapter<Group> {

    private final ItemClickListener _listener;

    /**
     * Constructor for {@link GroupAdapter}.
     */
    public GroupAdapter(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemClickListener listener) {
        super(Group.class, recyclerView, showHeader, false, sharedPreferences);
        _listener = listener;
    }

    /**
     * Create a newInstance of a {@link GroupAdapter} with blank data.
     *
     * @return an {@link GroupAdapter} with no data
     */
    public static GroupAdapter newInstance(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemClickListener listener) {
        return new GroupAdapter(recyclerView, sharedPreferences, showHeader, listener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof GroupViewHolder) {
            Group group = getItemData(position);
            setLineItemViewData((GroupViewHolder) holder, group);
        } else if (holder instanceof HeaderViewHolder) {
            bindHeaderViewData((HeaderViewHolder) holder, MAIN_GROUPS, true, false);
        }
    }

    /**
     * Set this ViewHolders underlying {@link Group} data.
     *
     * @param holder {@link Group} {@link GroupViewHolder}
     * @param group  the {@link Group} data
     */
    private void setLineItemViewData(final GroupViewHolder holder, Group group) {
        holder.groupName.setText(capitalize(group.label()));
    }

    @Override
    protected BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.common_adapter_text_item, parent, false);
        return GroupViewHolder.newInstance(view, _listener);
    }

    @Override
    protected int compare(Group item1, Group item2) {
        return item1.label().compareToIgnoreCase(item2.label());
    }

    @Override
    protected boolean areContentsTheSame(Group item1, Group item2) {
        //Sections will have the same ID but different categories
        return item1.id().equals(item2.id());
    }

    @Override
    protected boolean areItemsTheSame(Group item1, Group item2) {
        return (item1.id().equals(item2.id())
            && item1.label().equals(item2.label()));
    }


    public void refreshGroupData(final HashMap<String, Group> groups) {
        HashMap<String, Group> newGroups =
            new HashMap<String, Group>(groups.size()) {{putAll(groups);}};

        Group publicGroup = createPublicGroup();
        newGroups.put(publicGroup.id(), publicGroup);
        refreshData(newGroups.values());
    }

    /**
     * ViewHolder for the entered {@link Group} data.
     */
    public static class GroupViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_group_name)
        TextView groupName;

        /**
         * Constructor for the holder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for each viewholder item
         */
        private GroupViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for each viewholder item
         * @return a ViewHolder instance
         */
        public static GroupViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new GroupViewHolder(view, itemClickListener);
        }
    }
}
