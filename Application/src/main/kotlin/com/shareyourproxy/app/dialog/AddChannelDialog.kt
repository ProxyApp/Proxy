package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.event.AddChannelDialogSuccessEvent

import java.util.UUID

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindString
import butterknife.ButterKnife

import android.content.DialogInterface.OnClickListener
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.util.ObjectUtils.getSimpleName
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Add a new [Channel] to a [User].
 */
class AddChannelDialog : BaseDialogFragment() {
    @Bind(R.id.dialog_channel_action_address_edittext)
    internal var editTextActionAddress: EditText
    private val _negativeClicked = OnClickListener {
        hideSoftwareKeyboard(editTextActionAddress)
        dismiss()
    }
    @Bind(R.id.dialog_channel_label_edittext)
    internal var editTextLabel: EditText
    @Bind(R.id.dialog_channel_label_floatlabel)
    internal var floatLabelChannelLabel: TextInputLayout
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    internal var floatLabelAddress: TextInputLayout
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_divider)
    internal var colorGray: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    @BindString(R.string.required)
    internal var _required: String
    private var _channelType: ChannelType? = null
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val _onEditorActionListener = OnEditorActionListener { v, actionId, event ->
        // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
        // FixedDecimalEditText's inputType is Decimal
        if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.KEYCODE_ENDCALL) {
            saveChannelAndExit()
            return@OnEditorActionListener true
        }
        false
    }
    private val _positiveClicked = View.OnClickListener { saveChannelAndExit() }
    private var _dialogTitle: String? = null
    private var _channelAddressHint: String? = null
    private var _channelLabelHint: String? = null

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        //TODO:CLEAN THIS MESS UP
        val actionContent = editTextActionAddress.text.toString()
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = UUID.randomUUID().toString()
            val channel = createModelInstance(id, labelContent, _channelType, actionContent)
            val rxBus = rxBus
            val user = loggedInUser
            rxBus.post(AddUserChannelCommand(user, channel))
            user.channels().put(channel.id(), channel)
            rxBus.post(AddChannelDialogSuccessEvent(user, channel))
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        _channelType = ChannelType.valueOfLabel(arguments.getString(ARG_CHANNEL_TYPE))
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_channel, null, false)
        ButterKnife.bind(this, view)
        initializeDisplayValues()

        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener)
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(_dialogTitle).setView(view).setPositiveButton(getString(R.string.save), null).setNegativeButton(android.R.string.cancel, _negativeClicked).create()

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
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked)
        //Setup TextInput hints.
        floatLabelAddress.hint = _channelAddressHint
        floatLabelChannelLabel.hint = _channelLabelHint
    }

    fun saveChannelAndExit() {
        val addressHasText = editTextActionAddress.text.toString().trim { it <= ' ' }.length > 0
        if (!addressHasText) {
            floatLabelAddress.error = _required
        } else {
            floatLabelAddress.isErrorEnabled = false
            addUserChannel()
            dismiss()
        }
    }

    private fun initializeDisplayValues() {
        val name = _channelType!!.label
        when (_channelType) {
            ChannelType.Address -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Custom -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_custom)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_custom)
            }
            ChannelType.Ello -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Email -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_email)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_email)
            }
            ChannelType.Facebook -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default)
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.FBMessenger -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Github -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Googleplus -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Hangouts -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Instagram -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.LeagueOfLegends -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Linkedin -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Medium -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Meerkat -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.NintendoNetwork -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Periscope -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Phone -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_channel_hint_address_phone)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_phone)
            }
            ChannelType.PlaystationNetwork -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Reddit -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default)
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Skype -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Slack -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.SMS -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_sms)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_sms)
            }
            ChannelType.Snapchat -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Soundcloud -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Spotify -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Steam -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Tumblr -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Twitch -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Twitter -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default)
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Venmo -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Web, ChannelType.URL -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_web)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_web)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_web)
            }
            ChannelType.Whatsapp -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_phone)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.XboxLive -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(R.string.gamertag)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Yo -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            ChannelType.Youtube -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, name)
                _channelLabelHint = getString(R.string.label)
            }
            else -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_default)
                _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_default)
            }
        }
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
    fun show(fragmentManager: FragmentManager): AddChannelDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {
        private val ARG_CHANNEL_TYPE = "AddChannelDialog.ChannelType"
        private val TAG = Companion.getSimpleName(AddChannelDialog::class.java)

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
/**
 * Constructor.
 */
