package com.shareyourproxy.app.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback;
import com.shareyourproxy.app.UserContactActivity;
import com.shareyourproxy.app.dialog.UserGroupsDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.OnClick;
import rx.functions.Action1;

import static android.view.View.VISIBLE;
import static com.shareyourproxy.Constants.ARG_LOGGEDIN_USER_ID;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.api.rx.RxQuery.queryContactGroups;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;

/**
 * Display a contacts profile and channels.
 */
public class ContactProfileFragment extends UserProfileFragment {
    @Bind(R.id.fragment_user_profile_header_button)
    Button groupButton;
    @BindDimen(R.dimen.fragment_userprofile_header_contact_background_size)
    int marginContactHeight;
    private ArrayList<GroupToggle> _toggleGroups = new ArrayList<>();

    /**
     * Empty Fragment Constructor.
     */
    public ContactProfileFragment() {
    }

    /**
     * Return new instance for parent {@link UserContactActivity}.
     *
     * @return layouts.fragment
     */
    public static ContactProfileFragment newInstance(User contact, String loggedInUserId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_USER_SELECTED_PROFILE, contact);
        bundle.putString(ARG_LOGGEDIN_USER_ID, loggedInUserId);
        ContactProfileFragment fragment = new ContactProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_user_profile_header_button)
    void onClickGroup() {
        UserGroupsDialog.newInstance(_toggleGroups, getContact()).show(getFragmentManager());
    }

    @Override
    protected void onCreateView(View rootView) {
        super.onCreateView(rootView);
        initialize();
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        setHeaderHeight();
        setToolbarTitle();
        initializeHeader();
        initializeGroupButton();
    }

    private void setHeaderHeight() {
        LayoutParams lp = collapsingToolbarLayout.getLayoutParams();
        lp.height = marginContactHeight;
        collapsingToolbarLayout.setLayoutParams(lp);
    }

    private void setToolbarTitle() {
        String title = getContact().fullName();
        buildToolbar(toolbar, title, null);
    }

    /**
     * Initialize the Header view data and state.
     */
    private void initializeGroupButton() {
        groupButton.setVisibility(VISIBLE);
        groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getMenuIcon(getActivity(), R.raw.ic_groups), null, null, null);
        updateGroupButtonText(getGroupEditContacts());
    }


    private List<Group> getGroupEditContacts() {
        _toggleGroups.clear();
        List<GroupToggle> list = queryContactGroups(
            getLoggedInUser(), getContact());
        _toggleGroups.addAll(list);
        ArrayList<Group> selectedGroupsList = new ArrayList<>(list.size());
        for (GroupToggle groupToggle : list) {
            if (groupToggle.isChecked()) {
                selectedGroupsList.add(groupToggle.getGroup());
            }
        }
        return selectedGroupsList;
    }

    @SuppressWarnings("unchecked")
    private void updateGroupButtonText(List<Group> list) {
        if (list != null) {
            int groupSize = list.size();
            if (groupSize == 0) {
                groupButton.setText(R.string.add_to_group);
                groupButton.setBackgroundResource(R.drawable.selector_button_blue);
            } else if (groupSize == 1) {
                groupButton.setText(list.get(0).label());
                groupButton.setBackgroundResource(R.drawable.selector_button_grey);
            } else if (groupSize > 1) {
                groupButton.setText(getString(R.string.in_blank_groups, groupSize));
                groupButton.setBackgroundResource(R.drawable.selector_button_grey);
            }
        } else {
            groupButton.setText(R.string.add_to_group);
            groupButton.setBackgroundResource(R.drawable.selector_button_blue);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getRxBus().toObservable().subscribe(onNextEvent());
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent((GroupContactsUpdatedEventCallback) event);
                }
            }
        };
    }

    private void groupContactsUpdatedEvent(GroupContactsUpdatedEventCallback event) {
        updateGroupButtonText(event.contactGroups);
    }

}
