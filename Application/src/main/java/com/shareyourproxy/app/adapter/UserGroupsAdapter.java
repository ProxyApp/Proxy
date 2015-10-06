package com.shareyourproxy.app.adapter;

import android.support.v7.util.SortedList;
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

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.shareyourproxy.util.ObjectUtils.capitalize;

/**
 * Adapts the contactGroups that a user belongs to in a dialog.
 */
public class UserGroupsAdapter extends BaseRecyclerViewAdapter implements ItemClickListener {
    private SortedList<GroupToggle> _groups;
    private SortedList.Callback<GroupToggle> _sortedListCallback;

    private UserGroupsAdapter(ArrayList<GroupToggle> groups) {
        if (groups != null) {
            _groups = new SortedList<>(GroupToggle.class, getSortedCallback(), groups.size());
        } else {
            _groups = new SortedList<>(GroupToggle.class, getSortedCallback(), 0);
        }
        _groups.beginBatchedUpdates();
        _groups.addAll(groups);
        _groups.endBatchedUpdates();
    }

    public static UserGroupsAdapter newInstance(ArrayList<GroupToggle> groups) {
        return new UserGroupsAdapter(groups);
    }

    public SortedList.Callback<GroupToggle> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new SortedList.Callback<GroupToggle>() {

                @Override
                public int compare(GroupToggle item1, GroupToggle item2) {
                    String label1 = item1.getGroup().label();
                    String label2 = item2.getGroup().label();
                    return label1.compareToIgnoreCase(label2);
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
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
                public boolean areContentsTheSame(GroupToggle item1, GroupToggle item2) {
                    return item1.getGroup().equals(item2.getGroup())
                        && item1.isChecked() == item2.isChecked();
                }

                @Override
                public boolean areItemsTheSame(GroupToggle item1, GroupToggle item2) {
                    return item1.getGroup().equals(item2.getGroup());
                }
            };
        }
        return _sortedListCallback;
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
        holder.checkedTextView.setText(capitalize(getDataItem(position).getGroup().label()));
        holder.checkedTextView.setChecked(getDataItem(position).isChecked());
    }

    @Override
    public int getItemCount() {
        return _groups.size();
    }


    private GroupToggle getDataItem(int position) {
        return _groups.get(position);
    }

    @Override
    public void onItemClick(View view, int position) {
        CheckedTextView text = ButterKnife.findById(view, R.id.adapter_user_groups_textview);
        text.setChecked(!text.isChecked());
        GroupToggle group = getDataItem(position);
        group.setChecked(text.isChecked());
    }

    public ArrayList<GroupToggle> getDataArray() {
        ArrayList<GroupToggle> groups = new ArrayList<>(_groups.size());
        for (int i = 0; i < _groups.size(); ++i) {
            groups.add(_groups.get(i));
        }
        return groups;
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
