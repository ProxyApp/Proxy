package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.widget.transform.CircleTransform;
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
    private final User _currentUser;
    private String[] _values;
    private Target _target;
    private ItemClickListener _clickListener;

    /**
     * Constructor for {@link DrawerRecyclerAdapter}.
     *  @param currentUser   currently logged in User
     * @param settingsArray array of drawer options
     * @param listener
     */
    private DrawerRecyclerAdapter(User currentUser, String[] settingsArray,ItemClickListener
        listener) {
        _values = settingsArray;
        _currentUser = currentUser;
        _clickListener = listener;
    }

    /**
     * Create a newInstance of a {@link DrawerRecyclerAdapter} with blank data.
     *
     * @param currentUser   currently Logged in {@link User}
     * @param settingsArray array of drawer options
     * @return an {@link DrawerRecyclerAdapter} with no data
     */
    public static DrawerRecyclerAdapter newInstance(User currentUser, String[] settingsArray,
                                                    ItemClickListener listener) {
        return new DrawerRecyclerAdapter(currentUser, settingsArray, listener);
    }

    /**
     * Is the item at the specified position a header?
     *
     * @param position of item
     * @return is the item a header
     */
    public static boolean isHeader(int position) {
        return position == 0;
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
                Context context = viewHolder._view.getContext();
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
            return HeaderViewHolder.newInstance(view, _clickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_adapter_text_item, parent, false);
            return ItemViewHolder.newInstance(view, _clickListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            Context context = viewHolder._view.getContext();
            viewHolder.userName.setText(_currentUser.first() + " "
                + _currentUser.last());

            Picasso.with(context).load(_currentUser.imageURL())
                .transform(CircleTransform.create())
                .placeholder(R.mipmap.ic_proxy)
                .into(getBitmapTargetView(viewHolder));

        } else {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.name.setText(getItemValue(position));
        }
    }

    public String getItemValue(int position){
        return _values[position-1];
    }

    /**
     * Strong Reference Bitmap Target.
     *
     * @param viewHolder viewHolder to load bitmap into
     * @return target
     */
    private Target getBitmapTargetView(final HeaderViewHolder viewHolder) {
        if (_target == null) {
            _target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener(viewHolder));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener(viewHolder));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener(viewHolder));
                }
            };
        }
        return _target;
    }

    @Override
    public int getItemViewType(int position) {
        return position == TYPE_HEADER ? TYPE_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        // +1 for the header
        return _values.length + 1;
    }

    /**
     * Get Settings name.
     *
     * @param position position of item
     * @return _values string
     */

    public String getSettingValue(int position) {
        if (position == 0) {
            return HEADER;
        } else {
            return getItemValue(position);
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
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private HeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static HeaderViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new HeaderViewHolder(view, itemClickListener);
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
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }
    }
}
