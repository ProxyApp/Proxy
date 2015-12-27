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
class ErrorDialog : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments.getString(ARG_TITLE)
        val message = arguments.getString(ARG_MESSAGE)

        return AlertDialog.Builder(ContextThemeWrapper(activity,
                R.style.Widget_Proxy_App_Dialog))
                .setTitle(title)
                .setMessage(message)
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

    companion object {
        private val TAG = ErrorDialog::class.java.simpleName
        private val ARG_TITLE = "title"
        private val ARG_MESSAGE = "message"

        /**
         * Create a new dialog with a custom title and message body.
         * @param title   header of dialog
         * @param message body of dialog* *
         * @return this dialog
         */
        fun newInstance(title: String, message: String): ErrorDialog {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)

            val dialog = ErrorDialog()
            dialog.arguments = args
            return dialog
        }
    }

}
