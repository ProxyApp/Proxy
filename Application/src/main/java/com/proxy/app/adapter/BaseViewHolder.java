package com.proxy.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * ViewHolder for the entered view's data.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected View view;
    private ItemClickListener mItemClickListener;

    /**
     * Constructor for the holder.
     *
     * @param view          the inflated view
     * @param clickListener ItemClickListener
     */
    protected BaseViewHolder(View view, ItemClickListener clickListener) {
        super(view);
        ButterKnife.inject(this, view);

        this.view = view;
        this.view.setOnClickListener(this);
        this.mItemClickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        mItemClickListener.onItemClick(v, getAdapterPosition());
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
    }
}
