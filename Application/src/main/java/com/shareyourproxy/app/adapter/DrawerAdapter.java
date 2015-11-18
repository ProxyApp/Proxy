package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
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
import com.shareyourproxy.widget.transform.AlphaTransform;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;

import static com.shareyourproxy.util.ViewUtils.getMenuIconDark;

/**
 * Adapter to handle creating a drawer with a User Header and User Settings.
 */
public class DrawerAdapter extends BaseRecyclerViewAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    private final ItemClickListener _clickListener;
    private final List<DrawerItem> _drawerItems;
    private User _currentUser;
    private Target _targetProfileImage;
    private Target _targetBackground;

    /**
     * Constructor for {@link DrawerAdapter}.
     *
     * @param currentUser currently logged in User
     * @param listener    click listener
     */
    private DrawerAdapter(User currentUser, ItemClickListener listener) {
        _drawerItems = Arrays.asList(
            DrawerItem.SHARE_PROFILE,
            DrawerItem.INVITE_FRIEND,
            DrawerItem.TOUR,
            DrawerItem.REPORT_ISSUE,
            DrawerItem.LOGOUT);

        _currentUser = currentUser;
        _clickListener = listener;
    }

    /**
     * Create a newInstance of a {@link DrawerAdapter} with blank data.
     *
     * @param currentUser currently Logged in {@link User}
     * @return an {@link DrawerAdapter} with no data
     */
    public static DrawerAdapter newInstance(User currentUser, ItemClickListener listener) {
        return new DrawerAdapter(currentUser, listener);
    }

    /**
     * Async return when palette has been loaded.
     *
     * @param viewHolder viewholder to manipulate
     * @return palette listener
     */
    private Palette.PaletteAsyncListener getPaletteAsyncListener(
        final HeaderViewHolder viewHolder, final int defualtColor) {
        return new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                if (_currentUser.coverURL() == null || _currentUser.coverURL().trim().isEmpty()) {
                    viewHolder.backgroundContainer
                        .setBackgroundColor(palette.getVibrantColor(defualtColor));
                }
            }
        };
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_drawer_header, parent, false);
            return HeaderViewHolder.newInstance(view, _clickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_drawer_item, parent, false);
            return ItemViewHolder.newInstance(view, _clickListener);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeaderViewHolder((HeaderViewHolder) holder);
        } else {
            bindItemViewHolder((ItemViewHolder) holder, position);
        }
    }

    /**
     * Bind Item View. Set the text of the menu option.
     *
     * @param holder   view holder
     * @param position in list
     */
    public void bindItemViewHolder(ItemViewHolder holder, int position) {
        Context context = holder.view.getContext();
        String name = getItemStringValue(context, position);
        int resId = getItemIconValue(position);

        holder.name.setText(name);
        holder.image.setImageDrawable(getMenuIconDark(context, resId));
    }

    /**
     * Bind a header view. Create a user profile and background with a title.
     *
     * @param holder view holder
     */
    public void bindHeaderViewHolder(HeaderViewHolder holder) {
        Context context = holder.view.getContext();
        if (_currentUser != null) {

            holder.userName.setText(_currentUser.fullName());
            String profileURL = _currentUser.profileURL();
            String coverURL = _currentUser.coverURL();

            if (profileURL != null && !profileURL.trim().isEmpty() && !profileURL.contains("" +
                ".gif")) {
                Picasso.with(context).load(profileURL)
                    .transform(CircleTransform.create())
                    .placeholder(R.mipmap.ic_proxy)
                    .into(getBitmapTargetView(holder));
            } else {
                Picasso.with(context).load(R.mipmap.ic_proxy)
                    .into(getBitmapTargetView(holder));
            }

            if (coverURL != null && !coverURL.trim().isEmpty() && !coverURL.contains(".gif")) {
                Picasso.with(context).load(coverURL)
                    .transform(AlphaTransform.create())
                    .into(getBackgroundTarget(holder));
            } else {
                Picasso.with(context).load(R.mipmap.ic_proxy)
                    .transform(AlphaTransform.create())
                    .into(getBackgroundTarget(holder));
            }
        }
    }

    private String getItemStringValue(Context context, int position) {
        return context.getString(_drawerItems.get(position - 1).getLabelRes());
    }

    private int getItemIconValue(int position) {
        return _drawerItems.get(position - 1).getResId();
    }

    private Target getBackgroundTarget(final HeaderViewHolder viewHolder) {
        if (_targetBackground == null) {
            _targetBackground = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    viewHolder.backgroundContainer.setBackground(
                        new BitmapDrawable(viewHolder.view.getContext().getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
        }
        return _targetBackground;
    }

    /**
     * Strong Reference Bitmap Target.
     *
     * @param viewHolder viewHolder to load bitmap into
     * @return target
     */
    private Target getBitmapTargetView(final HeaderViewHolder viewHolder) {
        if (_targetProfileImage == null) {
            _targetProfileImage = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap)
                        .generate(getPaletteAsyncListener(viewHolder, viewHolder.purple));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap)
                        .generate(getPaletteAsyncListener(viewHolder, viewHolder.purple));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap)
                        .generate(getPaletteAsyncListener(viewHolder, viewHolder.purple));
                }
            };
        }
        return _targetProfileImage;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        // +1 for the header
        return _drawerItems.size() + 1;
    }

    /**
     * Get Settings name.
     *
     * @param position position of item
     * @return _strings string
     */
    public DrawerItem getSettingValue(int position) {
        return (position == 0) ? DrawerItem.HEADER : _drawerItems.get(position - 1);
    }

    /**
     * Update the logged in user.
     *
     * @param user updated user
     */
    public void updateUser(User user) {
        _currentUser = user;
        notifyDataSetChanged();
    }

    public enum DrawerItem {
        HEADER(R.raw.ic_chameleon, R.string.header),
        PROFILE(R.raw.ic_account_circle, R.string.profile),
        SHARE_PROFILE(R.raw.ic_share, R.string.share_profile),
        INVITE_FRIEND(R.raw.ic_local_play, R.string.invite_a_friend),
        TOUR(R.raw.ic_explore, R.string.tour),
        REPORT_ISSUE(R.raw.ic_bug_report, R.string.report_issue),
        LOGOUT(R.raw.ic_exit_to_app, R.string.logout);

        private final int resId;
        private final int labelRes;

        DrawerItem(int iconRes, int labelRes) {
            this.resId = iconRes;
            this.labelRes = labelRes;
        }

        public int getResId() {
            return resId;
        }

        public int getLabelRes() {
            return labelRes;
        }

    }

    /**
     * ViewHolder for the settings header.
     */
    static class HeaderViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_drawer_header_container)
        LinearLayout backgroundContainer;
        @Bind(R.id.adapter_drawer_header_image)
        ImageView userImage;
        @Bind(R.id.adapter_drawer_header_name)
        TextView userName;
        @BindColor(R.color.common_deep_purple)
        int purple;

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
    static class ItemViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_drawer_item_name)
        TextView name;
        @Bind(R.id.adapter_drawer_item_image)
        ImageView image;

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
