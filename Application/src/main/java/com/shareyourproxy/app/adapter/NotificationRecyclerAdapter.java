package com.shareyourproxy.app.adapter;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.NotificationCardDismissEvent;
import com.shareyourproxy.widget.DismissibleNotificationCard;
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard;

import butterknife.Bind;
import rx.functions.Action1;

import static android.view.View.GONE;

/**
 * Adapter that can handle displaying a dismissable notification card as a header, footer or both.
 */
public abstract class NotificationRecyclerAdapter<T> extends SortedRecyclerAdapter<T> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_LIST_ITEM = 1;
    public static final int TYPE_FOOTER = 2;
    private boolean _isHeaderVisible = false;
    private boolean _isFooterVisible = false;
    private SharedPreferences _prefs;
    private NotificationCard _headerCard;
    private NotificationCard _footerCard;

    public NotificationRecyclerAdapter(
        Class<T> clazz, final BaseRecyclerView recyclerView, boolean showHeader, boolean showFooter,
        SharedPreferences sharedPreferences) {
        super(clazz, recyclerView);
        _isHeaderVisible = showHeader;
        _isFooterVisible = showFooter;
        _prefs = sharedPreferences;
        RxBusDriver.getInstance().toObservable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof NotificationCardDismissEvent) {
                    removeNotificationCard(_prefs, (NotificationCardDismissEvent) event);
                }
            }
        });
    }

    public void removeNotificationCard(
        SharedPreferences prefs, NotificationCardDismissEvent
        event) {
        if (event.adapter.getClass().equals(this.getClass())) {
            if (event.holder instanceof HeaderViewHolder) {
                _isHeaderVisible = false;
                notifyItemRemoved(0);
            } else if (event.holder instanceof FooterViewHolder) {
                _isFooterVisible = false;
                notifyItemRemoved(getItemCount() - 1);
            }
            prefs.edit().putBoolean(event.cardType.getKey(), true).apply();
        }
    }

    public final void bindHeaderViewData(
        HeaderViewHolder holder, NotificationCard cardType, boolean showDismiss,
        boolean showAction) {
        _headerCard = cardType;
        if (_isHeaderVisible) {
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss,
                showAction);
        } else {
            holder.view.setVisibility(GONE);
        }
    }

    public final void bindFooterViewData(
        FooterViewHolder holder, NotificationCard cardType, boolean showDismiss,
        boolean showAction) {
        if (_isFooterVisible) {
            _footerCard = cardType;
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss,
                showAction);
        } else {
            holder.view.setVisibility(GONE);
        }
    }

    @Override
    final public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_dismissible_notification, parent, false);
            return HeaderViewHolder.newInstance(view, null);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_dismissible_notification, parent, false);
            return FooterViewHolder.newInstance(view, null);
        } else {
            return onCreateItemViewHolder(parent, viewType);
        }
    }

    @Override
    final public int getItemCount() {
        return getTotalItemCount();
    }

    private int getTotalItemCount() {
        int listSize = getStaticListSize();

        if (listSize == 0) {
            return 0;
        }
        if (_isHeaderVisible) {
            ++listSize;
        }
        if (_isFooterVisible) {
            ++listSize;
        }
        return listSize;
    }

    @Override
    final public int getItemViewType(int position) {
        return checkItemViewType(position);
    }

    @Override
    final public T getItemData(int position) {
        return super.getItemData(getDataPositionOffset(position));
    }

    @Override
    final public void removeItem(int position) {
        super.removeItem(getDataPositionOffset(position));
    }

    @Override
    protected void onInserted(int position, int count) {
        super.onInserted(getViewPositionOffset(position), count);
    }

    @Override
    protected void onRemoved(int position, int count) {
        super.onRemoved(getViewPositionOffset(position), count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        super.onMoved(getViewPositionOffset(fromPosition),
            getViewPositionOffset(toPosition));
    }

    @Override
    public void onChanged(int position, int count) {
        super.onChanged(getViewPositionOffset(position), count);
    }

    @Override
    protected void beforeDataSetChanged(int position, int count) {
        if (_headerCard != null) {
            _isHeaderVisible = !_prefs.getBoolean(_headerCard.getKey(), false);
        }
        if (_footerCard != null) {
            _isFooterVisible = !_prefs.getBoolean(_footerCard.getKey(), false);
        }
    }

    private int getDataPositionOffset(int position) {
        if (_isHeaderVisible || _isFooterVisible) {
            int offset = position - 1;
            int end = getTotalItemCount() - 1;
            return offset > 0 ? offset == getTotalItemCount() ? end : offset : 0;
        } else {
            return position;
        }
    }

    private int getViewPositionOffset(int position) {
        if (_isHeaderVisible) {
            return position + 1;
        } else {
            return position;
        }
    }

    private int checkItemViewType(int position) {
        if (position == 0 && isHeaderVisible()) {
            return TYPE_HEADER;
        } else if (position == (getTotalItemCount() - 1) && isFooterVisible()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_LIST_ITEM;
        }
    }

    public final boolean isHeaderVisible() {
        return _isHeaderVisible;
    }

    public final boolean isFooterVisible() {
        return _isFooterVisible;
    }

    protected abstract BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    /**
     * ViewHolder for the lists header.
     */
    public static class HeaderViewHolder extends NotificationViewHolder {
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
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static HeaderViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new HeaderViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for the lists footer.
     */
    public static class FooterViewHolder extends NotificationViewHolder {
        /**
         * Constructor for the FooterViewHolder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private FooterViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link RecyclerView.Adapter#onCreateViewHolder}
         * @param itemClickListener click listener for this view
         * @return a ViewHolder instance
         */
        public static FooterViewHolder newInstance(View view, ItemClickListener itemClickListener) {
            return new FooterViewHolder(view, itemClickListener);
        }
    }

    /**
     * ViewHolder for switching on type.
     */
    public static class NotificationViewHolder extends BaseViewHolder {
        @Bind(R.id.adapter_dismissible_notification_card)
        DismissibleNotificationCard notificationCard;

        /**
         * Constructor for the ViewHolder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for this view
         */
        private NotificationViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }
    }

}
