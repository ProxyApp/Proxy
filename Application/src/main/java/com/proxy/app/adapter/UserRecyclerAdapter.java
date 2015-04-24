package com.proxy.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.model.User;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.InjectView;


/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> implements
    Filterable {
    //Persisted User Array Data
    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<User> mAllUsers = new ArrayList<>();

    /**
     * Create a newInstance of a {@link UserRecyclerAdapter} with blank data.
     *
     * @return an {@link UserRecyclerAdapter} with no data
     */
    public static UserRecyclerAdapter newInstance() {
        return new UserRecyclerAdapter();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_item, parent, false);
        return UserViewHolder.newInstance(view);
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
        Context context = holder.view.getContext();

        holder.userName.setText(user.getFirstName() + " " + user.getLastName());
        Picasso.with(context).load(user.getImageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * Get the {@link User} array.
     *
     * @param users {@link User} array
     */
    public void setDataArray(ArrayList<User> users) {
        mUsers = users;
    }

    /**
     * Get the desired {@link User} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public User getItemData(int position) {
        return mUsers.get(position);
    }

    /**
     * Add {@link User} to this RecyclerView.Adapter's array data set at the end of the set.
     *
     * @param user the {@link User} to add
     */
    public void addUserData(@NonNull User user) {
        synchronized (UserRecyclerAdapter.class) {
            mUsers.add(mUsers.size(), user);
            mAllUsers.add(user);
        }
    }

    @Override
    public Filter getFilter() {
        return new UserFilter();
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
         * @param view the inflated view
         */
        private UserViewHolder(View view) {
            super(view);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link User} ViewHolder instance
         */
        public static UserViewHolder newInstance(View view) {
            return new UserViewHolder(view);
        }
    }

    /**
     * Filter for searching this adapters {@link User}s.
     */
    public class UserFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // Initiate our results objects
            FilterResults results = new FilterResults();
            ArrayList<User> filteredUsers = new ArrayList<>();

            if (constraint == null || constraint.equals("")) {
                results.values = mAllUsers;
                results.count = mAllUsers.size();
            } else {
                // Make sure we're searching plain lower case strings
                constraint = constraint.toString().toLowerCase();
                for (User user : mAllUsers) {
                    String firstName = user.getFirstName().toLowerCase();
                    String lastName = user.getLastName().toLowerCase();
                    String fullName = firstName + " " + lastName;
                    if (firstName.contains(constraint)
                        || lastName.contains(constraint)
                        || fullName.contains(constraint)) {
                        filteredUsers.add(user);
                    }
                }
                results.values = filteredUsers;
                results.count = filteredUsers.size();
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUsers = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    }
}
