package com.proxy.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.model.User;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.InjectView;

/**
 * Adapter to handle creating a drawer with a User Header and User Settings.
 */
public class DrawerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String HEADER = "HEADER";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    private final User mUser;
    private String[] mValues;
    private Target mTarget;

    /**
     * Constructor for {@link DrawerRecyclerAdapter}.
     *
     * @param currentUser   currently logged in User
     * @param settingsArray array of drawer options
     */
    private DrawerRecyclerAdapter(User currentUser, String[] settingsArray) {
        mValues = settingsArray;
        mUser = currentUser;
    }

    /**
     * Create a newInstance of a {@link DrawerRecyclerAdapter} with blank data.
     *
     * @param currentUser   currently Logged in {@link User}
     * @param settingsArray array of drawer options
     * @return an {@link DrawerRecyclerAdapter} with no data
     */
    public static DrawerRecyclerAdapter newInstance(User currentUser, String[] settingsArray) {
        return new DrawerRecyclerAdapter(currentUser, settingsArray);
    }

    /**
     * Async return when palette has been loaded.
     *
     * @param viewHolder viewholder to manipulate
     * @return palette listener
     */
    private Palette.PaletteAsyncListener getPaletteAsyncListener(
        final HeaderViewHolder viewHolder) {
        return new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Context context = viewHolder.view.getContext();
                viewHolder.backgroundContainer.setBackgroundColor(
                    palette.getVibrantColor(
                        context.getResources().getColor(R.color.common_deep_purple)));
            }
        };
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
            viewHolder.userName.setText(mUser.getFirstName() + " " + mUser.getLastName());

            Picasso.with(context).load(mUser.getImageURL())
                .transform(CircleTransform.create())
                .placeholder(R.mipmap.ic_proxy)
                .into(getBitmapTargetView(viewHolder));

        } else {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.name.setText(mValues[position - 1]);
        }
    }

    /**
     * Strong Reference Bitmap Target.
     *
     * @param viewHolder viewHolder to load bitmap into
     * @return target
     */
    private Target getBitmapTargetView(final HeaderViewHolder viewHolder) {
        if (mTarget == null) {
            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    viewHolder.userImage.setImageBitmap(bitmap);
                    Palette.generateAsync(bitmap, getPaletteAsyncListener(viewHolder));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    Palette.generateAsync(bitmap, getPaletteAsyncListener(viewHolder));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    Palette.generateAsync(bitmap, getPaletteAsyncListener(viewHolder));
                }
            };
        }
        return mTarget;
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
    protected static class HeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_drawer_header_container)
        protected LinearLayout backgroundContainer;
        @InjectView(R.id.adapter_drawer_header_image)
        protected ImageView userImage;
        @InjectView(R.id.adapter_drawer_header_name)
        protected TextView userName;

        /**
         * Constructor for the HeaderViewHolder.
         *
         * @param view the inflated view
         */
        private HeaderViewHolder(View view) {
            super(view);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static HeaderViewHolder newInstance(View view) {
            return new HeaderViewHolder(view);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    protected static class ItemViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_group_name)
        protected TextView name;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param view the inflated view
         */
        private ItemViewHolder(View view) {
            super(view);
        }
        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static ItemViewHolder newInstance(View view) {
            return new ItemViewHolder(view);
        }
    }


}
