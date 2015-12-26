package com.shareyourproxy.app.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.command.SaveGroupContactsCommand
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.UserGroupsAdapter
import com.shareyourproxy.util.ObjectUtils

import org.solovyev.android.views.llm.LinearLayoutManager

import java.util.ArrayList

import butterknife.Bind
import butterknife.BindColor
import butterknife.ButterKnife

/**
 * This Dialog provides a toggle selection to add a User contactId to the logged in User's various saved groups.
 */
class UserGroupsDialog : BaseDialogFragment() {
    private val _negativeClicked = OnClickListener { dialogInterface, i -> dialogInterface.dismiss() }
    @Bind(R.id.dialog_user_groups_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @Bind(R.id.dialog_user_groups_message)
    internal var message: TextView
    // Color
    @BindColor(R.color.common_text)
    internal var colorText: Int = 0
    @BindColor(R.color.common_blue)
    internal var colorBlue: Int = 0
    private var _adapter: UserGroupsAdapter? = null
    private val _positiveClicked = OnClickListener {
        dispatchUpdatedUserGroups()
        RxGoogleAnalytics(activity).contactGroupButtonHit()
    }

    /**
     * Issue a save group contacts command.
     */
    private fun dispatchUpdatedUserGroups() {
        val user = userArg
        rxBus.post(SaveGroupContactsCommand(loggedInUser, _adapter!!.data,
                user))
    }

    /**
     * Get the logged in user

     * @return user
     */
    private val userArg: User
        get() = arguments.getParcelable<User>(ARG_USER)

    /**
     * get the groups bundled into this dialog fragment.

     * @return selected groups
     */
    private val checkedGroups: ArrayList<GroupToggle>
        get() = arguments.getParcelableArrayList<GroupToggle>(ARG_GROUPS)

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(R.layout.dialog_user_groups, null, false)
        ButterKnife.bind(this, view)
        val title = getString(R.string.dialog_edit_user_groups, userArg.first())
        val dialog = AlertDialog.Builder(activity,
                R.style.Widget_Proxy_App_Dialog).setTitle(title).setView(view).setPositiveButton(R.string.save, _positiveClicked).setNegativeButton(android.R.string.cancel, _negativeClicked).create()

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
        _adapter = UserGroupsAdapter.newInstance(recyclerView, checkedGroups)
        //This Linear layout wraps content
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = _adapter
        recyclerView.hasFixedSize()
    }

    /**
     * Use the private string TAG from this class as an identifier.

     * @param fragmentManager manager of fragments
     * *
     * @return this dialog
     */
    fun show(fragmentManager: FragmentManager): UserGroupsDialog {
        show(fragmentManager, TAG)
        return this
    }

    companion object {

        private val TAG = ObjectUtils.Companion.getSimpleName(UserGroupsDialog::class.java)
        private val ARG_GROUPS = "com.shareyourproxy.app.dialog.UserGroupsList"
        private val ARG_USER = "com.shareyourproxy.app.dialog.User"

        /**
         * Create a new instance of a [UserGroupsDialog].

         * @param groups logged in user groups
         * *
         * @param user   this is actually the contactId of the logged in user
         * *
         * @return A [UserGroupsDialog]
         */
        fun newInstance(
                groups: ArrayList<GroupToggle>, user: User): UserGroupsDialog {
            //Bundle arguments
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_GROUPS, groups)
            bundle.putParcelable(ARG_USER, user)
            //copy dialog instance
            val dialog = UserGroupsDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

}
/**
 * Constructor
 */
