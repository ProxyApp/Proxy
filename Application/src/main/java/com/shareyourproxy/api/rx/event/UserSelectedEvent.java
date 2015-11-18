package com.shareyourproxy.api.rx.event;

import android.view.View;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 4/26/15.
 */
public class UserSelectedEvent {

    public final User user;
    public final View imageView;
    public final View textView;

    /**
     * Constructor.
     *
     * @param user      that was selected
     * @param imageView user image
     * @param textView  user name label
     */
    public UserSelectedEvent(View imageView, View textView, User user) {
        this.user = user;
        this.imageView = imageView;
        this.textView = textView;
    }
}
