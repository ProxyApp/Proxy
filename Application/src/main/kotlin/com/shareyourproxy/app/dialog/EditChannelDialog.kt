package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Activity
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand
import com.shareyourproxy.util.ObjectUtils

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindString
import butterknife.ButterKnife

import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Dialog that handles editing a selected channel.
 */
class EditChannelDialog : BaseDialogFragment() {
    // View
    @Bind(R.id.dialog_channel_action_address_edittext)
    internal var editTextActionAddress: EditText
    private val _negativeClicked = OnClickListener { hideSoftwareKeyboard(editTextActionAddress) }
    @Bind(R.id.dialog_channel_label_edittext)
    internal var editTextLabel: EditText
    @Bind(R.id.dialog_channel_label_floatlabel)
    internal var floatLabelChannelLabel: TextInputLayout
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    internal var floatLabelAddress: TextInputLayout
    // Color
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    @BindString(R.string.required)
    internal var stringRequired: String
    // Transient
    private var _channel: Channel? = null
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val _onEditorActionListener = OnEditorActionListener { v, actionId, event ->
        if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.KEYCODE_ENDCALL) {
            updateChannelAndExit()
            return@OnEditorActionListener true
        }
        false
    }
    private val _positiveClicked = View.OnClickListener { updateChannelAndExit() }
    private var _dialogTitle: String? = null
    private var _channelAddressHint: String? = null
    private var _channelLabelHint: String? = null
    private var _position: Int = 0
    private val _deleteClicked = OnClickListener { dialogInterface, i ->
        rxBus.post(
                DeleteUserChannelCommand(loggedInUser, _channel, _position))
        dialogInterface.dismiss()
    }

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = editTextActionAddress.text.toString().trim { it <= ' ' }
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = _channel!!.id()
            val channelType = _channel!!.channelType()
            val channel = if (_channel!!.channelType().equals(ChannelType.Facebook))
                createModelInstance(id, _channel!!.label(), channelType, actionContent)
            else
                createModelInstance(id, labelContent, channelType, actionContent)
            //post and save
            rxBus.post(AddUserChannelCommand(loggedInUser, channel, _channel))
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        _channel = arguments.getParcelable<Channel>(ARG_CHANNEL)
        _position = arguments.getInt(ARG_POSITION)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_channel, null, false)
        ButterKnife.bind(this, view)
        initializeDisplayValues()

        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(_dialogTitle).setView(view).setPositiveButton(R.string.save, null).setNegativeButton(android.R.string.cancel, _negativeClicked).setNeutralButton(R.string.delete, _deleteClicked).create()
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
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked)
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
        val label = _channel!!.channelType().getLabel()
        when (_channel!!.channelType()) {
            Address -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Custom -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_custom)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_custom)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_custom)
            }
            Ello -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Email -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_email)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_email)
            }
            Facebook -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_facebook)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            FBMessenger -> {
                _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Github -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Googleplus -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Hangouts -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Instagram -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            LeagueOfLegends -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            Linkedin -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Medium -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Meerkat -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            NintendoNetwork -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            Periscope -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Phone -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_phone)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_phone)
            }
            PlaystationNetwork -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            Reddit -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            Skype -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Slack -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            SMS -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_sms)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_sms)
            }
            Snapchat -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Soundcloud -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Spotify -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Steam -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            Tumblr -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Twitch -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.username)
                _channelLabelHint = getString(R.string.label)
            }
            Twitter -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
            Venmo -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Web, URL -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_web)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_web)
            }
            Whatsapp -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_phone)
                _channelLabelHint = getString(R.string.label)
            }
            XboxLive -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.gamertag)
                _channelLabelHint = getString(R.string.label)
            }
            Yo -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            Youtube -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(
                        R.string.dialog_channel_hint_address_blank_handle, label)
                _channelLabelHint = getString(R.string.label)
            }
            else -> {
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label)
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default)
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default)
            }
        }
    }

    /**
     * Initialize values for EditText to switch color.
     */
    private fun initializeEditText() {
        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener)
        editTextActionAddress.setText(_channel!!.actionAddress())
        floatLabelAddress.hint = _channelAddressHint

        if (_channel!!.channelType().equals(ChannelType.Facebook)) {
            editTextLabel.visibility = View.GONE
            floatLabelChannelLabel.visibility = View.GONE
        } else {
            editTextLabel.setText(_channel!!.label())
            floatLabelChannelLabel.hint = _channelLabelHint
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
        private val TAG = ObjectUtils.Companion.getSimpleName(AddChannelDialog::class.java)

        /**
         * Create a new instance of a [EditChannelDialog].

         * @return A [EditChannelDialog]
         */
        fun newInstance(
                channel: Channel, position: Int): EditChannelDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putParcelable(ARG_CHANNEL, channel)
            bundle.putInt(ARG_POSITION, position)
            //copy dialog instance
            val dialog = EditChannelDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
/**
 * Constructor.
 */
