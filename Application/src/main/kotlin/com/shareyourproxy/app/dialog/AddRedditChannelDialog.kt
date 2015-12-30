package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.annotation.SuppressLint
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
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_text
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.string.required
import com.shareyourproxy.R.string.save
import com.shareyourproxy.R.style.Widget_Proxy_App_Dialog
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView
import java.util.*

/**
 * Add a new Reddit [Channel] to a [User].
 */
class AddRedditChannelDialog : BaseDialogFragment() {
    private val editTextActionAddress: EditText by bindView(dialog_reddit_channel_action_address_edittext)
    private val negativeClicked = DialogInterface.OnClickListener { dialogInterface, i ->
        hideSoftwareKeyboard(editTextActionAddress)
        dialogInterface.dismiss()
    }
    private val editTextLabel: EditText by bindView(dialog_reddit_channel_label_edittext)
    private val floatLabelChannelLabel: TextInputLayout by bindView(dialog_reddit_channel_label_floatlabel)
    private val floatLabelAddress: TextInputLayout by bindView(dialog_reddit_channel_action_address_floatlabel)
    private val linkTypeProfile: RadioButton by bindView(dialog_reddit_channel_radiobutton_profile)
    private val linkTypeSub: RadioButton by bindView(dialog_reddit_channel_radiobutton_subreddit)
    private val colorText: Int by bindColor(common_text)
    private val colorBlue: Int by bindColor(common_blue)
    private val stringRequired: String by bindString(required)
    private val channelType: ChannelType = ChannelType.valueOfLabel(arguments.getString(ARG_CHANNEL_TYPE))
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is pressed.
     */
    private val onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
        // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button
        when(actionId){
            KeyEvent.KEYCODE_ENDCALL,
            KeyEvent.KEYCODE_ENTER -> saveChannelAndExit()
            else -> false
        }
    }
    private val positiveClicked = View.OnClickListener { saveChannelAndExit() }
    private var dialogTitle: String = ""
    private var channelAddressHint: String = ""
    private var channelLabelHint: String = ""

    private val onSubRedditChecked = CompoundButton.OnCheckedChangeListener { compoundButton, checked ->
        if (checked) {
            channelAddressHint = getString(R.string.dialog_addchannel_hint_address_reddit_subreddit)
            floatLabelAddress.hint = channelAddressHint

        }
    }


    private val onProfileChecked = CompoundButton.OnCheckedChangeListener { compoundButton, checked ->
        if (checked) {
            channelLabelHint = getString(R.string.dialog_addchannel_hint_address_reddit_username)
            floatLabelAddress.hint = channelLabelHint
        }
    }

    fun saveChannelAndExit() :Boolean {
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

    /**
     * Dispatch a Channel Added Event
     */
    private fun addUserChannel() {
        val actionContent = actionAddress
        val labelContent = editTextLabel.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(actionContent.trim { it <= ' ' })) {
            val id = UUID.randomUUID().toString()
            val channel = createModelInstance(id, labelContent, channelType, actionContent)
            post(AddUserChannelCommand(loggedInUser, channel))
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

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_reddit_channel, null, false)
        initializeDisplayValues()
        linkTypeProfile.setOnCheckedChangeListener(onProfileChecked)
        linkTypeSub.setOnCheckedChangeListener(onSubRedditChecked)
        editTextActionAddress.setOnEditorActionListener(onEditorActionListener)
        val dialog = AlertDialog.Builder(activity, Widget_Proxy_App_Dialog)
                .setTitle(dialogTitle)
                .setView(view)
                .setPositiveButton(save, null)
                .setNegativeButton(cancel, negativeClicked)
                .create()

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
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(positiveClicked)
    }

    private fun initializeDisplayValues() {
        val name = channelType.label
        dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name)
        channelAddressHint = getString(R.string.dialog_addchannel_hint_address_reddit_username)
        channelLabelHint = getString(R.string.label)
    }

    /**
     * Initialize color and hints for edit text.
     */
    private fun initializeEditTextColors() {
        floatLabelAddress.hint = channelAddressHint
        floatLabelChannelLabel.hint = channelLabelHint
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
        private val TAG = AddRedditChannelDialog::class.java.simpleName

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



