package com.proxy.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.proxy.R;
import com.proxy.model.User;
import com.proxy.widget.transform.GlideCircleTransform;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {
    //Persisted User Array Data
    private ArrayList<User> mUsers;

    /**
     * Constructor for {@link UserRecyclerAdapter}.
     *
     * @param users a list of {@link User}s
     */
    private UserRecyclerAdapter(@NonNull ArrayList<User> users) {
        mUsers = users;
    }

    /**
     * Create a newInstance of a {@link UserRecyclerAdapter} with blank data.
     *
     * @return an {@link UserRecyclerAdapter} with no data
     */
    public static UserRecyclerAdapter newInstance() {
        return new UserRecyclerAdapter(new ArrayList<User>());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_item, parent, false);
        return ViewHolder.newInstance(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = getItemData(position);
        setLineItemViewData(holder, user);
    }

    /**
     * Set this ViewHolders underlying {@link User} data.
     *
     * @param holder {@link User} {@link ViewHolder}
     * @param user   the {@link User} data
     */
    private void setLineItemViewData(ViewHolder holder, User user) {
        Context context = holder.view.getContext();
        holder.userName.setText(user.firstName() + " " + user.lastName());
        Glide.with(context).load(user.userImageURL())
            .transform(new GlideCircleTransform(context))
            .placeholder(R.drawable.evan).error(R.drawable.evan)
            .listener(getGlideListener(holder)).into(holder.userImage);
    }

    /**
     * Create a new target to load bitmaps into.
     *
     * @param holder view holder
     * @return Target
     */
    private RequestListener<String, GlideDrawable> getGlideListener(final ViewHolder holder) {
        return new RequestListener<String, GlideDrawable>() {

            @Override
            public boolean onException(
                Exception e, String model, Target<GlideDrawable> target,
                boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(
                GlideDrawable resource, String model,
                Target<GlideDrawable> target, boolean isFromMemoryCache,
                boolean isFirstResource) {
                holder.userImage.setImageDrawable(resource);
                return false;
            }
        };

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * Get the {@link User} array.
     *
     * @return the desired ArrayList<{@link User}>
     */
    public ArrayList<User> getDataArray() {
        return mUsers;
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
        }
    }

    /**
     * ViewHolder for the entered {@link User} data.
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.adapter_user_name)
        protected TextView userName;
        @InjectView(R.id.adapter_user_image)
        protected ImageView userImage;
        protected View view;

        /**
         * Constructor for the holder.
         *
         * @param view the inflated view
         */
        private ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            this.view = view;
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link User} ViewHolder instance
         */
        public static ViewHolder newInstance(View view) {
            return new ViewHolder(view);
        }
    }
}
