package com.proxy.app.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.factory.ChannelFactory;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.api.domain.realm.RealmChannelType;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.InjectView;
import io.realm.RealmList;
import timber.log.Timber;

import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannelType;
import static com.proxy.api.domain.model.ChannelSection.General;
import static com.proxy.api.domain.model.ChannelType.Custom;
import static com.proxy.util.ObjectUtils.joinWithSpace;
import static com.proxy.util.ViewUtils.getActivityIcon;

/**
 * Adapter for a users profile and their {@link Channel} package permissions.
 */
public class ChannelGridRecyclerAdapter extends BaseRecyclerViewAdapter {

    public static final int DURATION = 900;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SECTION = 1;
    private static final int VIEW_TYPE_CONTENT = 2;
    private final BaseViewHolder.ItemClickListener mClickListener;
    private Target mTarget;
    private Palette.PaletteAsyncListener mPaletteListener;
    private User mUser;
    private SortedList.Callback<RealmChannel> mSortedListCallback;
    SortedList<RealmChannel> mChannels = new SortedList<>(RealmChannel.class,
        getSortedCallback());

    /**
     * Constructor for {@link ChannelGridRecyclerAdapter}.
     *
     * @param currentUser currently logged in User
     * @param listener
     */
    private ChannelGridRecyclerAdapter(User currentUser, BaseViewHolder.ItemClickListener listener) {
        mUser = currentUser;
        mClickListener = listener;
        updateChannels(currentUser);
    }

    /**
     * Create a newInstance of a {@link ChannelGridRecyclerAdapter} with blank data.
     *
     * @param currentUser currently Logged in {@link User}
     * @return an {@link ChannelGridRecyclerAdapter} with no data
     */
    public static ChannelGridRecyclerAdapter newInstance(User currentUser, BaseViewHolder.ItemClickListener listener) {
        return new ChannelGridRecyclerAdapter(currentUser, listener);
    }

    private void updateChannels(User user) {
        if (user != null) {
            mChannels.beginBatchedUpdates();
            RealmList<RealmChannel> realmChannels = ChannelFactory.getRealmChannels(user.channels
                ());

            for (RealmChannel realmChannel : realmChannels) {
                mChannels.add(realmChannel);
            }
            mChannels.endBatchedUpdates();
        }
    }

