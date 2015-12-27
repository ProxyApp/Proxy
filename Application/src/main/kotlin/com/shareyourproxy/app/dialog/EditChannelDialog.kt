package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import com.shareyourproxy.R
import com.shareyourproxy.R.id.*
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType.*
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.bindView

/**
 * Dialog that handles editing a selected channel.
 */
class EditChannelDialog : BaseDialogFragment() {
    private val editTextActionAddress: EditText by bindView(dialog_channel_action_address_edittext)
    private val negativeClicked = OnClickListener { dialogInterface, i -> hideSoftwareKeyboard(editTextActionAddress) }
    private val editTextLabel: EditText by bindView(dialog_channel_label_edittext)
    private val floatLabelChannelLabel: TextInputLayout by bindView(dialog_channel_label_floatlabel)
    private val floatLabelAddress: TextInputLayout by bindView(R.id.dialog_channel_action_address_floatlabel)
    internal var colorText: Int = ContextCompat.getColor(context, R.color.common_text)
    internal var colorBlue: Int = ContextCompat.getColor(context, R.color.common_blue)
    internal var stringRequired: String = getString(R.string.required)
    // Transient
    private var channel: Channel = arguments.getParcelable<Channel>(ARG_CHANNEL)
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val onEditorActionListener = OnEditorActionListener { v, actionId, event ->
        if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.KEYCODE_ENDCALL) {
            updateChannelAndExit()
            return@OnEditorActionListener true
        }
        false
    }
    private val positiveClicked = View.OnClickListener { updateChannelAndExit() }
    private var dialogTitle: String? = null
    private var channelAddressHint: String? = null
    private var channelLabelHint: String? = null
    private var position: Int = arguments.getInt(ARG_POSITION)
    private val deleteClicked = OnClickListener { dialogInterface, i ->
        post(DeleteUserChannelCommand(loggedInUser, channel, position))
        dialogInterface.dismiss()
    }

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = editTextActionAddress.text.toString().trim { it <= ' ' }
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = channel.id
            val channelType = channel.channelType
            val channel = if (channel.channelType.equals(Facebook))
                createModelInstance(id, channel.label, channelType, actionContent)
            else
                createModelInstance(id, labelContent, channelType, actionContent)
            //post and save
            post(AddUserChannelCommand(loggedInUser, channel, channel))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_channel, null, false)
        initializeDisplayValues()

        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(dialogTitle).setView(view).setPositiveButton(R.string.save, null).setNegativeButton(android.R.string.cancel, negativeClicked).setNeutralButton(R.string.delete, deleteClicked).create()
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

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), colorText)
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(positiveClicked)
    }

    fun updateChannelAndExit() {
        val addressHasText = editTextActionAddress.text.toString().trim { it <= ' ' }.length > 0
        if (!addressHasText) {
            floatLabelAddress.error = stringRequired
        } else {
            floatLabelAddress.isErrorEnabled = false
            addUserChannel()
            dismiss()
        }
    }

    private fun initializeDisplayValues() {
        val label = channel.channelType.label
        when (channel.channelType) {
            Address -> {
                dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Custom -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_custom)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_custom)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_custom)
            }
            Ello -> {
                dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Email -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_email)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_email)
            }
            Facebook -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_facebook)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            FBMessenger -> {
                dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Github -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Googleplus -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Hangouts -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Instagram -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            LeagueOfLegends -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.username)
                channelLabelHint = getString(R.string.label)
            }
            Linkedin -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Medium -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Meerkat -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            NintendoNetwork -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.username)
                channelLabelHint = getString(R.string.label)
            }
            Periscope -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Phone -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_phone)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_phone)
            }
            PlaystationNetwork -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.username)
                channelLabelHint = getString(R.string.label)
            }
            Reddit -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            Skype -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Slack -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            SMS -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_sms)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_sms)
            }
            Snapchat -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Soundcloud -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Spotify -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Steam -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.username)
                channelLabelHint = getString(R.string.label)
            }
            Tumblr -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Twitch -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.username)
                channelLabelHint = getString(R.string.label)
            }
            Twitter -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            Venmo -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Web, URL -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_web)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_web)
            }
            Whatsapp -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_phone)
                channelLabelHint = getString(R.string.label)
            }
            XboxLive -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.gamertag)
                channelLabelHint = getString(R.string.label)
            }
            Yo -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            Youtube -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                channelLabelHint = getString(R.string.label)
            }
            else -> {
                dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
        }
    }

    /**
     * Initialize values for EditText to switch color.
     */
    private fun initializeEditText() {
        editTextActionAddress.setOnEditorActionListener(onEditorActionListener)
        editTextActionAddress.setText(channel.actionAddress)
        floatLabelAddress.hint = channelAddressHint

        if (channel.channelType.equals(Facebook)) {
            editTextLabel.visibility = View.GONE
            floatLabelChannelLabel.visibility = View.GONE
        } else {
            editTextLabel.setText(channel.label)
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
        show(fragmentManager, TAG)
        return this
    }

    companion object {
        // Final
        private val ARG_CHANNEL = "EditChannelDialog.Channel"
        private val ARG_POSITION = "EditChannelDialog.Position"
        private val TAG = AddChannelDialog::class.java.simpleName

        /**
         * Create a new instance of a [EditChannelDialog].

         * @return A [EditChannelDialog]
         */
        fun newInstance(channel: Channel, position: Int): EditChannelDialog {
            val bundle = Bundle()
            bundle.putParcelable(ARG_CHANNEL, channel)
            bundle.putInt(ARG_POSITION, position)

            val dialog = EditChannelDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
