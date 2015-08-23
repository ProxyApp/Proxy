package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;

import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;


/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final ItemClickListener _clickListener;
    //Persisted User Array Data
    private SortedList<User> _users;
    private SortedList.Callback<User> _sortedListCallback;
    private boolean _needsRefresh = true;

    public UserAdapter(ItemClickListener listener) {
        _clickListener = listener;
        _users = new SortedList<>(User.class, getSortedCallback());
    }

    /**
     * Create a newInstance of a {@link UserAdapter} with blank data.
     *
     * @return an {@link UserAdapter} with no data
     */
    public static UserAdapter newInstance(ItemClickListener listener) {
        return new UserAdapter(listener);
    }

    public SortedList.Callback<User> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new SortedList.Callback<User>() {
                @Override
                public int compare(User item1, User item2) {
                    int comapreFirst = item1.first().compareTo(item2.first());

                    if (comapreFirst == 0) {
                        return item1.last().compareTo(item2.last());
                    } else {
                        return comapreFirst;
                    }
                }

                @Override
                public void onInserted(int position, int count) {
                    if (_needsRefresh) {
                        notifyDataSetChanged();
                        _needsRefresh = false;
                    }
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    if (getItemCount() == 0) {
                        notifyDataSetChanged();
                        _needsRefresh = true;
                    } else {
                        notifyItemRangeRemoved(position, count);
                    }
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public boolean areContentsTheSame(User item1, User item2) {
                    // we dont compare resId because its probably going to be removed
                    return (item1.id().value().equals(item2.id().value())
                        && item1.first().equals(item2.first())
                        && item1.last().equals(item2.last())
                        && item1.email().equals(item2.email())
                        && item1.groups().equals(item2.groups())
                        && item1.contacts().equals(item2.contacts())
                        && item1.channels().equals(item2.channels()));
                }

                @Override
                public boolean areItemsTheSame(User item1, User item2) {
                    return (item1.id().value().equals(item2.id().value()));
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_item, parent, false);
        return UserViewHolder.newInstance(view, _clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        setItemViewData((UserViewHolder) holder, getItemData(position));
    }

    /**
     * Set this ViewHolders underlying {@link User} data.
     *
     * @param holder {@link User} {@link BaseViewHolder}
     * @param user   the {@link User} data
     */
    private void setItemViewData(UserViewHolder holder, User user) {
        Context context = holder._view.getContext();
        holder.userName.setText(joinWithSpace(new String[]{ user.first(),
            user.last() }));
        Picasso.with(context).load(user.profileURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return _users.size();
    }

    public void refreshUserList(HashMap<String, User> users) {
        _users.clear();
        _users.beginBatchedUpdates();
        for (Map.Entry<String, User> user : users.entrySet()) {
            _users.add(user.getValue());
        }
        _users.endBatchedUpdates();
    }

    /**
     * Get the desired {@link User} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public User getItemData(int position) {
        return _users.get(position);
    }

    /**
     * ViewHolder for the entered {@link User} data.
     */
    protected static class UserViewHolder extends BaseViewHolder {

        @Bind(R.id.adapter_user_name)
        protected TextView userName;
        @Bind(R.id.adapter_user_image)
        protected ImageView userImage;

        /**
         * Constructor for the holder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for each item
         */
        private UserViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link #onCreateViewHolder}
         * @param itemClickListener click listener for each ViewHolder item
         * @return a {@link User} ViewHolder instance
         */
        public static UserViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new UserViewHolder(view, itemClickListener);
        }
    }

}
