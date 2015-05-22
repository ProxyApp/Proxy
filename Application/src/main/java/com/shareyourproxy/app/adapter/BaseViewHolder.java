package com.shareyourproxy.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * ViewHolder for the entered view's data.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener, View.OnLongClickListener {
    protected View _view;
    private ItemClickListener _itemClickListener;

    /**
     * Constructor for the holder.
     *
     * @param view          the inflated view
     * @param clickListener ItemClickListener
     */
    protected BaseViewHolder(View view, ItemClickListener clickListener) {
        super(view);
        ButterKnife.inject(this, view);
        _view = view;
        _view.setOnClickListener(this);
        _view.setOnLongClickListener(this);
        _itemClickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        _itemClickListener.onItemClick(v, getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
        _itemClickListener.onItemLongClick(v, getAdapterPosition());
        return true;
    }

    /**
     * Click listener for all adapter items
     */
    public interface ItemClickListener {
        /**
         * ItemClick event.
         *
         * @param view     clicked
         * @param position in list
         */
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}
