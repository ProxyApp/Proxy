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
import com.proxy.api.domain.realm.RealmUser;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;

import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.proxy.util.ObjectUtils.joinWithSpace;


/**
 * An Adapter to handle displaying {@link User}s.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final BaseViewHolder.ItemClickListener mClickListener;
    //Persisted User Array Data
    private Realm mRealm;
    private RealmResults<RealmUser> mUsers;

    public UserRecyclerAdapter(Realm realm, BaseViewHolder.ItemClickListener listener) {
        mRealm = realm;
        mUsers = realm.where(RealmUser.class).findAllSorted("lastName");
        mClickListener = listener;
    }

    /**
     * Create a newInstance of a {@link UserRecyclerAdapter} with blank data.
     *
     * @return an {@link UserRecyclerAdapter} with no data
     */
    public static UserRecyclerAdapter newInstance(Realm realm, BaseViewHolder.ItemClickListener
        listener) {
        return new UserRecyclerAdapter(realm, listener);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_user_item, parent, false);
        return UserViewHolder.newInstance(view, mClickListener);
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
    private void setItemViewData(UserViewHolder holder, RealmUser user) {
        Context context = holder.view.getContext();
        holder.userName.setText(joinWithSpace(new String[]{ user.getFirstName(),
            user.getLastName() }));
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
     * Get the desired {@link User} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public RealmUser getItemData(int position) {
        return mUsers.get(position);
    }

    public void updateSearchText(CharSequence constraint) {
        if (constraint.equals("")) {
            mUsers = mRealm.where(RealmUser.class).findAllSorted("lastName");
        } else {
            mUsers = mRealm.where(RealmUser.class)
                .contains("firstName", constraint.toString(), false)
                .or().contains("lastName", constraint.toString(), false)
                .or().contains("fullName", constraint.toString(), false)
                .findAllSorted("lastName");
        }
        notifyDataSetChanged();
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
