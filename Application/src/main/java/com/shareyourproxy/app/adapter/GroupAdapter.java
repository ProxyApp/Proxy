package com.shareyourproxy.app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.util.SortedList.Callback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import java.util.ArrayList;

import butterknife.InjectView;

/**
 * An Adapter to handle displaying {@link Group}s.
 */
public class GroupAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private final BaseRecyclerView _recyclerView;
    //Persisted Group Array Data
    private SortedList<Group> _groups;
    private ItemClickListener _listener;
    private Callback<Group> _sortedListCallback;

    /**
     * Constructor for {@link GroupAdapter}.
     *
     * @param groups a list of {@link Group}s
     */
    public GroupAdapter(
        BaseRecyclerView recyclerView, ArrayList<Group> groups, ItemClickListener listener) {
        _recyclerView = recyclerView;
        _groups = new SortedList<>(Group.class, getSortedCallback(), groups.size());
        addGroups(groups);
        _listener = listener;
    }

    /**
     * Create a newInstance of a {@link GroupAdapter} with blank data.
     *
     * @param groups initialize {@link com.shareyourproxy.api.domain.model.User} {@link
     *               com.shareyourproxy.api.domain.model.Group}s
     * @return an {@link GroupAdapter} with no data
     */
    public static GroupAdapter newInstance(
        BaseRecyclerView recyclerView,
        ArrayList<Group> groups, ItemClickListener listner) {
        return new GroupAdapter(recyclerView, groups, listner);
    }

    public Callback<Group> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new Callback<Group>() {

                @Override
                public int compare(Group o1, Group o2) {
                    return o1.label().compareTo(o2.label());
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                    _recyclerView.smoothScrollToPosition(position);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(Group oldItem, Group newItem) {
                    return (oldItem.id().equals(newItem.id())
                        && oldItem.label().equals(newItem.label()));
                }

                @Override
                public boolean areItemsTheSame(Group item1, Group item2) {
                    //Sections will have the same ID but different categories
                    return item1.id().value().equals(item2.id().value());
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.common_adapter_text_item, parent, false);
        return GroupViewHolder.newInstance(view, _listener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        Group group = getItemData(position);
        setLineItemViewData((GroupViewHolder) holder, group);
    }

    /**
     * Set this ViewHolders underlying {@link Group} data.
     *
     * @param holder {@link Group} {@link GroupViewHolder}
     * @param group  the {@link Group} data
     */
    private void setLineItemViewData(final GroupViewHolder holder, Group group) {
        holder.groupName.setText(group.label());
    }

    @Override
    public int getItemCount() {
        return _groups.size();
    }

    /**
     * Get the desired {@link Group} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link Group}
     */
    public Group getItemData(int position) {
        return _groups.get(position);
    }

    /**
     * Add {@link Group} to this Adapter's array data set at the end of the set.
     *
     * @param group the {@link Group} to add
     */
    public void addGroupData(@NonNull Group group) {
        synchronized (GroupAdapter.class) {
            _groups.add(group);
        }
    }

    /**
     * Get the desired {@link Group} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link Group}
     */
    public Group getGroupData(int position) {
        return _groups.get(position);
    }

    public void refreshGroupData(ArrayList<Group> groups) {
        _groups.clear();
        addGroups(groups);
    }

    private void addGroups(ArrayList<Group> groups) {
        _groups.beginBatchedUpdates();
        for (Group group : groups) {
            _groups.add(group);
        }
        _groups.endBatchedUpdates();
    }

    /**
     * ViewHolder for the entered {@link Group} data.
     */
    public static class GroupViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_group_name)
        protected TextView groupName;

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
