package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.callback.GroupContactsUpdatedEvent;
import com.shareyourproxy.api.rx.command.callback.UserChannelAddedEvent;
import com.shareyourproxy.api.rx.command.callback.UserChannelDeletedEvent;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.ChannelGridAdapter;
import com.shareyourproxy.app.dialog.EditChannelDialog;
import com.shareyourproxy.app.dialog.UserGroupsDialog;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.shareyourproxy.Constants.ARG_USER_LOGGED_IN;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.api.domain.factory.ContactFactory.createModelContact;
import static com.shareyourproxy.api.rx.RxQuery.queryContactGroups;
import static com.shareyourproxy.api.rx.RxQuery.queryPermissionedChannels;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.app.adapter.ChannelGridAdapter.VIEW_TYPE_SECTION;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;
import static com.shareyourproxy.util.ViewUtils.getMenuIcon;
import static rx.android.app.AppObservable.bindFragment;

/**
 * Display a User or Contacts Profile.
 */
public class UserProfileFragment extends BaseFragment implements ItemClickListener {

    public static final int SPAN_COUNT = 4;
    @InjectView(R.id.fragment_user_profile_toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.fragment_user_profile_recyclerview)
    protected BaseRecyclerView recyclerView;
    @InjectView(R.id.fragment_user_profile_header_image)
    protected ImageView userImage;
    @InjectView(R.id.fragment_user_profile_header_button)
    protected Button groupButton;
    @InjectView(R.id.fragment_user_profile_empty_textview)
    protected TextView emptyTextView;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected FrameLayout userProfileBackground;
    private ChannelGridAdapter _adapter;
    private Target _target;
    private PaletteAsyncListener _paletteListener;
    private User _userContact;
    private CompositeSubscription _subscriptions;
    private boolean _isLoggedInUser;
    private ArrayList<GroupEditContact> _contactGroups = new ArrayList<>();


    /**
     * Constructor.
     */
    public UserProfileFragment() {
    }

