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
    private static final String HEADER = "HEADER";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    private final User mUser = getUserVinny();
    private String[] mValues;

    /**
     * Constructor for {@link DrawerRecyclerAdapter}.
     *
     * @param settingsArray array of drawer options
     */
    private DrawerRecyclerAdapter(String[] settingsArray) {
        mValues = settingsArray;
    }

    /**
     * Create a newInstance of a {@link DrawerRecyclerAdapter} with blank data.
     *
     * @param settingsArray array of drawer options
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
            .userImageURL("http://i.imgur.com/DvpvklR.png")
            .build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_drawer_header, parent, false);
            return HeaderViewHolder.newInstance(view);
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
                .bitmapTransform(GlideCircleTransform.create(Glide.get(context).getBitmapPool()))
                .crossFade()

                .into(viewHolder.userImage);

            viewHolder.backgroundContainer.setBackgroundColor(
                context.getResources().getColor(R.color.common_deep_purple));
        } else {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.name.setText(mValues[position - 1]);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position == TYPE_HEADER ? TYPE_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        // +1 for the header
        return mValues.length + 1;
    }

    /**
     * Get Settings name.
     *
     * @param position position of item
     * @return mValues string
     */

    public String getSettingValue(int position) {
        if (position == 0) {
            return HEADER;
        } else {
            return mValues[position - 1];
        }
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
        private HeaderViewHolder(View view) {
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
        public static HeaderViewHolder newInstance(View view) {
            return new HeaderViewHolder(view);
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
