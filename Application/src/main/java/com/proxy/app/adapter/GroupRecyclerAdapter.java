package com.proxy.app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.model.Group;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An Adapter to handle displaying {@link Group}s.
 */
public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {

    //Persisted Group Array Data
    private ArrayList<Group> mGroups;

    /**
     * Constructor for {@link GroupRecyclerAdapter}.
     *
     * @param groups a list of {@link Group}s
     */
    private GroupRecyclerAdapter(@NonNull ArrayList<Group> groups) {
        mGroups = groups;
    }

    /**
     * Create a newInstance of a {@link GroupRecyclerAdapter} with blank data.
     *
     * @return an {@link GroupRecyclerAdapter} with no data
     */
    public static GroupRecyclerAdapter newInstance() {
        return new GroupRecyclerAdapter(new ArrayList<Group>());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.common_adapter_text_item, parent, false);
        return ViewHolder.newInstance(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Group group = getItemData(position);
        setLineItemViewData(holder, group);
    }

    /**
     * Set this ViewHolders underlying {@link Group} data.
     *
     * @param holder {@link Group} {@link ViewHolder}
     * @param group  the {@link Group} data
     */
    private void setLineItemViewData(final ViewHolder holder, Group group) {
        holder.groupName.setText(group.name());
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
    public ArrayList<Group> getDataArray() {
        return mGroups;
    }

    /**
     * Get the {@link Group} array.
     *
     * @param groups {@link Group} array
     */
    public void setDataArray(ArrayList<Group> groups) {
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
     * ViewHolder for the entered {@link Group} data.
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.adapter_group_name)
        protected TextView groupName;

        /**
         * Constructor for the holder.
         *
         * @param view the inflated view
         */
        private ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link Group} ViewHolder instance
         */
        public static ViewHolder newInstance(View view) {
            return new ViewHolder(view);
        }
    }
}
