package com.proxy.app.adapter;

import android.content.Context;
import android.content.res.Resources;
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
import com.proxy.api.model.Channel;
import com.proxy.api.model.User;
import com.proxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.InjectView;
import timber.log.Timber;

/**
 * Adapter for a users profile and their {@link Channel} package permissions.
 */
public class ChannelRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SECTION = 1;
    private static final int VIEW_TYPE_CONTENT = 2;
    ArrayList<Channel> mChannels = new ArrayList<>();
    private Target mTarget;
    private User profileUser;

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
                Resources res = viewHolder.view.getContext().getResources();
                viewHolder.userBackground.setBackgroundColor(
                    palette.getVibrantColor(res.getColor(R.color.common_deep_purple)));
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        //Header is always the first item. Sections divide newly added channels.
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (isSection(position)) {
            return VIEW_TYPE_SECTION;
        } else {
            return VIEW_TYPE_CONTENT;
        }
    }

    /**
     * If the category of the position before the current item is different, we need to enter a
     * section divider.
     *
     * @param position of current item
     * @return is this a section
     */
    private boolean isSection(int position) {
        return !getItemData(position).getCategory().equals(getItemData(position - 1).getCategory());
    }

    /**
     * Calculate the number of section breaks in categories of channels.
     *
     * @return section break count
     */
    private int calculateSections() {
        String lastCategory = "";
        int sectionCount = 0;
        for (Channel channel : mChannels) {
            if (!channel.getCategory().equals(lastCategory)) {
                sectionCount++;
                lastCategory = channel.getCategory();
            }
        }
        return sectionCount;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_header, parent, false);
            return HeaderViewHolder.newInstance(view);
        } else if (viewType == VIEW_TYPE_SECTION) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_section, parent, false);
            return SectionViewHolder.newInstance(view);
        } else if (viewType == VIEW_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_content, parent, false);
            return ContentViewHolder.newInstance(view);
        } else {
            Timber.e("Error, Unknown ViewType");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            setHeaderItemViewData((HeaderViewHolder) holder);
        }
//        else if (holder instanceof SectionViewHolder) {
//        } else if (holder instanceof ContentViewHolder) {
//        } else {
//            Timber.e("Unknown ViewHolder Instance check");
//        }
    }

    /**
     * Set this ViewHolders underlying {@link Channel} data.
     *
     * @param holder {@link Channel} {@link BaseViewHolder}
     */
    private void setHeaderItemViewData(HeaderViewHolder holder) {
        Context context = holder.view.getContext();
        Picasso.with(context).load(profileUser.getImageURL())
            .placeholder(R.drawable.proxy_icon)
            .transform(new CircleTransform())
            .into(getBitmapTargetView(holder));
        holder.userName.setText(profileUser.getFirstName() + " " + profileUser.getLastName());
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
    public int getItemCount() {
        return mChannels.size() + calculateSections() + 1;
    }

    /**
     * Get the desired {@link Channel} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link User}
     */
    public Channel getItemData(int position) {
        return mChannels.get(position);
    }

    /**
     * ViewHolder for the entered settings data.
     */
    protected static class HeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_header_container)
        protected LinearLayout userBackground;
        @InjectView(R.id.adapter_channel_header_image)
        protected ImageView userImage;
        @InjectView(R.id.adapter_channel_header_name)
        protected TextView userName;

        /**
         * Constructor for the ItemViewHolder.
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
    protected static class SectionViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_section_image)
        protected ImageView sectionImage;
        @InjectView(R.id.adapter_channel_section_name)
        protected TextView sectionName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param view the inflated view
         */
        private SectionViewHolder(View view) {
            super(view);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static SectionViewHolder newInstance(View view) {
            return new SectionViewHolder(view);
        }
    }

    /**
     * ViewHolder for the entered settings data.
     */
    protected static class ContentViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_channel_content_image)
        protected ImageView channelImage;
        @InjectView(R.id.adapter_channel_content_name)
        protected TextView channeltName;

        /**
         * Constructor for the ItemViewHolder.
         *
         * @param view the inflated view
         */
        private ContentViewHolder(View view) {
            super(view);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @return a ViewHolder instance
         */
        public static ContentViewHolder newInstance(View view) {
            return new ContentViewHolder(view);
        }
    }
}
