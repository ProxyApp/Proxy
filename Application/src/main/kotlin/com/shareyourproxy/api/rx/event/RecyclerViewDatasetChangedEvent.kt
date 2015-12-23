package com.shareyourproxy.api.rx.event


import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter

/**
 * Created by Evan on 11/2/15.
 */
class RecyclerViewDatasetChangedEvent(val adapter: BaseRecyclerViewAdapter, val viewState: BaseRecyclerView.ViewState)
