package com.shareyourproxy.app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;

import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;

import java.util.ArrayList;
import java.util.Collection;

import static com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY;
import static com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.MAIN;

/**
 * Adapter that is backed by a sorted List.
 */
public abstract class SortedRecyclerAdapter<T> extends BaseRecyclerViewAdapter {
    private BaseRecyclerView _recyclerView;
    private SortedList<T> _data;
    private boolean _needsRefresh = true;
    private SortedList.Callback<T> _sortedListCallback;
    private RxBusDriver _rxBus = RxBusDriver.getInstance();

    public SortedRecyclerAdapter(Class<T> clazz, BaseRecyclerView recyclerView) {
        _recyclerView = recyclerView;
        _data = new SortedList<>(clazz, getSortedCallback(this), 0);
    }

    private SortedList.Callback<T> getSortedCallback(final SortedRecyclerAdapter<T> callback) {
        if (_sortedListCallback == null) {
            _sortedListCallback = new SortedList.Callback<T>() {
                @Override
                public int compare(T item1, T item2) {
                    return callback.compare(item1, item2);
                }

                @Override
                public void onInserted(int position, int count) {
                    callback.onInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    callback.onRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    callback.onMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    callback.onChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(T item1, T item2) {
                    return callback.areContentsTheSame(item1, item2);
                }

                @Override
                public boolean areItemsTheSame(T item1, T item2) {
                    return callback.areItemsTheSame(item1, item2);
                }
            };
        }
        return _sortedListCallback;
    }

    protected void beforeDataSetChanged(int position, int count) {
    }

    protected abstract int compare(T item1, T item2);

    protected void onInserted(int position, int count) {
        if (_needsRefresh) {
            _needsRefresh = false;
            RecyclerViewDatasetChangedEvent event = new
                RecyclerViewDatasetChangedEvent(this, MAIN);
            _rxBus.post(event);
            _recyclerView.updateViewState(event);
            beforeDataSetChanged(position, count);
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(position, count);
        }
    }

    protected void onRemoved(int position, int count) {
        if (getItemCount() == 0) {
            _needsRefresh = true;
            RecyclerViewDatasetChangedEvent event = new
                RecyclerViewDatasetChangedEvent(this, EMPTY);
            _rxBus.post(event);
            _recyclerView.updateViewState(event);
        } else {
            notifyItemRangeRemoved(position, count);
        }
    }

    protected void onMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    protected void onChanged(int position, int count) {
        notifyItemRangeChanged(position, count);
    }

    protected abstract boolean areContentsTheSame(T item1, T item2);

    protected abstract boolean areItemsTheSame(T item1, T item2);


    final protected int getStaticListSize() {
        return _data.size();
    }

    final public ArrayList<T> getData() {
        ArrayList<T> datas = new ArrayList<>(_data.size());
        for (int i = 0; i < _data.size(); i++) {
            datas.add(i, _data.get(i));
        }
        return datas;
    }

    final protected SortedList<T> getSortedList() {
        return _data;
    }

    final protected T getLastItem() {
        return _data.get(_data.size() - 1);
    }

    final public void refreshData(Collection<T> datas) {
        _data.beginBatchedUpdates();
        if (datas != null) {
            _data.addAll(datas);
        }
        _data.endBatchedUpdates();
    }

    public void setNeedsRefresh(boolean needsRefresh) {
        _needsRefresh = needsRefresh;
    }

    final public void updateItem(@NonNull T oldItem, @NonNull T newItem) {
        _data.updateItemAt(_data.indexOf(oldItem), newItem);
    }

    public T getItemData(int position) {
        return _data.get(position);
    }

    public void removeItem(int position) {
        _data.removeItemAt(position);
    }

    public void addItem(@NonNull T item) {
        _data.add(item);
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

}
