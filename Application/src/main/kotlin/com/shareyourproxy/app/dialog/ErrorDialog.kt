package com.shareyourproxy.app.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.ContextThemeWrapper
import com.shareyourproxy.R
import com.shareyourproxy.R.string.ok
import com.shareyourproxy.util.ButterKnife.LazyVal


/**
 * Dialog to handle onError messaging during login.
 */
internal final class ErrorDialog private constructor(title: String, message: String) : BaseDialogFragment() {
    companion object {
        private val ARG_TITLE = "title"
        private val ARG_MESSAGE = "message"
        fun show(manager: FragmentManager, title: String, message: String): ErrorDialog {
            return setArgs(manager, title, message)
        }

        private fun setArgs(manager: FragmentManager, title: String, message: String): ErrorDialog {
            val dialog= ErrorDialog(title, message)
            val args: Bundle = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            dialog.arguments = args
            return dialog.show(manager)
        }
    }

    private val parcelTitle: String by LazyVal { arguments.getString(ARG_TITLE) }
    private val parcelMessage: String by LazyVal { arguments.getString(ARG_MESSAGE) }

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
        show(fragmentManager, ErrorDialog::class.java.simpleName)
        return this
    }
}
