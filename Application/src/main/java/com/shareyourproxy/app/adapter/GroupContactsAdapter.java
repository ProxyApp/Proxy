package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import butterknife.Bind;

import static com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder;
import static com.shareyourproxy.util.ViewUtils.getUserImageHierarchy;

/**
 * Created by Evan on 11/10/15.
 */
public class GroupContactsAdapter extends SortedRecyclerAdapter<User> {
    private final ItemClickListener _clickListener;

    public GroupContactsAdapter(BaseRecyclerView recyclerView, ItemClickListener listener) {
        super(User.class, recyclerView);
        _clickListener = listener;
    }

    /**
     * Create a newInstance of a {@link GroupContactsAdapter} with blank data.
     *
     * @return an {@link GroupContactsAdapter} with no data
     */
    public static GroupContactsAdapter newInstance(
        BaseRecyclerView recyclerView, ItemClickListener listener) {
        return new GroupContactsAdapter(recyclerView, listener);
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
        Context context = holder.view.getContext();
        holder.userName.setText(user.fullName());
        String profileURL = user.profileURL();

        holder.userImage.setHierarchy(getUserImageHierarchy(context));
        holder.userImage.setController(newDraweeControllerBuilder()
            .setUri(profileURL == null ? null : Uri.parse(profileURL))
            .setAutoPlayAnimations(true)
            .build());
    }

    @Override
    protected int compare(User item1, User item2) {
        int comapreFirst = item1.first().compareToIgnoreCase(item2.first());

        if (comapreFirst == 0) {
            return item1.last().compareToIgnoreCase(item2.last());
        } else {
            return comapreFirst;
        }
    }

    @Override
    protected boolean areContentsTheSame(User item1, User item2) {
        return item1.id().equals(item2.id());
    }

    @Override
    protected boolean areItemsTheSame(User item1, User item2) {
        return item1.id().equals(item2.id());
    }

    /**
     * ViewHolder for the entered {@link User} data.
     */
    public static class UserViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_user_name)
        public TextView userName;
        @Bind(R.id.adapter_user_image)
        public SimpleDraweeView userImage;

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