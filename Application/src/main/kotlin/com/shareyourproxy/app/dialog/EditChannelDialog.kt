package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_text
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.layout.dialog_add_channel
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType.*
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Dialog that handles editing a selected channel.
 */
internal final class EditChannelDialog private constructor() : BaseDialogFragment() {
    companion object {
        private val ARG_CHANNEL = "EditChannelDialog.Channel"
        private val ARG_POSITION = "EditChannelDialog.Position"
        fun show(manager: FragmentManager, channel: Channel, position: Int): EditChannelDialog {
            return setArgs(manager, channel, position)
        }

        private fun setArgs(manager: FragmentManager, channel: Channel, position: Int): EditChannelDialog {
            val dialog = EditChannelDialog()
            val args: Bundle = Bundle()
            args.putParcelable(ARG_CHANNEL, channel)
            args.putInt(ARG_POSITION, position)
            dialog.arguments = args
            return dialog.show(manager)
        }
    }

    private val editTextActionAddress: EditText by bindView(dialog_channel_action_address_edittext)
    private val editTextLabel: EditText by bindView(dialog_channel_label_edittext)
    private val floatLabelChannelLabel: TextInputLayout by bindView(dialog_channel_label_floatlabel)
    private val floatLabelAddress: TextInputLayout by bindView(dialog_channel_action_address_floatlabel)
    private val colorText: Int by bindColor(common_text)
    private val colorBlue: Int by bindColor(common_blue)
    private val stringRequired: String by bindString(required)
    // Transient
    private val parcelChannel: Channel by LazyVal { arguments.getParcelable<Channel>(ARG_CHANNEL) }
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val onEditorActionListener = OnEditorActionListener { v, actionId, event ->
        when (actionId) {
            KeyEvent.KEYCODE_ENDCALL,
            KeyEvent.KEYCODE_ENTER -> updateChannelAndExit()
            else -> false
        }
    }
    private val parcelPosition: Int = arguments.getInt(ARG_POSITION)
    private val negativeClicked = OnClickListener { dialogInterface, i -> hideSoftwareKeyboard(editTextActionAddress) }
    private val positiveClicked = View.OnClickListener { updateChannelAndExit() }
    private val deleteClicked = OnClickListener { dialogInterface, i ->
        post(DeleteUserChannelCommand(loggedInUser, parcelChannel, parcelPosition))
        dialogInterface.dismiss()
    }
    private var dialogTitle: String = ""
    private var channelAddressHint: String = ""
    private var channelLabelHint: String = ""

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = editTextActionAddress.text.toString().trim { it <= ' ' }
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = parcelChannel.id
            val channelType = parcelChannel.channelType
            val channel = if (parcelChannel.channelType.equals(Facebook))
                createModelInstance(id, parcelChannel.label, channelType, actionContent)
            else
                createModelInstance(id, labelContent, channelType, actionContent)
            //post and save
            post(AddUserChannelCommand(loggedInUser, channel, parcelChannel))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(dialog_add_channel, null, false)
        initializeDisplayValues()

        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(dialogTitle).setView(view)
                .setPositiveButton(save, null)
                .setNegativeButton(cancel, negativeClicked)
                .setNeutralButton(delete, deleteClicked).create()

        dialog.setOnShowListener(onShowListener)
        //Override the dialog wrapping content and cancel dismiss on click outside
        // of the dialog window
        dialog.window.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        // Show the SW Keyboard on dialog start. Always.
        dialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.setCanceledOnTouchOutside(false)
        initializeEditText()
        return dialog
    }

    private val onShowListener = DialogInterface.OnShowListener {
        val dialog = dialog as AlertDialog
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText)
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(positiveClicked)
    }

    private fun updateChannelAndExit(): Boolean {
        val addressHasText = editTextActionAddress.text.toString().trim { it <= ' ' }.length > 0
        if (!addressHasText) {
            floatLabelAddress.error = stringRequired
        } else {
            floatLabelAddress.isErrorEnabled = false
            addUserChannel()
            dismiss()
        }
        return true
    }

    private fun initializeDisplayValues() {
        val name = parcelChannel.channelType.label
        when (parcelChannel.channelType) {
            Address -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Custom -> {
                dialogTitle = getString(dialog_editchannel_title_custom)
                channelAddressHint = getString(dialog_editchannel_hint_address_custom)
                channelLabelHint = getString(dialog_editchannel_hint_label_custom)
            }
            Ello -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Email -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_editchannel_hint_address_email)
                channelLabelHint = getString(dialog_editchannel_hint_label_email)
            }
            Facebook -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_editchannel_hint_address_facebook)
                channelLabelHint = getString(dialog_editchannel_hint_label_default)
            }
            FBMessenger -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Github -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Googleplus -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Hangouts -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(
                        dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Instagram -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            LeagueOfLegends -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Linkedin -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Medium -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Meerkat -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            NintendoNetwork -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Periscope -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Phone -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_phone)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_phone)
            }
            PlaystationNetwork -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Reddit -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_editchannel_hint_address_default)
                channelLabelHint = getString(dialog_editchannel_hint_label_default)
            }
            Skype -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Slack -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            SMS -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_sms)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_sms)
            }
            Snapchat -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Soundcloud -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Spotify -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Steam -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Tumblr -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Twitch -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Twitter -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_editchannel_hint_address_default)
                channelLabelHint = getString(dialog_editchannel_hint_label_default)
            }
            Venmo -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Web, URL -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_web)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_web)
            }
            Whatsapp -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_phone)
                channelLabelHint = getString(label)
            }
            XboxLive -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(gamertag)
                channelLabelHint = getString(label)
            }
            Yo -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Youtube -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            else -> {
                dialogTitle = getString(dialog_editchannel_title_blank, name)
                channelAddressHint = getString(dialog_editchannel_hint_address_default)
                channelLabelHint = getString(dialog_editchannel_hint_label_default)
            }
        }
    }

    /**
     * Initialize values for EditText to switch color.
     */
    private fun initializeEditText() {
        editTextActionAddress.setOnEditorActionListener(onEditorActionListener)
        editTextActionAddress.setText(parcelChannel.actionAddress)
        floatLabelAddress.hint = channelAddressHint

        if (parcelChannel.channelType.equals(Facebook)) {
            editTextLabel.visibility = View.GONE
            floatLabelChannelLabel.visibility = View.GONE
        } else {
            editTextLabel.setText(parcelChannel.label)
            floatLabelChannelLabel.hint = channelLabelHint
        }
    }

    /**
     * Use the private string TAG from this class as an identifier.

     * @param fragmentManager manager of fragments
     * *
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): EditChannelDialog {
        show(fragmentManager, AddChannelDialog::class.java.simpleName)
        return this
    }
}
