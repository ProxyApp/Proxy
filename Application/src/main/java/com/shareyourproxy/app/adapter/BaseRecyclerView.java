package com.shareyourproxy.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;

import static android.support.v4.content.ContextCompat.getColor;

/**
 * {@link BaseRecyclerView} that handles empty views.
 */
@SuppressWarnings("unused")
public class BaseRecyclerView extends RecyclerView {

    private View _emptyView;
    private View _loadingView;
    private RxBusDriver _rxBus;
    private ViewState _viewState;
    private View _refreshView;

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
     * Designate a view as the empty view. When the backing adapter has no data this view will be made visible and the recycler view hidden.
     *
     * @param emptyView the view to display when this adapter has no data
     */
    public void setEmptyView(@NonNull View emptyView) {
        _emptyView = emptyView;
    }

    public void setRefreshView(@NonNull View refreshView) {
        _refreshView = refreshView;
    }

    /**
     * Designate a view as the loading view. When the backing adapter is loading data this view will be made visible and the recycler view hidden.
     *
     * @param loadingView the view to display when this adapter is loading data
     */
    public void setLoadingView(@NonNull View loadingView) {
        _loadingView = loadingView;
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        // make sure to set a loading or empty view before you call set adapter.
        // That way it will show automatically. Loading views take precedence over empty views.
        super.setAdapter(adapter);
        if (_loadingView != null) {
            toggleVisibility(_loadingView);
        } else if (_emptyView != null) {
            toggleVisibility(_emptyView);
        } else {
            toggleVisibility(this);
        }
    }

    /**
     * Show or hide the empty view.
     */
    public void updateViewState(RecyclerViewDatasetChangedEvent event) {
        _viewState = event.viewState;
        switch (_viewState) {
            case MAIN:
                toggleVisibility(this);
                break;
            case EMPTY:
                toggleVisibility(_emptyView);
                break;
            case LOADING:
                toggleVisibility(_loadingView);
                break;
        }
    }

    private void toggleVisibility(View view) {
        if (view != null) {
            clearViewTypeVisibility(view);
            view.setVisibility(getViewVis(view));
        }
    }

    private int getViewVis(View view) {
        if (view.equals(this) && _refreshView != null) {
            _refreshView.setVisibility(View.VISIBLE);
        }
        return View.VISIBLE;
    }

    /**
     * Set all ViewState's visibility to be gone.
     *
     * @param view view that will be toggled visible
     */
    private void clearViewTypeVisibility(View view) {
        if (view != this) {
            this.setVisibility(View.GONE);
            if (_refreshView != null) {
                _refreshView.setVisibility(View.GONE);
            }
        }
        if (_emptyView != view && _emptyView != null) {
            _emptyView.setVisibility(View.GONE);
        }
        if (_loadingView != view && _loadingView != null) {
            _loadingView.setVisibility(View.GONE);
        }
    }

    public ViewState getViewType() {
        return _viewState;
    }

    /**
     * The type of view state this recycler view is in.
     */
    public enum ViewState {
        MAIN, EMPTY, LOADING
    }


    public static class SubHeadItemDecoration extends RecyclerView.ItemDecoration {
        public static final int PADDING = 8;
        private final int _textWidth;
        private final int _textHeight;
        Paint _paint;
        private String _title;

        @SuppressLint("InflateParams")
        public SubHeadItemDecoration(Context context, String title) {
            TextView textView = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.common_textview_body1_disabled, null, false);
            _paint = textView.getPaint();
            _paint.setColor(getColor(context, R.color.common_text_disabled));
            Rect textBounds = new Rect();
            _paint.getTextBounds(title, 0, title.length(), textBounds);
            _textWidth = Math.abs(textBounds.width());
            _textHeight = Math.abs(textBounds.height());
            _title = title;
        }

        @Override
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, State state) {
            super.onDrawOver(canvas, recyclerView, state);
            final int size = recyclerView.getHeight() + recyclerView.getPaddingTop() +
                recyclerView.getPaddingBottom();

            canvas.drawText(_title, 60, 60, _paint);
        }

        @Override
        public void getItemOffsets(
            Rect outRect, View view, RecyclerView recyclerView, State state) {
            super.getItemOffsets(outRect, view, recyclerView, state);
            if (recyclerView.getChildLayoutPosition(view) == 0) {
                outRect.set(0, 0, 0, _textHeight);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.topMargin = _textHeight + PADDING;
            }
        }

    }
}
