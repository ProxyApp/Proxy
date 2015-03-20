package com.proxy.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.proxy.R;
import com.proxy.model.Group;
import com.proxy.model.User;
import com.proxy.widget.transform.GlideCircleTransform;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Adapter to handle creating a drawer with a User Header and User Settings.
 */
public class DrawerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    private final User mUser = getUserVinny();
    private String[] VALUES;

    /**
     * Constructor for {@link DrawerRecyclerAdapter}.
     */
    private DrawerRecyclerAdapter(String[] settingsArray) {
        VALUES = settingsArray;
    }

    /**
     * Create a newInstance of a {@link DrawerRecyclerAdapter} with blank data.
     *
     * @return an {@link DrawerRecyclerAdapter} with no data
     */
    public static DrawerRecyclerAdapter newInstance(String[] settingsArray) {
        return new DrawerRecyclerAdapter(settingsArray);
    }

    /**
     * Build a user to mock "currentUser".
     *
     * @return Vinny
     */
    private User getUserVinny() {
        return User.builder().firstName("Vinny").lastName("Bucchino").email("vinny@gmail.com")
            .userImageURL("http://upload.wikimedia.org/wikipedia/commons/0/0d/Astronaut_adam.jpg")
            .build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_drawer_header, parent, false);
            return HeaderViewHolder.newInstance(view, parent.getContext());
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_adapter_text_item, parent, false);
            return ItemViewHolder.newInstance(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            Context context = viewHolder.view.getContext();
            viewHolder.userName.setText(mUser.firstName() + " " + mUser.lastName());
            Glide.with(context).load(mUser.userImageURL())
                .transform(new GlideCircleTransform(context))
                .placeholder(R.drawable.adam).error(R.drawable.evan)
                .listener(getGlideListener(viewHolder)).into(viewHolder.userImage);

            viewHolder.backgroundContainer.setBackgroundColor(
                viewHolder.view.getContext().getResources().getColor(R.color.common_deep_purple));
        } else {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.name.setText(VALUES[position - 1]);
        }
    }


    /**
     * Create a new target to load bitmaps into.
     *
     * @param holder viewHolder
     * @return Target
     */
    private RequestListener<String, GlideDrawable> getGlideListener(final HeaderViewHolder holder) {
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
    public int getItemViewType(int position) {
        return position == TYPE_HEADER ? TYPE_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        // +1 for the header
        return VALUES.length + 1;
    }

    /**
     * Get Settings name.
     *
     * @param position position of item
     * @return VALUES string
     */

    public String getSettingValue(int position) {
        return VALUES[position - 1];
    }

    /**
     * ViewHolder for the settings header.
     */
    protected static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.adapter_drawer_header_container)
        protected LinearLayout backgroundContainer;
        @InjectView(R.id.adapter_drawer_header_image)
        protected ImageView userImage;
        @InjectView(R.id.adapter_drawer_header_name)
        protected TextView userName;
        protected View view;

        /**
         * Constructor for the HeaderViewHolder.
         *
         * @param view the inflated view
         */
        private HeaderViewHolder(View view, Context context) {
            super(view);
            ButterKnife.inject(this, view);
            this.view = view;
        }

        /**
         * Create a new Instance of the HeaderViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link Group} ViewHolder instance
         */
        public static HeaderViewHolder newInstance(View view, Context context) {
            return new HeaderViewHolder(view, context);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.adapter_group_name)
        protected TextView name;
        protected View view;


        /**
         * Constructor for the ItemViewHolder.
         *
         * @param view the inflated view
         */
        private ItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            this.view = view;
        }

        /**
         * Create a new Instance of the ItemViewHolder.
         *
         * @param view inflated in {@link #onCreateViewHolder}
         * @return a {@link Group} ViewHolder instance
         */
        public static ItemViewHolder newInstance(View view) {
            return new ItemViewHolder(view);
        }
    }


}
