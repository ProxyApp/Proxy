package com.shareyourproxy.app.dialog

import android.content.SharedPreferences
import android.support.v4.app.DialogFragment
import android.widget.Button

import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.app.BaseActivity

/**
 * Base Dialog Abstraction.
 */
open class BaseDialogFragment : DialogFragment() {
    /**
     * Sets the color of button input.

     * @param button Button
     * *
     * @param color  integer value of color.
     */
    fun setButtonTint(button: Button, color: Int) {
        button.setTextColor(color)
    }

    /**
     * Get the currently logged in user.

     * @return logged in user
     */
    val loggedInUser: User
        get() = (activity as BaseActivity).loggedInUser

    /**
     * Get this applications Observable event bus.

     * @return
     */
    val rxBus: RxBusDriver
        get() = (activity as BaseActivity).rxBus


    /**
     * Get this applications Observable event bus.

     * @return
     */
    val sharedPreferences: SharedPreferences
        get() = (activity as BaseActivity).sharedPreferences
}
