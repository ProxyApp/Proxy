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
import android.widget.RadioButton
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.AddUserChannelCommand

import java.util.UUID

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindString
import butterknife.ButterKnife
import butterknife.OnCheckedChanged

import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.util.ObjectUtils.getSimpleName
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard

/**
 * Add a new Reddit [Channel] to a [User].
 */
class AddRedditChannelDialog : BaseDialogFragment() {
    @Bind(R.id.dialog_reddit_channel_action_address_edittext)
    internal var editTextActionAddress: EditText
    private val _negativeClicked = DialogInterface.OnClickListener {
        hideSoftwareKeyboard(editTextActionAddress)
        dismiss()
    }
    @Bind(R.id.dialog_reddit_channel_label_edittext)
    internal var editTextLabel: EditText
    @Bind(R.id.dialog_reddit_channel_label_floatlabel)
    internal var floatLabelChannelLabel: TextInputLayout
    @Bind(R.id.dialog_reddit_channel_action_address_floatlabel)
    internal var floatLabelAddress: TextInputLayout
    @Bind(R.id.dialog_reddit_channel_linktype_header)
    internal var linkTypeHeader: TextView
    @Bind(R.id.dialog_reddit_channel_radiobutton_profile)
    internal var linkTypeProfile: RadioButton
    @Bind(R.id.dialog_reddit_channel_radiobutton_subreddit)
    internal var linkTypeSub: RadioButton
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    @BindString(R.string.required)
    internal var _required: String
    private var _channelType: ChannelType? = null
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val _onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
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

    @OnCheckedChanged(R.id.dialog_reddit_channel_radiobutton_subreddit)
    protected fun onSubRedditChecked(checked: Boolean) {
        if (checked) {
            _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_reddit_subreddit)
            floatLabelAddress.hint = _channelAddressHint

        }
    }

    @OnCheckedChanged(R.id.dialog_reddit_channel_radiobutton_profile)
    protected fun onProfileChecked(checked: Boolean) {
        if (checked) {
            _channelLabelHint = getString(R.string.dialog_addchannel_hint_address_reddit_username)
            floatLabelAddress.hint = _channelLabelHint
        }
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

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = actionAddress
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = UUID.randomUUID().toString()
            val channel = createModelInstance(id, labelContent, _channelType, actionContent)
            rxBus.post(AddUserChannelCommand(loggedInUser, channel))
        }
    }

    private val actionAddress: String
        get() {
            val action = editTextActionAddress.text.toString()
            if (linkTypeProfile.isChecked) {
                return getString(R.string.reddit_linktype_profile, action)
            } else if (linkTypeSub.isChecked) {
                return getString(R.string.reddit_linktype_subreddit, action)
            } else {
                return action
            }
        }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        _channelType = ChannelType.valueOfLabel(arguments.getString(ARG_CHANNEL_TYPE))
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_reddit_channel, null, false)
        ButterKnife.bind(this, view)
        initializeDisplayValues()

        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener)
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(_dialogTitle).setView(view).setPositiveButton(R.string.save, null).setNegativeButton(android.R.string.cancel, _negativeClicked).create()

        // Show the SW Keyboard on dialog start. Always.
        dialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.window.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.setCanceledOnTouchOutside(false)

        // Setup Button Colors
        initializeEditTextColors()
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked)
    }

    private fun initializeDisplayValues() {
        val name = _channelType!!.label
        _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
        _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_reddit_username)
        _channelLabelHint = getString(R.string.label)
    }

    /**
     * Initialize color and hints for edit text.
     */
    private fun initializeEditTextColors() {
        floatLabelAddress.hint = _channelAddressHint
        floatLabelChannelLabel.hint = _channelLabelHint
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
    fun show(fragmentManager: FragmentManager): AddRedditChannelDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {


        private val ARG_CHANNEL_TYPE = "AddRedditChannelDialog.ChannelType"
        private val TAG = Companion.getSimpleName(AddRedditChannelDialog::class.java)

        /**
         * Create a new instance of a [AddRedditChannelDialog].

         * @return A [AddRedditChannelDialog]
         */
        fun newInstance(channelType: ChannelType): AddRedditChannelDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putString(ARG_CHANNEL_TYPE, channelType.label)
            //create dialog instance
            val dialog = AddRedditChannelDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
/**
 * Constructor.
 */




