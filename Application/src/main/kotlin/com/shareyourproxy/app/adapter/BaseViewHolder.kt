package com.shareyourproxy.app.adapter

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * ViewHolder for the entered view's data.
 */
internal abstract class BaseViewHolder : RecyclerView.ViewHolder, View.OnClickListener, View.OnLongClickListener {
    var view: View
    private var itemClickListener: ItemClickListener? = null
    private var itemLongClickListener: ItemLongClickListener? = null

    /**
     * Constructor for the holder.
     * @param view          the inflated view
     * @param clickListener ItemClickListener
     */
    constructor(view: View, clickListener: BaseViewHolder.ItemClickListener?) : super(view) {
        this.view = view
        this.view.setOnClickListener(this)
        itemClickListener = clickListener
    }

    /**
     * Constructor for the holder.
     * @param view          the inflated view
     * @param clickListener ItemLongClickListener
     */
    constructor(view: View, clickListener: BaseViewHolder.ItemLongClickListener?) : super(view) {
        this.view = view
        this.view.setOnClickListener(this)
        this.view.setOnLongClickListener(this)
        itemLongClickListener = clickListener
    }

    override fun onClick(v: View) {
        itemClickListener?.onItemClick(v, itemPosition)
        itemLongClickListener?.onItemClick(v, itemPosition)
    }

    override fun onLongClick(v: View): Boolean {
        itemLongClickListener?.onItemLongClick(v, itemPosition)
        return true
    }

    val itemPosition: Int get() {
        var position = layoutPosition
        if (position < 0) {
            position = 0
        }
        return position
    }

    /**
     * Click listener for all adapter items
     */
    interface ItemClickListener {
        /**
         * ItemClick event.
         * @param view     clicked
         * @param position in list
         */
        fun onItemClick(view: View, position: Int)

    }


    interface ItemLongClickListener : ItemClickListener {
        /**
         * ItemLongClick event.
         * @param view     clicked
         * @param position in list
         */
        fun onItemLongClick(view: View, position: Int)
    }
}
