package com.proxy.app.adapter;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.util.SortedList;
import android.text.Layout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.realm.RealmChannelType;
import com.proxy.api.domain.realm.RealmGroupEditChannel;
import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannelType;
import static com.proxy.api.domain.model.ChannelType.Custom;
import static com.proxy.util.ViewUtils.getActivityIcon;

import butterknife.InjectView;

public class EditGroupAdapter extends BaseRecyclerViewAdapter{
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_LIST_ITEM = 1;
    private BaseViewHolder.ItemClickListener mClickListener;
    SortedList.Callback<RealmGroupEditChannel> mSortedListCallback;
    SortedList<RealmGroupEditChannel> mChannels = new SortedList<>(RealmGroupEditChannel.class,
        getSortedCallback());


    public EditGroupAdapter(BaseViewHolder.ItemClickListener listener) {
        mClickListener = listener;
    }

    public static EditGroupAdapter newInstance(BaseViewHolder.ItemClickListener listener) {
        return new EditGroupAdapter(listener);
    }

    //todo ccoffey lift this out to a common call
    public SortedList.Callback<RealmGroupEditChannel> getSortedCallback() {
        if (mSortedListCallback == null) {
            mSortedListCallback = new SortedList.Callback<RealmGroupEditChannel>() {


                @Override
                public int compare(RealmGroupEditChannel o1, RealmGroupEditChannel o2) {
                    return o1.getChannel().getChannelId().compareTo(o2.getChannel().getChannelId());
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
                public boolean areContentsTheSame(RealmGroupEditChannel oldItem, RealmGroupEditChannel newItem) {
                    // we dont compare resId because its probably going to be removed
                    return (oldItem.getChannel().getChannelId().equals(newItem.getChannel().getChannelId())
                            && oldItem.getChannel().getLabel().equals(newItem.getChannel().getLabel())
                            && oldItem.getChannel().getPackageName().equals(newItem.getChannel().getPackageName())
                            && oldItem.getChannel().getSection() == newItem.getChannel().getSection()
                            && oldItem.getChannel().getChannelType().equals(newItem.getChannel().getChannelType()));
                }

                @Override
                public boolean areItemsTheSame(RealmGroupEditChannel item1, RealmGroupEditChannel item2) {
                    //Sections will have the same ID but different categories
                    return (item1.getChannel().getChannelId().equals(item2.getChannel().getChannelId())
                            && item1.getChannel().getSection() == item2.getChannel().getSection());
                }
            };
        }
        return mSortedListCallback;
    }

    public boolean isSectionHeader(int position) {
        return position == 0;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(isSectionHeader(viewType)) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_list_section, parent, false);
            return SectionHeaderViewHolder.newInstance(view, mClickListener);
        }
        else {
            View view = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.adapter_edit_group_item, parent, false);
            return ItemViewHolder.newInstance(view, mClickListener);
        }

    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int i) {
        //todo implement this
    }

    private RealmGroupEditChannel getItemData(int position) {
        return mChannels.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position == TYPE_SECTION_HEADER ? TYPE_SECTION_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return mChannels.size() +1;
    }

    private void bindItemViewData(ItemViewHolder holder, RealmGroupEditChannel channel) {
        Context context = holder.view.getContext();
        RealmChannelType realmChannelType = channel.getChannel().getChannelType();
        if(realmChannelType.equals(getRealmChannelType(Custom))) {
            holder.itemImage.setImageDrawable(getAndroidIconDrawable(
                    context, getActivityIcon(context, channel.getChannel().getPackageName())));
        }
        else {
            holder.itemImage.setImageDrawable(
                    getSVGIconDrawable(context, channel.getChannel().getChannelType().getResId()));
        }
        holder.itemLabel.setText(channel.getChannel().getLabel().toLowerCase());
        holder.itemSwitch.setChecked(channel.getInGroup());
    }


    public static final class SectionHeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_add_channel_list_section_image)
        protected ImageView sectionImage;
        @InjectView(R.id.adapter_add_channel_list_section_label)
        protected TextView sectionLabel;

        private SectionHeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static SectionHeaderViewHolder newInstance(
                View view, ItemClickListener
                itemClickListener) {
            return new SectionHeaderViewHolder(view, itemClickListener);
        }
    }


    public static final class ItemViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_edit_group_list_item_image)
        protected ImageView itemImage;
        @InjectView(R.id.adapter_edit_group_list_item_label)
        protected TextView itemLabel;
        @InjectView(R.id.adapter_edit_group_list_item_switch)
        protected Switch itemSwitch;

        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }

    }
}