    public SortedList.Callback<RealmChannel> getSortedCallback() {
        if (mSortedListCallback == null) {
            mSortedListCallback = new SortedList.Callback<RealmChannel>() {


                @Override
                public int compare(RealmChannel o1, RealmChannel o2) {
                    //reverse order with the negative sign
                    return -o1.getChannelId().compareTo(o2.getChannelId());
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(RealmChannel oldItem, RealmChannel newItem) {
                    // we dont compare resId because its probably going to be removed
                    return (oldItem.getChannelId().equals(newItem.getChannelId())
                        && oldItem.getLabel().equals(newItem.getLabel())
                        && oldItem.getPackageName().equals(newItem.getPackageName())
                        && oldItem.getSection() == newItem.getSection()
                        && oldItem.getChannelType().equals(newItem.getChannelType()));
                }

                @Override
                public boolean areItemsTheSame(RealmChannel item1, RealmChannel item2) {
                    //Sections will have the same ID but different categories
                    return (item1.getChannelId().equals(item2.getChannelId())
                        && item1.getSection() == item2.getSection());
                }
            };
        }
        return mSortedListCallback;
    }

    /**
     * Async return when palette has been loaded.
     *
     * @param viewHolder viewholder to manipulate
     * @return palette listener
     */
    private Palette.PaletteAsyncListener getPaletteAsyncListener(
        final HeaderViewHolder viewHolder) {
        if (mPaletteListener == null) {
            mPaletteListener = new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    Resources res = viewHolder.view.getContext().getResources();

                    Integer colorFrom = Color.TRANSPARENT;
                    Integer colorTo = palette.getVibrantColor(
                        res.getColor(R.color.common_deep_purple));

                    ValueAnimator colorAnimation =
                        ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            viewHolder.userBackground.setBackgroundColor(
                                (Integer) animator.getAnimatedValue());
                        }

                    });
                    colorAnimation.setDuration(DURATION);
                    colorAnimation.start();
                }
            };
        }
        return mPaletteListener;
    }

    @Override
    public int getItemViewType(int position) {
        //Header is always the first item. Sections divide newly added channels.
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (position == 1) {
            return VIEW_TYPE_SECTION;
        } else {
            return VIEW_TYPE_CONTENT;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_grid_header, parent, false);
            return HeaderViewHolder.newInstance(view, mClickListener);
        } else if (viewType == VIEW_TYPE_SECTION) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_grid_section, parent, false);
            return SectionViewHolder.newInstance(view, mClickListener);
        } else if (viewType == VIEW_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_grid_content, parent, false);
            return ContentViewHolder.newInstance(view, mClickListener);
        } else {
            Timber.e("Error, Unknown ViewType");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeaderItemViewData((HeaderViewHolder) holder);
        } else if (holder instanceof SectionViewHolder) {
            bindSectionItemViewData((SectionViewHolder) holder);
        } else if (holder instanceof ContentViewHolder) {
            position = position - 2;
            bindContextItemViewData((ContentViewHolder) holder, getItemData(position));
        }
    }

    /**
     * Set the Channel Intent link content.
     *
     * @param holder  {@link Channel} {@link BaseViewHolder}
     * @param channel {@link Channel} data
     */
    @SuppressLint("NewApi")
    private void bindContextItemViewData(ContentViewHolder holder, RealmChannel channel) {
        Context context = holder.view.getContext();
        RealmChannelType realmChannelType = channel.getChannelType();
        if (realmChannelType.equals(getRealmChannelType(Custom))) {
            holder.channelImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, channel.getPackageName())));
        } else {
            holder.channelImage.setImageDrawable(
                getSVGIconDrawable(context, channel.getChannelType().getResId()));
        }
        holder.channelName.setText(channel.getLabel().toLowerCase());
    }

    /**
     * Set this {@link BaseViewHolder} underlying {@link User} data for the header.
     *
     * @param holder {@link Channel} {@link BaseViewHolder}
     */
    private void bindHeaderItemViewData(HeaderViewHolder holder) {
        Context context = holder.view.getContext();
        Picasso.with(context).load(mUser.imageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(getBitmapTargetView(holder));
        holder.userName.setText(joinWithSpace(new String[]{ mUser.firstName(), mUser.lastName() }));
    }

    /**
     * Set this {@link BaseViewHolder} underlying section data.
     *
     * @param holder {@link Channel} {@link BaseViewHolder}
     */
    private void bindSectionItemViewData(SectionViewHolder holder) {
        Context context = holder.view.getContext();
        holder.sectionName.setText(General.toString());
        int resourceId = General.getResId();
        holder.sectionImage.setImageDrawable(getSectionResourceDrawable(context, resourceId));
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
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener(viewHolder));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    viewHolder.userImage.setImageBitmap(bitmap);
                }
            };
        }
        return mTarget;
    }


    @Override
    public int getItemCount() {
        return mChannels.size() + 2;
    }

    /**
     * Get the desired {@link Channel} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public RealmChannel getItemData(int position) {
        return mChannels.get(position);
    }

    /**
     * Is this item at this position a section or a header?
     *
     * @param position of the item
     * @return yes or no
     */
    public boolean isHeaderOrSection(int position) {
        return position == 0 || position == 1;
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static class HeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_grid_header_container)
        protected LinearLayout userBackground;
        @InjectView(R.id.adapter_channel_grid_header_image)
        protected ImageView userImage;
        @InjectView(R.id.adapter_channel_grid_header_name)
        protected TextView userName;

        /**
         * Constructor for the ItemViewHolder.
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
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
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
    public static final class SectionViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_grid_section_image)
        protected ImageView sectionImage;
        @InjectView(R.id.adapter_channel_grid_section_name)
        protected TextView sectionName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              the inflated view
         */
        private SectionViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static SectionViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new SectionViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    public static final class ContentViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_grid_content_image)
        protected ImageView channelImage;
        @InjectView(R.id.adapter_channel_grid_content_name)
        protected TextView channelName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              the inflated view
         */
        private ContentViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param itemClickListener click listener for this view
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static ContentViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new ContentViewHolder(view, itemClickListener);
        }
    }
}
