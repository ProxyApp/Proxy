package com.proxy.app.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.proxy.R;
import com.proxy.api.domain.realm.RealmChannelSection;
import com.proxy.api.domain.realm.RealmChannelType;
import com.proxy.api.domain.realm.RealmGroupEditChannel;
import com.proxy.util.ObjectUtils;

import butterknife.InjectView;
import io.realm.RealmObject;
import timber.log.Timber;

import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannelType;
import static com.proxy.api.domain.model.ChannelType.Custom;
import static com.proxy.util.ViewUtils.getActivityIcon;

public class EditGroupRecyclerAdapter extends BaseRecyclerViewAdapter {
    public static final int TYPE_LIST_ITEM = 1;
    private static final int TYPE_SECTION_HEADER = 0;
    private BaseViewHolder.ItemClickListener mClickListener;
    private SortedList.Callback<RealmObject> mSortedListCallback;
    private SortedList<RealmObject> mRealmData = new SortedList<>(RealmObject.class,
        getSortedCallback());


    public EditGroupRecyclerAdapter(BaseViewHolder.ItemClickListener listener) {
        mClickListener = listener;
    }

    public static EditGroupRecyclerAdapter newInstance(BaseViewHolder.ItemClickListener listener) {
        return new EditGroupRecyclerAdapter(listener);
    }

    //todo ccoffey lift this out to a common call
    public SortedList.Callback<RealmObject> getSortedCallback() {
        if (mSortedListCallback == null) {
            mSortedListCallback = new SortedList.Callback<RealmObject>() {

                @Override
                public int compare(RealmObject item1, RealmObject item2) {
                    Boolean isItem1Section = item1 instanceof RealmChannelSection;
                    Boolean isItem2Section = item2 instanceof RealmChannelSection;
                    Boolean isItem1EditChannel = item1 instanceof RealmGroupEditChannel;
                    Boolean isItem2EditChannel = item2 instanceof RealmGroupEditChannel;

                    if (isItem1Section && isItem2Section) {
                        return ObjectUtils.compare(((RealmChannelSection) item1).getWeight(), (
                            (RealmChannelSection) item2).getWeight());
                    } else if (isItem1Section && isItem2EditChannel) {
                        return -1;
                    } else if (isItem1EditChannel && isItem2Section) {
                        return 1;
                    } else if (isItem1EditChannel && isItem2EditChannel) {
                        return ((RealmGroupEditChannel) item1).getChannel().getChannelId()
                            .compareTo(((RealmGroupEditChannel) item2).getChannel().getChannelId());
                    } else {
                        Timber.e("Unexpected list compare case returning lhs");
                        return -1;
                    }
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
                public boolean areContentsTheSame(
                    RealmObject item1, RealmObject item2) {
                    if (item1 instanceof RealmChannelSection
                        && item2 instanceof RealmChannelSection) {
                        return ((RealmChannelSection) item1).getWeight()
                            == ((RealmChannelSection) item2).getWeight()
                            && ((RealmChannelSection) item1).getLabel()
                            .equals(((RealmChannelSection) item2).getLabel());
                    } else if (item1 instanceof RealmChannelSection
                        && item2 instanceof RealmGroupEditChannel) {
                        return false;
                    } else if (item1 instanceof RealmGroupEditChannel
                        && item1 instanceof RealmChannelSection) {
                        return false;
                    } else if (item1 instanceof RealmGroupEditChannel
                        && item2 instanceof RealmGroupEditChannel) {
                        return ((RealmGroupEditChannel) item1).getChannel().getChannelId()
                            .equals(((RealmGroupEditChannel) item2).getChannel().getChannelId());
                    } else {
                        Timber.e("Invalid list contents comparison");
                        return false;
                    }
                }

                @Override
                public boolean areItemsTheSame(RealmObject item1, RealmObject item2) {
                    //Sections will have the same ID but different categories
                    if (item1 instanceof RealmChannelSection
                        && item2 instanceof RealmChannelSection) {
                        return ((RealmChannelSection) item1).getWeight()
                            == ((RealmChannelSection) item2).getWeight()
                            && ((RealmChannelSection) item1).getLabel()
                            .equals(((RealmChannelSection) item2).getLabel());
                    } else if (item1 instanceof RealmChannelSection
                        && item2 instanceof RealmGroupEditChannel) {
                        return false;
                    } else if (item1 instanceof RealmGroupEditChannel
                        && item1 instanceof RealmChannelSection) {
                        return false;
                    } else if (item1 instanceof RealmGroupEditChannel
                        && item2 instanceof RealmGroupEditChannel) {
                        return ((RealmGroupEditChannel) item1).getChannel().getChannelId()
                            .equals(((RealmGroupEditChannel) item2).getChannel().getChannelId());
                    } else {
                        Timber.e("Invalid list contents comparison");
                        return false;
                    }
                }
            };
        }
        return mSortedListCallback;
    }

    public boolean isSectionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_SECTION_HEADER : TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        return mRealmData.size() + 1;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isSectionHeader(viewType)) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_list_section, parent, false);
            return SectionHeaderViewHolder.newInstance(view, mClickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_edit_group_item, parent, false);
            return ItemViewHolder.newInstance(view, mClickListener);
        }

    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            bindSectionViewData(
                (SectionHeaderViewHolder) holder, (RealmChannelSection) getItemData(position));
        } else if (holder instanceof ItemViewHolder) {
            bindItemViewData(
                (ItemViewHolder) holder, (RealmGroupEditChannel) getItemData(position));
        } else {
            Timber.e("Invalid ViewHolder");
        }
    }

    private RealmObject getItemData(int position) {
        return mRealmData.get(position);
    }

    private void bindItemViewData(ItemViewHolder holder, RealmGroupEditChannel channel) {
        Context context = holder.view.getContext();
        RealmChannelType realmChannelType = channel.getChannel().getChannelType();
        if (realmChannelType.equals(getRealmChannelType(Custom))) {
            holder.itemImage.setImageDrawable(getAndroidIconDrawable(
                context, getActivityIcon(context, channel.getChannel().getPackageName())));
        } else {
            holder.itemImage.setImageDrawable(
                getSVGIconDrawable(context, channel.getChannel().getChannelType().getResId()));
        }
        holder.itemLabel.setText(channel.getChannel().getLabel().toLowerCase());
        holder.itemSwitch.setChecked(channel.getInGroup());
    }

    private void bindSectionViewData(
        SectionHeaderViewHolder holder, RealmChannelSection sectionData) {
        holder.sectionLabel.setText(sectionData.getLabel());
    }


    public static final class SectionHeaderViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_add_channel_list_section_label)
        protected TextView sectionLabel;

        private SectionHeaderViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static SectionHeaderViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new SectionHeaderViewHolder(view, itemClickListener);
        }
    }


    public static final class ItemViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_edit_group_list_item_switch)
        public Switch itemSwitch;
        @InjectView(R.id.adapter_edit_group_list_item_image)
        protected ImageView itemImage;
        @InjectView(R.id.adapter_edit_group_list_item_label)
        protected TextView itemLabel;

        private ItemViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        public static ItemViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new ItemViewHolder(view, itemClickListener);
        }

    }
}
