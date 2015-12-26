package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.rx.command.GenerateShareLinkCommand
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.ShareLinkAdapter

import org.solovyev.android.views.llm.LinearLayoutManager

import java.util.HashMap

import butterknife.Bind
import butterknife.BindColor
import butterknife.ButterKnife

import com.shareyourproxy.util.ObjectUtils.getSimpleName

/**
 * Share links to group channels in your web profile.
 */
class ShareLinkDialog : BaseDialogFragment() {
    private val _negativeClicked = negOnClickListener
    @Bind(R.id.dialog_sharelink_message)
    internal var message: TextView
    @Bind(R.id.dialog_sharelink_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_divider)
    internal var colorGray: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    private var _adapter: ShareLinkAdapter? = null
    private val _positiveClicked = posOnClickListener

    private val posOnClickListener: DialogInterface.OnClickListener
        get() = DialogInterface.OnClickListener { rxBus.post(GenerateShareLinkCommand(loggedInUser, _adapter!!.data)) }

    private val negOnClickListener: DialogInterface.OnClickListener
        get() = DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_sharelink, null, false)
        ButterKnife.bind(this, view)

        // build dialog
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(getString(R.string.dialog_sharelink_title)).setView(view).setPositiveButton(getString(R.string.share), _positiveClicked).setNegativeButton(android.R.string.cancel, _negativeClicked).create()


        message.text = getString(R.string.dialog_sharelink_message)
        // Show the SW Keyboard on dialog start. Always.
        dialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.window.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        _adapter = ShareLinkAdapter.newInstance(recyclerView, groups)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.hasFixedSize()
        recyclerView.adapter = _adapter
    }

    private val groups: HashMap<String, Group>
        @SuppressWarnings("unchecked")
        get() = arguments.getSerializable(ARG_GROUPS) as HashMap<String, Group>?

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * Use the private string TAG from this class as an identifier.

     * @param fragmentManager manager of fragments
     * *
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): ShareLinkDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {

        private val TAG = Companion.getSimpleName(ShareLinkDialog::class.java)
        private val ARG_GROUPS = "com.shareyourproxy.sharelinkdialog.group"

        /**
         * Create a new instance of a [AddChannelDialog].

         * @return A [AddChannelDialog]
         */
        fun newInstance(groups: HashMap<String, Group>): ShareLinkDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putSerializable(ARG_GROUPS, groups)
            //create dialog instance
            val dialog = ShareLinkDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

}
/**
 * Constructor.
 */
