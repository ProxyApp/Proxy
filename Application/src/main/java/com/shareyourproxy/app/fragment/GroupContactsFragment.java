package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxQuery;
import com.shareyourproxy.api.rx.command.eventcallback.GroupChannelsUpdatedEventCallback;
import com.shareyourproxy.api.rx.event.RecyclerViewDatasetChangedEvent;
import com.shareyourproxy.api.rx.event.UserSelectedEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import com.shareyourproxy.app.adapter.GroupContactsAdapter;
import com.shareyourproxy.app.adapter.UserContactsAdapter.UserViewHolder;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_SELECTED_GROUP;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;
import static com.shareyourproxy.util.ObjectUtils.capitalize;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Display the {@link User} contacts added to the selected {@link Group}.
 */
public class GroupContactsFragment extends BaseFragment implements ItemClickListener {
    private final RxQuery _rxQuery = RxQuery.INSTANCE;
    @Bind(R.id.fragment_contacts_group_toolbar)
    Toolbar toolbar;
    @Bind(R.id.fragment_contacts_group_recyclerview)
    BaseRecyclerView recyclerView;
    @Bind(R.id.fragment_contacts_group_empty_textview)
    TextView emptyTextView;
    @BindString(R.string.fragment_contact_group_empty_title)
    String emptyTextTitle;
    @BindString(R.string.fragment_contact_group_empty_message)
    String emptyTextMessage;
    @BindDimen(R.dimen.common_svg_null_screen_small)
    int marginNullScreen;
    @BindColor(android.R.color.white)
    int colorWhite;
    @BindDimen(R.dimen.common_svg_large)
    int marginSVGLarge;
    private GroupContactsAdapter _adapter;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public GroupContactsFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return GroupContactsFragment
     */
    public static GroupContactsFragment newInstance() {
        return new GroupContactsFragment();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_group_users, container, false);
        ButterKnife.bind(this, rootView);
        initialize();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getBusObserver()));
        _adapter.refreshData(_rxQuery.queryUserContacts(
            getActivity(), getGroupArg().contacts()).values());
    }

    public JustObserver<Object> getBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof UserSelectedEvent) {
                    onUserSelected((UserSelectedEvent) event);
                } else if (event instanceof GroupChannelsUpdatedEventCallback) {
                    channelsUpdated((GroupChannelsUpdatedEventCallback) event);
                } else if (event instanceof RecyclerViewDatasetChangedEvent) {
                    recyclerView.updateViewState(((RecyclerViewDatasetChangedEvent) event));
                }
            }
        };
    }

    /**
     * A Group has been edited in {@link EditGroupChannelsFragment}. Update this fragments intent data and title.
     *
     * @param event group data
     */
    private void channelsUpdated(GroupChannelsUpdatedEventCallback event) {
        getActivity().getIntent().putExtra(ARG_SELECTED_GROUP, event.getGroup());
        getSupportActionBar().setTitle(Companion.capitalize(getGroupArg().label()));
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * User selected from this groups contacts. Open that Users profile.
     *
     * @param event data
     */
    public void onUserSelected(UserSelectedEvent event) {
        launchUserProfileActivity(getActivity(), event.getUser(),
            getLoggedInUser().id(), event.getImageView(), event.getTextView());
    }

    /**
     * Get the group selected and bundled in this activities {@link IntentLauncher#launchEditGroupContactsActivity(Activity, Group)} call.
     *
     * @return selected group
     */
    private Group getGroupArg() {
        return (Group) getActivity().getIntent().getExtras().getParcelable(ARG_SELECTED_GROUP);
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeRecyclerView();
        buildToolbar(toolbar, Companion.capitalize(getGroupArg().label()), null);
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        initializeEmptyView();
        _adapter = GroupContactsAdapter.newInstance(recyclerView, this);

        recyclerView.setEmptyView(emptyTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(_adapter);
    }

    private void initializeEmptyView() {
        Context context = getContext();
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, getFishDrawable(), null, null);
        SpannableStringBuilder sb = new SpannableStringBuilder(emptyTextTitle).append("\n")
            .append(emptyTextMessage);

        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body2),
            0, emptyTextTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body),
            emptyTextTitle.length() + 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        emptyTextView.setText(sb);
    }

    /**
     * Parse a svg and return a null screen sized {@link ContentDescriptionDrawable} .
     *
     * @return Drawable with a contentDescription
     */
    private Drawable getFishDrawable() {
        return svgToBitmapDrawable(getActivity(), R.raw.ic_fish, marginNullScreen);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserViewHolder holder = (UserViewHolder) recyclerView.getChildViewHolder(view);
        getRxBus().post(new UserSelectedEvent(
            holder.userImage, holder.userName, _adapter.getItemData(position)));
    }

}
