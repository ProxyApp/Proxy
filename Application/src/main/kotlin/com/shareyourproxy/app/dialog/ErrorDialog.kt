package com.shareyourproxy.app.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.ContextThemeWrapper
import com.shareyourproxy.R
import com.shareyourproxy.R.string.ok


/**
 * Dialog to handle onError messaging during login.
 */
class ErrorDialog(private val title: String, private val message: String) : BaseDialogFragment() {
    private val TAG = ErrorDialog::class.java.simpleName
    private val ARG_TITLE = "title"
    private val ARG_MESSAGE = "message"
    private val parcelTitle = arguments.getString(ARG_TITLE)
    private val parcelMessage = arguments.getString(ARG_MESSAGE)
    init {
        arguments.putString(ARG_TITLE, title)
        arguments.putString(ARG_MESSAGE, message)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(ContextThemeWrapper(activity,
                R.style.Widget_Proxy_App_Dialog))
                .setTitle(parcelTitle)
                .setMessage(parcelMessage)
                .setPositiveButton(ok, null)
                .create()
    }

    /**
     * Use the private string TAG from this class as an identifier.
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): ErrorDialog {
        show(fragmentManager, TAG)
        return this
    }
}
