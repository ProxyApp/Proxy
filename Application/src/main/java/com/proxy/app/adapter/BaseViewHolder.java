package com.proxy.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * ViewHolder for the entered view's data.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {
    protected View view;

    /**
     * Constructor for the holder.
     *
     * @param view the inflated view
     */
    protected BaseViewHolder(View view) {
        super(view);
        ButterKnife.inject(this, view);
        this.view = view;
    }
}
