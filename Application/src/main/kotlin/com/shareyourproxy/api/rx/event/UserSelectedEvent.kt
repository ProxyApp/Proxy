package com.shareyourproxy.api.rx.event

import android.view.View

import com.shareyourproxy.api.domain.model.User

/**
 * Constructor.
 * @param user      that was selected
 * @param imageView user image
 * @param textView  user name label
 */
class UserSelectedEvent(val imageView: View, val textView: View, val user: User)
