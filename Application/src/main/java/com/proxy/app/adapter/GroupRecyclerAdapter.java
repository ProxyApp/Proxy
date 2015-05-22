package com.proxy.app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.model.Group;
import com.proxy.app.adapter.BaseViewHolder.ItemClickListener;

import java.util.ArrayList;

import butterknife.InjectView;

/**
 * An Adapter to handle displaying {@link Group}s.
 */
public class GroupRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    //Persisted Group Array Data
    private ArrayList<Group> _groups;
    private ItemClickListener _listener;

    /**
     * Constructor for {@link GroupRecyclerAdapter}.
     *
     * @param groups a list of {@link Group}s
     */
    private GroupRecyclerAdapter(
        @NonNull ArrayList<Group> groups, ItemClickListener listener) {
        _groups = groups;
        _listener = listener;
    }

    /**
     * Create a newInstance of a {@link GroupRecyclerAdapter} with blank data.
     *
     * @param groups initialize {@link com.proxy.api.domain.model.User} {@link
     *               com.proxy.api.domain.model.Group}s
     * @return an {@link GroupRecyclerAdapter} with no data
     */
    public static GroupRecyclerAdapter newInstance(
        ArrayList<Group> groups, ItemClickListener listner) {
        return new GroupRecyclerAdapter(groups, listner);
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
     * Get the {@link Group} array.
     *
     * @return the desired ArrayList<{@link Group}>
     */
    public ArrayList<Group> getDataArray() {
        return _groups;
    }

    /**
     * Get the {@link Group} array.
     *
     * @param groups {@link Group} array
     */
    public void setDataArray(ArrayList<Group> groups) {
        _groups = groups;
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
        synchronized (GroupRecyclerAdapter.class) {
            _groups.add(_groups.size(), group);
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

    /**
     * Remove item at specified position.
     *
     * @param position of item to delete
     */
    public void removeGroupData(int position) {
        synchronized (GroupRecyclerAdapter.class) {
            if (_groups.size() > 0) {
                _groups.remove(position);
            }
        }
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
