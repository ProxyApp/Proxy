package com.shareyourproxy.app.dialog

import android.R.string.cancel
import android.app.Dialog
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.FragmentManager
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
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindView
import org.solovyev.android.views.llm.LinearLayoutManager
import java.util.*

/**
 * This Dialog provides a toggle selection to add a User contactId to the logged in User's various saved groups.
 */
class UserGroupsDialog(private val groups: ArrayList<GroupToggle>, private val user: User) : BaseDialogFragment() {
    private val TAG = UserGroupsDialog::class.java.simpleName
    private val ARG_GROUPS = "com.shareyourproxy.app.dialog.UserGroupsList"
    private val ARG_USER = "com.shareyourproxy.app.dialog.User"
    private val recyclerView: BaseRecyclerView by bindView(R.id.dialog_user_groups_recyclerview)
    private val textView: TextView by bindView(R.id.dialog_user_groups_message)
    private val colorText: Int by bindColor(common_text)
    private val colorBlue: Int by bindColor(common_blue)
    private val parcelUser: User = arguments.getParcelable<User>(ARG_USER)
    private val parcelGroups: ArrayList<GroupToggle> = arguments.getParcelableArrayList<GroupToggle>(ARG_GROUPS)
    private var adapter: UserGroupsAdapter = UserGroupsAdapter(recyclerView, parcelGroups)
    private val negativeClicked = OnClickListener { dialogInterface, i -> dialogInterface.dismiss() }
    private val positiveClicked = OnClickListener { dialogInterface, i -> dispatchUpdatedUserGroups() }

    init {
        arguments.putParcelableArrayList(ARG_GROUPS, groups)
        arguments.putParcelable(ARG_USER, user)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity.layoutInflater.inflate(dialog_user_groups, null, false)
        val title = getString(dialog_edit_user_groups, parcelUser.first)
        val dialog = AlertDialog.Builder(activity, Widget_Proxy_App_Dialog)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(save, positiveClicked)
                .setNegativeButton(cancel, negativeClicked)
                .create()

        textView.text = getString(R.string.dialog_group_channel_message)
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
     * Issue a save group contacts command.
     */
    private fun dispatchUpdatedUserGroups() {
        RxGoogleAnalytics(activity).contactGroupButtonHit()
        post(SaveGroupContactsCommand(loggedInUser, adapter.data, parcelUser))
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
}