    /**
     * Return new {@link UserProfileFragment} instance.
     *
     * @return fragment
     */
    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @OnClick(R.id.fragment_user_profile_header_button)
    protected void onClickGroup() {
        UserGroupsDialog.newInstance(_contactGroups, _userContact).show(getFragmentManager());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _userContact = activity.getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
        _isLoggedInUser = activity.getIntent().getExtras().getBoolean(ARG_USER_LOGGED_IN);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            rootView = inflater.inflate(R.layout.fragment_user_profile_v21, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        }
        ButterKnife.inject(this, rootView);
        initialize(rootView);
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize(View rootView) {
        initializeActionBar(rootView);
        initializeHeader();
        if (!_isLoggedInUser) {
            getGroupEditContacts();
            initializeRecyclerView(null);
            getSharedChannels();
        } else {
            initializeRecyclerView(getLoggedInUser().channels());
        }
    }

    private void getGroupEditContacts() {
        _contactGroups.clear();
        _contactGroups.addAll(queryContactGroups(
            getLoggedInUser(), createModelContact(_userContact)));
        updateGroupButtonText(queryContactGroups(
            _contactGroups, createModelContact(_userContact)));
    }

    /**
     * Initialize this view.
     *
     * @param rootView
     */
    private void initializeActionBar(View rootView) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title = joinWithSpace(new String[]{ _userContact.first(), _userContact.last() });
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setTitle(title);
            userProfileBackground = ButterKnife.findById(rootView, R.id
                .fragment_user_profile_header_background);
        } else {
            collapsingToolbarLayout = ButterKnife.findById(rootView, R.id
                .fragment_user_profile_collapsing_toolbar);
            collapsingToolbarLayout.setTitle(title);
            getSupportActionBar().setTitle("");
        }
    }

    private void initializeHeader() {
        Picasso.with(getActivity()).load(_userContact.imageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(getBitmapTargetView());
        if (_isLoggedInUser) {
            groupButton.setVisibility(View.GONE);
        } else {
            groupButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getMenuIcon(getActivity(), R.raw.ic_groups), null, null, null);
        }
    }

    /**
     * Strong Reference Bitmap Target.
     *
     * @return target
     */
    private Target getBitmapTargetView() {
        if (_target == null) {
            _target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    userImage.setImageBitmap(bitmap);
                    new Palette.Builder(bitmap).generate(getPaletteAsyncListener());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(errorDrawable.getIntrinsicWidth(),
                        errorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    userImage.setImageBitmap(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(placeHolderDrawable.getIntrinsicWidth(),
                        placeHolderDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    userImage.setImageBitmap(bitmap);
                }
            };
        }
        return _target;
    }

    /**
     * Async return when palette has been loaded.
     *
     * @return palette listener
     */
    private PaletteAsyncListener getPaletteAsyncListener() {
        if (_paletteListener == null) {
            _paletteListener = new PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    Resources res = getActivity().getResources();
                    Integer offColor = palette.getMutedColor(
                        res.getColor(R.color.common_blue));

                    Integer color = palette.getVibrantColor(offColor);
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
                        userProfileBackground.setBackgroundColor(color);
                    } else {
                        collapsingToolbarLayout.setContentScrimColor(color);
                        collapsingToolbarLayout.setStatusBarScrimColor(color);
                        collapsingToolbarLayout.setBackgroundColor(color);
                    }
                }
            };
        }
        return _paletteListener;
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView(ArrayList<Channel> channels) {
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (VIEW_TYPE_SECTION == _adapter.getItemViewType(position))
                    ? manager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(manager);
        _adapter = ChannelGridAdapter.newInstance(channels, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public final void onItemClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
//        int viewType = _adapter.getItemViewType(position);
//        if (viewType == VIEW_TYPE_SECTION) {
//            Timber.v("view section clicked");
//        } else if (viewType == VIEW_TYPE_CONTENT) {
        getRxBus().post(new SelectUserChannelEvent(channel));
//        } else {
//            Timber.e("Unknown ViewType Clicked");
//        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
//        int viewType = _adapter.getItemViewType(position);
//        if (viewType == VIEW_TYPE_SECTION) {
//            String sectionName = channel.channelSection().getLabel();
//            Toast.makeText(getActivity(), sectionName + " section", Toast.LENGTH_SHORT).show();
//        } else if (viewType == VIEW_TYPE_CONTENT) {
        if (isLoggedInUser()) {
            EditChannelDialog.newInstance(channel).show(getFragmentManager());
        }
//        } else {
//            Timber.e("Unknown Viewtype Clicked");
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCompositButton();
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
    }

    private void checkCompositButton() {
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserChannelAddedEvent) {
                    addUserChannel(((UserChannelAddedEvent) event));
                } else if (event instanceof UserChannelDeletedEvent) {
                    deleteUserChannel(((UserChannelDeletedEvent) event));
                } else if (event instanceof GroupContactsUpdatedEvent) {
                    groupContactsUpdatedEvent((GroupContactsUpdatedEvent) event);
                }
            }
        };
    }

    private void groupContactsUpdatedEvent(GroupContactsUpdatedEvent event) {
        updateGroupButtonText(event);
    }

    private void updateGroupButtonText(GroupContactsUpdatedEvent event) {
        int groupSize = event.contactGroups.size();
        if (groupSize == 0) {
            groupButton.setText(R.string.add_to_group);
        } else if (groupSize > 1) {
            groupButton.setText(getString(R.string.in_blank_groups, groupSize));
        } else {
            groupButton.setText(event.contactGroups.get(0).label());
        }
    }

    private boolean isLoggedInUser() {
        return getActivity().getIntent().getExtras().getBoolean(ARG_USER_LOGGED_IN);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void addUserChannel(UserChannelAddedEvent event) {
        _adapter.addChannel(event.channel);
    }

    private void deleteUserChannel(UserChannelDeletedEvent event) {
        _adapter.removeChannel(event.channel);
    }

    public void getSharedChannels() {
        checkCompositButton();
        _subscriptions.add(bindFragment(this, queryPermissionedChannels(
            getActivity(), getLoggedInUser().id().value(), _userContact.id().value()))
            .subscribe(permissionedObserver()));
    }

    private JustObserver<ArrayList<Channel>> permissionedObserver() {
        return new JustObserver<ArrayList<Channel>>() {


            @Override
            public void onError() {

            }

            @Override
            public void onNext(ArrayList<Channel> channels) {
                if (channels.size() == 0) {
                    emptyTextView.setText(getString(R.string.no_information_to_share,
                        _userContact.first()));
                    recyclerView.setEmptyView(emptyTextView);
                    _adapter.notifyDataSetChanged();
                } else {
                    _adapter.refreshChannels(channels);
                }
            }
        };
    }
}
