package com.proxy.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * {@link BaseRecyclerView} that has a {@link RecyclerItemClickListener}.
 */
@SuppressWarnings("unused")
public class BaseRecyclerView extends RecyclerView {

    /**
     * ClickListener CallBack.
     */
    public interface OnItemClickListener {
        /**
         * Return the view and position of the clicked item
         *
         * @param view     view pressed
         * @param position position of the view in this {@link BaseRecyclerView}
         */
        void onItemClick(View view, int position);
    }

    private static RecyclerItemClickListener mRecyclerItemClickListener = null;

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
     * Get ItemClickListener.
     *
     * @param context       activity context
     * @param clickListener implemented click listener in adapter
     * @return ItemClickListener
     */
    public static RecyclerItemClickListener getItemClickListener(
        Context context, OnItemClickListener clickListener) {
        if (mRecyclerItemClickListener == null) {
            mRecyclerItemClickListener = new RecyclerItemClickListener(context, clickListener);
        }
        return mRecyclerItemClickListener;
    }

    /**
     * Create a static {@link OnItemTouchListener} for the {@link BaseRecyclerView}.
     */
    static final class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        GestureDetector mGestureDetector;
        private OnItemClickListener mListener;

        /**
         * Implement {@link OnItemTouchListener}.
         *
         * @param context  activity context
         * @param listener listener callback
         */
        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector
                .SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }
}
