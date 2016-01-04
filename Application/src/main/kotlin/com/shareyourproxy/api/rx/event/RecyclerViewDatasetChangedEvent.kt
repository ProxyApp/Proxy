package com.shareyourproxy.api.rx.event


import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter
import com.shareyourproxy.util.Enumerations

/**
 * Recycler view has changed data set.
 */
internal class RecyclerViewDatasetChangedEvent(val adapter: BaseRecyclerViewAdapter, val viewState: Enumerations.ViewState)
