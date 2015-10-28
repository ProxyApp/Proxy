package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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

import butterknife.Bind;

import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.INVITE_FRIENDS;

/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserContactsAdapter extends NotificationRecyclerAdapter<User> {
    private final ItemClickListener _clickListener;

    public UserContactsAdapter(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemClickListener listener) {
        super(User.class, recyclerView, showHeader, false, sharedPreferences);
        _clickListener = listener;
    }

    /**
     * Create a newInstance of a {@link UserContactsAdapter} with blank data.
     *
     * @return an {@link UserContactsAdapter} with no data
     */
    public static UserContactsAdapter newInstance(
        BaseRecyclerView recyclerView, SharedPreferences sharedPreferences, boolean showHeader,
        ItemClickListener listener) {
        return new UserContactsAdapter(recyclerView, sharedPreferences, showHeader, listener);
    }

    @Override
    protected BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_item, parent, false);
        return UserViewHolder.newInstance(view, _clickListener);
    }

    @Override
    protected int compare(User item1, User item2) {
        int compareFirst = item1.first().compareToIgnoreCase(item2.first());
        if (compareFirst == 0) {
            return item1.last().compareToIgnoreCase(item2.last());
        } else {
            return compareFirst;
        }
    }

    @Override
    protected boolean areContentsTheSame(User item1, User item2) {
        return (item1.id().equals(item2.id()));
    }

    @Override
    protected boolean areItemsTheSame(User item1, User item2) {
        return (item1.id().equals(item2.id()));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeaderViewData((HeaderViewHolder) holder, INVITE_FRIENDS, true, true);
        } else {
            setItemViewData((UserViewHolder) holder, getItemData(position));
        }
    }

    /**
     * Set this ViewHolders underlying {@link User} data.
     *
     * @param holder {@link User} {@link BaseViewHolder}
     * @param user   the {@link User} data
     */
    private void setItemViewData(UserViewHolder holder, User user) {
        Context context = holder.view.getContext();
        holder.userName.setText(user.fullName());
        String profileURL = user.profileURL();
        if (profileURL != null && !profileURL.isEmpty() && !profileURL.contains(".gif")) {
            Picasso.with(context).load(profileURL)
                .placeholder(R.mipmap.ic_proxy)
                .transform(new CircleTransform())
                .into(holder.userImage);
        } else {
            Picasso.with(context).load(R.mipmap.ic_proxy)
                .into(holder.userImage);
        }
    }

    public void refreshUserList(HashMap<String, User> users) {
        refreshData(users.values());
    }

    /**
     * ViewHolder for the entered {@link User} data.
     */
    public static class UserViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_user_name)
        public TextView userName;
        @Bind(R.id.adapter_user_image)
        public ImageView userImage;

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
