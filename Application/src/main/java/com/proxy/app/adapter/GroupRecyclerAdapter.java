package com.proxy.app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.model.Group;
import com.proxy.api.model.User;

import butterknife.InjectView;
import io.realm.RealmList;

/**
 * An Adapter to handle displaying {@link Group}s.
 */
public class GroupRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    //Persisted Group Array Data
    private RealmList<Group> mGroups;

    /**
     * Constructor for {@link GroupRecyclerAdapter}.
     *
     * @param groups a list of {@link Group}s
     */
    private GroupRecyclerAdapter(@NonNull RealmList<Group> groups) {
        mGroups = groups;
    }

    /**
     * Create a newInstance of a {@link GroupRecyclerAdapter} with blank data.
     *
     * @param groups initialize {@link User} {@link Group}s
     * @return an {@link GroupRecyclerAdapter} with no data
     */
    public static GroupRecyclerAdapter newInstance(RealmList<Group> groups) {
        return new GroupRecyclerAdapter(groups);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.common_adapter_text_item, parent, false);
        return GroupViewHolder.newInstance(view);
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
        holder.groupName.setText(group.getLabel());
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    /**
     * Get the {@link Group} array.
     *
     * @return the desired ArrayList<{@link Group}>
     */
    public RealmList<Group> getDataArray() {
        return mGroups;
    }

    /**
     * Get the {@link Group} array.
     *
     * @param groups {@link Group} array
     */
    public void setDataArray(RealmList<Group> groups) {
        mGroups = groups;
    }

    /**
     * Get the desired {@link Group} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link Group}
     */
    public Group getItemData(int position) {
        return mGroups.get(position);
    }

    /**
     * Add {@link Group} to this Adapter's array data set at the end of the set.
     *
     * @param group the {@link Group} to add
     */
    public void addGroupData(@NonNull Group group) {
        synchronized (GroupRecyclerAdapter.class) {
            mGroups.add(mGroups.size(), group);
        }
    }

    /**
     * Add {@link Group} to this Adapter's array data at the specified position.
     *
     * @param position position in the array
     * @param group    the {@link Group} to add
     */
    public void addGroupData(int position, @NonNull Group group) {
        synchronized (GroupRecyclerAdapter.class) {
            mGroups.add(position, group);
        }
    }

    /**
     * Get the desired {@link Group} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link Group}
     */
    public Group getGroupData(int position) {
        return mGroups.get(position);
    }

    /**
     * Remove item at specified position.
     *
     * @param position of item to delete
     */
    public void removeGroupData(int position) {
        synchronized (GroupRecyclerAdapter.class) {
            if (mGroups.size() > 0) {
                mGroups.remove(position);
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
         * @param view the inflated view
         */
        private GroupViewHolder(View view) {
            super(view);
        }
        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static GroupViewHolder newInstance(View view) {
            return new GroupViewHolder(view);
        }
    }
}
