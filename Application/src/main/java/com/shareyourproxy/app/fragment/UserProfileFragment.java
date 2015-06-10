package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddUserContactCommand;
import com.shareyourproxy.api.rx.command.DeleteUserContactCommand;
import com.shareyourproxy.api.rx.command.event.GroupContactsUpdatedEvent;
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent;
import com.shareyourproxy.api.rx.command.event.UserChannelAddedEvent;
import com.shareyourproxy.api.rx.command.event.UserChannelDeletedEvent;
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
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.app.adapter.ChannelGridAdapter.VIEW_TYPE_SECTION;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;
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
    @InjectView(R.id.fragment_user_profile_collapsing_toolbar)
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    @InjectView(R.id.fragment_user_profile_header_button)
    protected Button groupButton;
    private ChannelGridAdapter _adapter;
    private Target _target;
    private PaletteAsyncListener _paletteListener;
    private User _user;
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
        UserGroupsDialog.newInstance(_contactGroups, _user).show(getFragmentManager());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _user = activity.getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
        _isLoggedInUser = activity.getIntent().getExtras().getBoolean(ARG_USER_LOGGED_IN);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.inject(this, rootView);
        initialize();
        return rootView;
    }

    /**
     * Initialize this fragments views.
     */
    private void initialize() {
        initializeActionBar();
        initializeHeader();
        initializeRecyclerView();
        if (!_isLoggedInUser) {
            getGroupEditContacts();
        }
    }

    private void getGroupEditContacts() {
        //TODO: TRASH this n^2 bullshit
        ArrayList<Group> groups = getLoggedInUser().groups();
        _contactGroups.clear();
        if (groups != null) {
            for (Group group : groups) {
                ArrayList<Contact> contacts = group.contacts();
                if (contacts != null && contacts.size() > 0) {
                    //default to contact not in group
                    boolean hasContact = false;
                    // for every group's contacts
                    for (Contact contact : contacts) {
                        // if the selected contact is in the group
                        if (_user.id().value().equals(contact.id().value())){
                            hasContact = true;
                            break;
                        }
                    }
                    _contactGroups.add(GroupEditContact.create(group, hasContact));
                } else {
                    _contactGroups.add(GroupEditContact.create(group, false));
                }
            }
        }
    }

    /**
     * Initialize this view.
     */
    private void initializeActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(
            joinWithSpace(new String[]{ _user.first(), _user.last() }));
    }

    private void initializeHeader() {
        Picasso.with(getActivity()).load(_user.imageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(getBitmapTargetView());
        if (_isLoggedInUser) {
            groupButton.setVisibility(View.GONE);
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
                    Integer color = palette.getVibrantColor(res.getColor(R.color.common_blue));
                    collapsingToolbarLayout.setContentScrimColor(color);
                    collapsingToolbarLayout.setStatusBarScrimColor(color);
                    collapsingToolbarLayout.setBackgroundColor(color);
                }
            };
        }
        return _paletteListener;
    }

    /**
     * Initialize a recyclerView with User data.
     */
    private void initializeRecyclerView() {
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (VIEW_TYPE_SECTION == _adapter.getItemViewType(position))
                    ? manager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(manager);
        _adapter = ChannelGridAdapter.newInstance(_user, this);
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
        if (_subscriptions == null) {
            _subscriptions = new CompositeSubscription();
        }
        _subscriptions.add(bindFragment(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof UserChannelAddedEvent) {
                    addUserChannel(((UserChannelAddedEvent) event));
                } else if (event instanceof UserChannelDeletedEvent) {
                    deleteUserChannel(((UserChannelDeletedEvent) event));
                }
                else if (event instanceof GroupContactsUpdatedEvent){
                    checkUpdateUserContacts((GroupContactsUpdatedEvent) event);
                }
            }
        };
    }

    private void checkUpdateUserContacts(GroupContactsUpdatedEvent event) {
        if (event.inGroup) {
            getRxBus().post(new AddUserContactCommand(getLoggedInUser(), event.contact));
        } else {
            getRxBus().post(new DeleteUserContactCommand(getLoggedInUser(), event.contact));
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
}
