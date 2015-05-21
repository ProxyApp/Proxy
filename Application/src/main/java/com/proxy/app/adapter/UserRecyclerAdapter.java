package com.proxy.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.model.User;
import com.proxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.InjectView;

import static com.proxy.util.ObjectUtils.joinWithSpace;


/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final ItemClickListener _clickListener;
    //Persisted User Array Data
    private ArrayList<User> _users;

    public UserRecyclerAdapter(ItemClickListener listener) {
        _clickListener = listener;
        _users = new ArrayList<>();
    }

    /**
     * Create a newInstance of a {@link UserRecyclerAdapter} with blank data.
     *
     * @return an {@link UserRecyclerAdapter} with no data
     */
    public static UserRecyclerAdapter newInstance(ItemClickListener listener) {
        return new UserRecyclerAdapter(listener);
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
        Picasso.with(context).load(user.imageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return _users.size();
    }

    public void setUsers(ArrayList<User> users){
        _users = users;
        notifyDataSetChanged();
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
        @InjectView(R.id.adapter_user_name)
        protected TextView userName;
        @InjectView(R.id.adapter_user_image)
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
