package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat.getColor
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
import com.shareyourproxy.R.string.*
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.ChannelType.*
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccessEvent
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.bindView
import java.util.*

/**
 * Add a new [Channel] to a [User].
 */
class AddChannelDialog : BaseDialogFragment() {
    private val editTextActionAddress: EditText by bindView(dialog_channel_action_address_edittext)
    private val negativeClicked = OnClickListener { dialogInterface, i ->
        hideSoftwareKeyboard(editTextActionAddress)
        dismiss()
    }
    private val editTextLabel: EditText by bindView(dialog_channel_label_edittext)
    private val floatLabelChannelLabel: TextInputLayout by bindView(dialog_channel_label_floatlabel)
    private val floatLabelAddress: TextInputLayout by bindView(dialog_channel_action_address_floatlabel)
    internal var colorText: Int = getColor(context, common_text)
    internal var colorBlue: Int = getColor(context, common_blue)
    internal var required: String = getString(R.string.required)
    private var channelType: ChannelType = ChannelType.valueOfLabel(arguments.getString(ARG_CHANNEL_TYPE))
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val onEditorActionListener = OnEditorActionListener { v, actionId, event ->
        // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
        // FixedDecimalEditText's inputType is Decimal
        if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.KEYCODE_ENDCALL) {
            saveChannelAndExit()
            return@OnEditorActionListener true
        }
        false
    }
    private val positiveClicked = View.OnClickListener { saveChannelAndExit() }
    private var dialogTitle: String? = null
    private var channelAddressHint: String? = null
    private var channelLabelHint: String? = null

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        //TODO:CLEAN THIS MESS UP
        val actionContent = editTextActionAddress.text.toString()
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = UUID.randomUUID().toString()
            val channel = createModelInstance(id, labelContent, channelType, actionContent)
            val user = loggedInUser
            post(AddUserChannelCommand(user, channel))
            user.channels.put(channel.id, channel)
            post(AddChannelDialogSuccessEvent(user, channel))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_channel, null, false)
        initializeDisplayValues()

        editTextActionAddress.setOnEditorActionListener(onEditorActionListener)
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(dialogTitle).setView(view).setPositiveButton(getString(R.string.save), null).setNegativeButton(android.R.string.cancel, negativeClicked).create()

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
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(positiveClicked)
        //Setup TextInput hints.
        floatLabelAddress.hint = channelAddressHint
        floatLabelChannelLabel.hint = channelLabelHint
    }

    fun saveChannelAndExit() {
        val addressHasText = editTextActionAddress.text.toString().trim { it <= ' ' }.length > 0
        if (!addressHasText) {
            floatLabelAddress.error = required
        } else {
            floatLabelAddress.isErrorEnabled = false
            addUserChannel()
            dismiss()
        }
    }

    private fun initializeDisplayValues() {
        val name = channelType.label
        when (channelType) {
            Address -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Custom -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_addchannel_hint_address_custom)
                channelLabelHint = getString(dialog_addchannel_hint_label_custom)
            }
            Ello -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Email -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_addchannel_hint_address_email)
                channelLabelHint = getString(dialog_addchannel_hint_label_email)
            }
            Facebook -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            FBMessenger -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Github -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Googleplus -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Hangouts -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Instagram -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            LeagueOfLegends -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Linkedin -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Medium -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Meerkat -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            NintendoNetwork -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Periscope -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Phone -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_phone)
                channelLabelHint = getString(dialog_addchannel_hint_label_phone)
            }
            PlaystationNetwork -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Reddit -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Skype -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Slack -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            SMS -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_addchannel_hint_address_sms)
                channelLabelHint = getString(dialog_addchannel_hint_label_sms)
            }
            Snapchat -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Soundcloud -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Spotify -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Steam -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Tumblr -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Twitch -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(username)
                channelLabelHint = getString(label)
            }
            Twitter -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_addchannel_hint_address_default)
                channelLabelHint = getString(dialog_addchannel_hint_label_default)
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Venmo -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Web, URL -> {
                dialogTitle = getString(dialog_addchannel_title_web)
                channelAddressHint = getString(dialog_addchannel_hint_address_web)
                channelLabelHint = getString(dialog_addchannel_hint_label_web)
            }
            Whatsapp -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_phone)
                channelLabelHint = getString(label)
            }
            XboxLive -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(gamertag)
                channelLabelHint = getString(label)
            }
            Yo -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            Youtube -> {
                dialogTitle = getString(dialog_addchannel_title_add_blank, name)
                channelAddressHint = getString(dialog_channel_hint_address_blank_handle, name)
                channelLabelHint = getString(label)
            }
            else -> {
                dialogTitle = getString(R.string.dialog_addchannel_title_default)
                channelAddressHint = getString(dialog_addchannel_hint_address_default)
                channelLabelHint = getString(dialog_addchannel_hint_label_default)
            }
        }
    }

    /**
     * Use the private string TAG from this class as an identifier.
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): AddChannelDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {
        private val ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType"
        private val TAG = AddChannelDialog::class.java.simpleName

        /**
         * Create a new instance of a [AddChannelDialog].
         * @return A [AddChannelDialog]
         */
        fun newInstance(channelType: ChannelType): AddChannelDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putString(ARG_CHANNEL_TYPE, channelType.label)
            //create dialog instance
            val dialog = AddChannelDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
