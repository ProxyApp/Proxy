package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.annotation.SuppressLint
import android.app.Dialog.*
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.shareyourproxy.IntentLauncher
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_text
import com.shareyourproxy.R.id.dialog_channel_auth_action_address_edittext
import com.shareyourproxy.R.id.dialog_channel_auth_action_address_floatlabel
import com.shareyourproxy.R.layout.dialog_auth_channel
import com.shareyourproxy.R.string.*
import com.shareyourproxy.R.style.Widget_Proxy_App_Dialog
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Add a channel that requires OAuth.
 */
internal final class AddAuthChannelDialog private constructor(channel: Channel) : BaseDialogFragment() {
    private val parcelChannel: Channel by LazyVal { arguments.getParcelable<Channel>(ARG_CHANNEL) }
    private val editTextActionAddress: EditText by bindView(dialog_channel_auth_action_address_edittext)
    private val floatLabelAddress: TextInputLayout by bindView(dialog_channel_auth_action_address_floatlabel)
    private val colorText: Int by bindColor(common_text)
    private val colorBlue: Int by bindColor(common_blue)
    private val helpClicked = OnClickListener { dialogInterface, i -> IntentLauncher.launchFacebookHelpIntent(activity) }
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.KEYCODE_ENDCALL) {
            addUserChannel()
            return@OnEditorActionListener true
        }
        false
    }
    private val negativeClicked = OnClickListener { dialogInterface, i ->
        hideKeyboardAndDismiss(dialogInterface)
    }
    private val positiveClicked = OnClickListener { dialogInterface, i -> addUserChannel() }


    companion object {
        private val ARG_CHANNEL = "AddAuthChannelDialog.Channel"
        fun show(manager: FragmentManager, channel: Channel): AddAuthChannelDialog {
            return setArgs(manager, channel)
        }

        private fun setArgs(manager: FragmentManager, channel: Channel): AddAuthChannelDialog {
            val dialog = AddAuthChannelDialog(channel)
            val args: Bundle = Bundle()
            args.putParcelable(ARG_CHANNEL, channel)
            dialog.arguments = args
            return dialog.show(manager)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(dialog_auth_channel, null, false)
        editTextActionAddress.setOnEditorActionListener(onEditorActionListener)
        val dialog = AlertDialog.Builder(activity, Widget_Proxy_App_Dialog)
                .setTitle(getString(dialog_addchannel_title_add_blank, parcelChannel.channelType.label))
                .setView(view)
                .setPositiveButton(save, positiveClicked)
                .setNegativeButton(cancel, negativeClicked)
                .setNeutralButton(common_help, helpClicked)
                .create()
        dialog.setOnShowListener(onShowListener)
        // Show the SW Keyboard on dialog start. Always.
        dialog.window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.window.attributes.width = MATCH_PARENT
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }


    private val onShowListener = DialogInterface.OnShowListener {
        val dialog = dialog as AlertDialog
        // Setup Button Colors
        setButtonTint(dialog.getButton(BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(BUTTON_NEGATIVE), colorText)
        setButtonTint(dialog.getButton(BUTTON_NEUTRAL), colorText)
        // Set TextInput hint
        floatLabelAddress.hint = getString(dialog_channel_hint_address_blank_handle, parcelChannel.channelType.label)
    }

    /**
     * Use the private string TAG from this class as an identifier.
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): AddAuthChannelDialog {
        show(fragmentManager, AddAuthChannelDialog::class.java.simpleName)
        return this
    }

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = editTextActionAddress.text.toString()
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val channel = createModelInstance(parcelChannel, actionContent)
            post(AddUserChannelCommand(loggedInUser, channel))
        }
        dismiss()
    }

    private fun hideKeyboardAndDismiss(dialogInterface: DialogInterface) {
        hideSoftwareKeyboard(editTextActionAddress)
        dialogInterface.dismiss()
    }
}
