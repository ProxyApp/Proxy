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
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;

import butterknife.Bind;

import static com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder;
import static com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.LOADING;
import static com.shareyourproxy.util.ViewUtils.getUserImageHierarchy;


/**
 * An Adapter to handle displaying {@link User}s.
 */
public class SearchUserAdapter extends SortedRecyclerAdapter<User> {
    private final ItemClickListener _clickListener;
    private final BaseRecyclerView _recyclerView;
    private String _queryString = "";

    public SearchUserAdapter(BaseRecyclerView recyclerView, ItemClickListener listener) {
        super(User.class, recyclerView);
        _clickListener = listener;
        _recyclerView = recyclerView;
    }

    /**
     * Create a newInstance of a {@link SearchUserAdapter} with blank data.
     *
     * @return an {@link SearchUserAdapter} with no data
     */
    public static SearchUserAdapter newInstance(
        BaseRecyclerView recyclerView, ItemClickListener listener) {
        return new SearchUserAdapter(recyclerView, listener);
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
        holder.userName.setText(user.fullName());
        bindUserImage(holder, user);
    }

    private void bindUserImage(UserViewHolder holder, User user) {
        Context context = holder.view.getContext();
        String profileURL = user.profileURL();

        holder.userImage.setHierarchy(getUserImageHierarchy(context));
        holder.userImage.setController(newDraweeControllerBuilder()
            .setUri(profileURL == null ? null : Uri.parse(profileURL))
            .setAutoPlayAnimations(true)
            .build());
    }

    public void clearUserList() {
        getSortedList().clear();
        notifyDataSetChanged();
    }

    @Override
    protected int compare(User item1, User item2) {
        int compareFirst = sortQueriedString(item1, item2);
        if (compareFirst == 0) {
            return item1.fullName().compareToIgnoreCase(item2.fullName());
        } else {
            return compareFirst;
        }
    }

    public int sortQueriedString(User item1, User item2) {
        if (_queryString.length() > 1) {
            int item1Count = getStartsWithCount(item1.fullName());
            int item2Count = getStartsWithCount(item2.fullName());
            if (item1Count == item2Count) {
                return 0;
            } else if (item1Count > item2Count) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public int getStartsWithCount(String fullname) {
        int count = 0;
        char[] fullnameArray = fullname.toUpperCase().toCharArray();
        char[] queryStringArray = _queryString.toUpperCase().toCharArray();
        for (int i = 0; i < queryStringArray.length; i++) {
            if (fullnameArray[i] == queryStringArray[i]) {
                ++count;
            }
        }
        return count;
    }

    @Override
    protected boolean areContentsTheSame(User item1, User item2) {
        return item1.id().equals(item2.id());
    }

    @Override
    protected boolean areItemsTheSame(User item1, User item2) {
        return item1.id().equals(item2.id());
    }

    public void setQueryString(String queryString) {
        _queryString = queryString;
    }

    @Override
    protected void onRemoved(int position, int count) {
        if (getItemCount() == 0) {
            setNeedsRefresh(true);
            RecyclerViewDatasetChangedEvent event = new
                RecyclerViewDatasetChangedEvent(this, LOADING);
            _recyclerView.updateViewState(event);
        } else {
            notifyItemRangeRemoved(position, count);
        }
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
