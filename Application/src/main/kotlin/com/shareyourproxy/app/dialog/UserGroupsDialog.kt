package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.view.WindowManager
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_blue
import com.shareyourproxy.R.color.common_text
import com.shareyourproxy.R.layout.dialog_user_groups
import com.shareyourproxy.R.string.dialog_edit_user_groups
import com.shareyourproxy.R.string.save
import com.shareyourproxy.R.style.Widget_Proxy_App_Dialog
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.command.SaveGroupContactsCommand
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.UserGroupsAdapter
import com.shareyourproxy.util.bindView
import org.solovyev.android.views.llm.LinearLayoutManager
import java.util.*

/**
 * This Dialog provides a toggle selection to add a User contactId to the logged in User's various saved groups.
 */
class UserGroupsDialog : BaseDialogFragment() {
    private val negativeClicked = OnClickListener { dialogInterface, i -> dialogInterface.dismiss() }
    private val recyclerView: BaseRecyclerView by bindView(R.id.dialog_user_groups_recyclerview)
    private val message: TextView by bindView(R.id.dialog_user_groups_message)
    internal var colorText: Int = getColor(context, common_text)
    internal var colorBlue: Int = getColor(context, common_blue)
    private var adapter: UserGroupsAdapter = UserGroupsAdapter.newInstance(recyclerView, arguments.getParcelableArrayList<GroupToggle>(ARG_GROUPS))
    private val positiveClicked = OnClickListener { dialogInterface, i ->
        dispatchUpdatedUserGroups()
    }

    /**
     * Issue a save group contacts command.
     */
    private fun dispatchUpdatedUserGroups() {
        RxGoogleAnalytics(activity).contactGroupButtonHit()
        post(SaveGroupContactsCommand(loggedInUser, adapter.data, userArg))
    }

    /**
     * Get the logged in user
     * @return user
     */
    private val userArg: User get() = arguments.getParcelable<User>(ARG_USER)

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(dialog_user_groups, null, false)
        val title = getString(dialog_edit_user_groups, userArg.first)
        val dialog = AlertDialog.Builder(activity, Widget_Proxy_App_Dialog)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(save, positiveClicked)
                .setNegativeButton(cancel, negativeClicked)
                .create()

        message.text = getString(R.string.dialog_group_channel_message)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), colorBlue)
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), colorText)
        initializeRecyclerView()
    }

    /**
     * Setup the group list UI.
     */
    private fun initializeRecyclerView() {
        //This Linear layout wraps content
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
    }

    /**
     * Use the private string TAG from this class as an identifier.
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): UserGroupsDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {

        private val TAG = UserGroupsDialog::class.java.simpleName
        private val ARG_GROUPS = "com.shareyourproxy.app.dialog.UserGroupsList"
        private val ARG_USER = "com.shareyourproxy.app.dialog.User"

        /**
         * Create a new instance of a [UserGroupsDialog].
         * @param groups logged in user groups
         * @param user   this is actually the contactId of the logged in user
         * @return A [UserGroupsDialog]
         */
        fun newInstance(groups: ArrayList<GroupToggle>, user: User): UserGroupsDialog {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_GROUPS, groups)
            bundle.putParcelable(ARG_USER, user)

            val dialog = UserGroupsDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
