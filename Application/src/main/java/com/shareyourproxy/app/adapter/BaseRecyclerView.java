package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * {@link BaseRecyclerView} that handles empty views.
 */
@SuppressWarnings("unused")
public class BaseRecyclerView extends RecyclerView {

    private View _emptyView;
    private SwipeRefreshLayout _swipeRefreshLayout;

    /**
     * Observer to monitor if we have an empty dataset.
     */
    private AdapterDataObserver _dataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateEmptyView();
        }
    };

    /**
     * Constructor.
     *
     * @param context activity context
     */
    public BaseRecyclerView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context activity context
     * @param attrs   app attributes
     */
    public BaseRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor.
     *
     * @param context  activity context
     * @param attrs    app attributes
     * @param defStyle defined style
     */
    public BaseRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Designate a view as the empty view. When the backing adapter has no data this view will be
     * made visible and the recycler view hidden.
     *
     * @param emptyView the view to display when this array has no data
     */
    public void setEmptyView(@NonNull View emptyView) {
        _emptyView = emptyView;
        _emptyView.setVisibility(View.GONE);
    }

    public void setSwipeRefreshLayout(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        _swipeRefreshLayout = swipeRefreshLayout;
        _swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(_dataObserver);
        }
        if (adapter != null) {
            adapter.registerAdapterDataObserver(_dataObserver);
        }
        super.setAdapter(adapter);
        updateEmptyView();
    }


    /**
     * Show or hide the empty view.
     */
    private void updateEmptyView() {
        if (_emptyView != null && getAdapter() != null) {
            boolean showEmptyView = getAdapter().getItemCount() == 0;
            _emptyView.setVisibility(showEmptyView ? VISIBLE : GONE);
            setVisibility(showEmptyView ? GONE : VISIBLE);
            if(_swipeRefreshLayout != null){
                _swipeRefreshLayout.setVisibility(showEmptyView ? GONE : VISIBLE);
            }
        }
    }
}
