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

import static com.shareyourproxy.util.ObjectUtils.capitalize;

/**
 * Add a new channel to groups after its made.
 */
public class SaveGroupChannelAdapter extends BaseRecyclerViewAdapter implements ItemClickListener {
    private final GroupToggle _publicGroup;
    private ArrayList<GroupToggle> _groups;

    private SaveGroupChannelAdapter(ArrayList<GroupToggle> groups) {
        // There's a public group no matter what
        _publicGroup = new GroupToggle(Group.createPublicGroup(), false);
        groups.add(_publicGroup);
        _groups = groups;
    }

    public static SaveGroupChannelAdapter newInstance(HashMap<String, Group> groups) {
        ArrayList<GroupToggle> groupToggles = new ArrayList<>(groups.size());
        for (Map.Entry<String, Group> group : groups.entrySet()) {
            GroupToggle newEntry = new GroupToggle(group.getValue(), false);
            groupToggles.add(newEntry);
        }
        return new SaveGroupChannelAdapter(groupToggles);
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

    //public should always be the last item
    public boolean isPublicChecked(){
        return _groups.get(_groups.size()-1).isChecked();
    }

    @Override
    public void onItemClick(View view, int position) {
        CheckedTextView text = ButterKnife.findById(view, R.id.adapter_user_groups_textview);
        text.setChecked(!text.isChecked());
        GroupToggle group = getDataItem(position);
        group.setChecked(text.isChecked());
    }

    public ArrayList<GroupToggle> getDataArray() {
        ArrayList<GroupToggle> userGroups = _groups;
        userGroups.remove(_publicGroup);
        return userGroups;
    }

    /**
     * ViewHolder for the entered {@link Group} data.
     */
    static class ContentViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_user_groups_textview)
        protected CheckedTextView checkedTextView;

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
