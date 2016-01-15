package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_text
import com.shareyourproxy.R.id.dialog_user_groups_message
import com.shareyourproxy.R.id.dialog_user_groups_recyclerview
import com.shareyourproxy.R.string.save
import com.shareyourproxy.R.string.select_groups_for_channel
import com.shareyourproxy.R.style.Widget_Proxy_App_Dialog
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.command.AddGroupChannelAndPublicCommand
import com.shareyourproxy.api.rx.command.AddGroupsChannelCommand
import com.shareyourproxy.api.rx.event.ChannelAddedEvent
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.SaveGroupChannelAdapter
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import org.solovyev.android.views.llm.LinearLayoutManager


/**
 * Save a new channel to selected groups after creating it.
 */
internal final class SaveGroupChannelDialog private constructor(channel: Channel, user: User) : BaseDialogFragment() {
    companion object {
        private val ARG_CHANNEL = "com.shareyourproxy.savegroupchanneldialog.arg.channel"
        private val ARG_USER = "com.shareyourproxy.savegroupchanneldialog.arg.user"
        fun show(manager: FragmentManager, channel: Channel, user: User): SaveGroupChannelDialog {
            return setArgs(manager, channel, user)
        }

        private fun setArgs(manager: FragmentManager, channel: Channel, user: User): SaveGroupChannelDialog {
            val dialog = SaveGroupChannelDialog(channel, user)
            val args: Bundle = Bundle()
            args.putParcelable(ARG_CHANNEL, channel)
            args.putParcelable(ARG_USER, user)
            dialog.arguments = args
            return dialog.show(manager)
        }
    }


    private val recyclerView: BaseRecyclerView by bindView(dialog_user_groups_recyclerview)
    private val message: TextView by bindView(dialog_user_groups_message)
    private val colorText: Int by bindColor(common_text)
    private val colorBlue: Int by bindColor(common_blue)
    private val stringMessage: String by bindString(select_groups_for_channel)
    private val parcelChannel: Channel = arguments.getParcelable<Channel>(ARG_CHANNEL)
    private val parcelUser: User = arguments.getParcelable<User>(ARG_USER)
    private val negativeClicked = OnClickListener { dialogInterface, i -> updateChannel(dialogInterface) }
    private val positiveClicked = OnClickListener { DialogInterface, i -> dispatchUpdatedUserGroups() }
    private val adapter: SaveGroupChannelAdapter = SaveGroupChannelAdapter(recyclerView, user.groups)

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_user_groups, null, false)
        val dialog = AlertDialog.Builder(activity, Widget_Proxy_App_Dialog)
                .setTitle(R.string.select_groups)
                .setView(view)
                .setPositiveButton(save, positiveClicked)
                .setNegativeButton(cancel, negativeClicked)
                .create()

        dialog.setOnShowListener(onShowListener)
        // Show the SW Keyboard on dialog start. Always.
        dialog.window.attributes.width = MATCH_PARENT
        dialog.window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    private val onShowListener = DialogInterface.OnShowListener {
        val dialog = dialog as AlertDialog
        // Setup Button Colors
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText)
        initializeRecyclerView()
        message.text = stringMessage
    }

    /**
     * Setup the group list UI.
     */
    private fun initializeRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.hasFixedSize()
        recyclerView.adapter = adapter
    }

    private fun updateChannel(dialogInterface: DialogInterface) {
        post(ChannelAddedEvent(parcelUser, parcelChannel))
        dialogInterface.dismiss()
    }

    private fun dispatchUpdatedUserGroups() {
        post(ChannelAddedEvent(parcelUser, parcelChannel))
        if (adapter.isAnyItemChecked) {
            val data = adapter.dataArray
            if (adapter.isPublicChecked) {
                post(AddGroupChannelAndPublicCommand(parcelUser, data, parcelChannel))
            } else {
                post(AddGroupsChannelCommand(parcelUser, data, parcelChannel))
            }
        }
    }

    /**
     * Use the private string TAG from this class as an identifier.
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): SaveGroupChannelDialog {
        show(fragmentManager, SaveGroupChannelDialog::class.java.simpleName)
        return this
    }

}