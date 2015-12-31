package com.shareyourproxy.app.adapter

import android.support.v7.util.SortedList
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.EMPTY
import com.shareyourproxy.app.adapter.BaseRecyclerView.ViewState.MAIN
import java.util.*

/**
 * Adapter that is backed by a sorted List.
 */
abstract class SortedRecyclerAdapter<T>(clazz: Class<T>, private val recyclerView: BaseRecyclerView) : BaseRecyclerViewAdapter() {
    protected val sortedList: SortedList<T> = SortedList(clazz, getSortedCallback(this), 0)
    private var needsRefresh = true

    private fun getSortedCallback(callback: SortedRecyclerAdapter<T>): SortedList.Callback<T> {
        return object : SortedList.Callback<T>() {
            override fun compare(item1: T, item2: T): Int {
                return callback.compare(item1, item2)
            }

            override fun onInserted(position: Int, count: Int) {
                callback.onInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                callback.onRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                callback.onMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                callback.onChanged(position, count)
            }

            override fun areContentsTheSame(item1: T, item2: T): Boolean {
                return callback.areContentsTheSame(item1, item2)
            }

            override fun areItemsTheSame(item1: T, item2: T): Boolean {
                return callback.areItemsTheSame(item1, item2)
            }
        }
    }

    protected open fun beforeDataSetChanged(position: Int, count: Int) {
    }

    protected abstract fun compare(item1: T, item2: T): Int

    protected open fun onInserted(position: Int, count: Int) {
        if (needsRefresh) {
            needsRefresh = false
            val event = RecyclerViewDatasetChangedEvent(this, MAIN)
            post(event)
            recyclerView.updateViewState(event)
            beforeDataSetChanged(position, count)
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(position, count)
        }
    }

    protected open fun onRemoved(position: Int, count: Int) {
        if (itemCount == 0) {
            needsRefresh = true
            val event = RecyclerViewDatasetChangedEvent(this, EMPTY)
            post(event)
            recyclerView.updateViewState(event)
        } else {
            notifyItemRangeRemoved(position, count)
        }
    }

    protected open fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    protected open fun onChanged(position: Int, count: Int) {
        notifyItemRangeChanged(position, count)
    }

    protected abstract fun areContentsTheSame(item1: T, item2: T): Boolean

    protected abstract fun areItemsTheSame(item1: T, item2: T): Boolean


    protected val staticListSize: Int get() = sortedList.size()

    val data: ArrayList<T> get() {
        val datas = ArrayList<T>(sortedList.size())
        for (i in 0..sortedList.size() - 1) {
            datas.add(i, sortedList.get(i))
        }
        return datas
    }

    protected val lastItem: T get() = sortedList.get(sortedList.size() - 1)

    fun refreshData(datas: Collection<T>?) {
        sortedList.beginBatchedUpdates()
        if (datas != null) {
            sortedList.addAll(datas)
        }
        sortedList.endBatchedUpdates()
    }

    fun setNeedsRefresh(needsRefresh: Boolean) {
        this.needsRefresh = needsRefresh
    }

    fun updateItem(oldItem: T, newItem: T) {
        sortedList.updateItemAt(sortedList.indexOf(oldItem), newItem)
    }

    open fun getItemData(position: Int): T {
        return sortedList.get(position)
    }

    open fun removeItem(position: Int) {
        sortedList.removeItemAt(position)
    }

    fun addItem(item: T) {
        sortedList.add(item)
    }

    override fun getItemCount(): Int {
        return sortedList.size()
    }

}
