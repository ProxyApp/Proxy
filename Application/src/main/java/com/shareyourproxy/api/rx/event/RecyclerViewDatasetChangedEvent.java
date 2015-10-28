package com.shareyourproxy.api.rx.event;


import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter;

/**
 * Created by Evan on 11/2/15.
 */
public class RecyclerViewDatasetChangedEvent {
    public final BaseRecyclerView.ViewState viewState;
    public final BaseRecyclerViewAdapter adapter;

    public RecyclerViewDatasetChangedEvent(BaseRecyclerViewAdapter adapter, BaseRecyclerView
        .ViewState viewState) {
        this.viewState = viewState;
        this.adapter = adapter;
    }
}
