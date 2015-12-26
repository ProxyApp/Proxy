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
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.command.AddGroupChannelAndPublicCommand
import com.shareyourproxy.api.rx.command.AddGroupsChannelCommand
import com.shareyourproxy.api.rx.event.ChannelAddedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.SaveGroupChannelAdapter
import com.shareyourproxy.util.ObjectUtils

import org.solovyev.android.views.llm.LinearLayoutManager

import java.util.ArrayList

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindString
import butterknife.ButterKnife


/**
 * Save a new channel to selected groups after creating it.
 */
class SaveGroupChannelDialog : BaseDialogFragment() {
    @Bind(R.id.dialog_user_groups_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.dialog_user_groups_message)
    internal var message: TextView
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    @BindString(R.string.select_groups_for_channel)
    internal var stringMessage: String
    private var _channel: Channel? = null
    private var _user: User? = null
    private val _negativeClicked = DialogInterface.OnClickListener { dialogInterface, i ->
        rxBus.post(ChannelAddedEvent(_user, _channel))
        dialogInterface.dismiss()
    }
    private var _adapter: SaveGroupChannelAdapter? = null
    private val _positiveClicked = DialogInterface.OnClickListener { dispatchUpdatedUserGroups() }

    private fun dispatchUpdatedUserGroups() {
        val rxBus = rxBus
        rxBus.post(ChannelAddedEvent(_user, _channel))
        if (_adapter!!.isAnyItemChecked) {
            val data = _adapter!!.dataArray
            if (_adapter!!.isPublicChecked) {
                rxBus.post(AddGroupChannelAndPublicCommand(
                        _user, data, _channel))
            } else {
                rxBus.post(
                        AddGroupsChannelCommand(_user, data, _channel))
            }
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        _channel = arguments.getParcelable<Channel>(ARG_CHANNEL)
        _user = arguments.getParcelable<User>(ARG_USER)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_user_groups, null, false)
        ButterKnife.bind(this, view)
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(R.string.select_groups).setView(view).setPositiveButton(getString(R.string.save), _positiveClicked).setNegativeButton(android.R.string.cancel, _negativeClicked).create()

        message.text = stringMessage
        // Show the SW Keyboard on dialog start. Always.
        dialog.window.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        // Setup Button Colors
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText)
        initializeRecyclerView()
    }

    /**
     * Setup the group list UI.
     */
    private fun initializeRecyclerView() {
        _adapter = SaveGroupChannelAdapter.newInstance(recyclerView, _user!!.groups())

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.hasFixedSize()
        recyclerView.adapter = _adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * Use the private string TAG from this class as an identifier.

     * @param fragmentManager manager of fragments
     * *
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): SaveGroupChannelDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {
        private val ARG_CHANNEL = "com.shareyourproxy.savegroupchanneldialog.arg.channel"
        private val ARG_USER = "com.shareyourproxy.savegroupchanneldialog.arg.user"
        private val TAG = ObjectUtils.Companion.getSimpleName(SaveGroupChannelDialog::class.java)

        /**
         * Create a new instance of a [SaveGroupChannelDialog].

         * @return A [SaveGroupChannelDialog]
         */
        fun newInstance(channel: Channel, user: User): SaveGroupChannelDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putParcelable(ARG_USER, user)
            bundle.putParcelable(ARG_CHANNEL, channel)
            //create dialog instance
            val dialog = SaveGroupChannelDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
/**
 * Constructor.
 */
