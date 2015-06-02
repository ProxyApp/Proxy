package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.event.ChannelAddedEvent;
import com.shareyourproxy.api.rx.event.ChannelSelectedEvent;
import com.shareyourproxy.api.rx.event.DeleteChannelEvent;
import com.shareyourproxy.app.adapter.ChannelGridRecyclerAdapter;
import com.shareyourproxy.app.dialog.EditChannelDialog;
import com.shareyourproxy.app.dialog.ErrorDialog;
import com.shareyourproxy.widget.BaseRecyclerView;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.ARG_USER_CREATED_CHANNEL;
import static com.shareyourproxy.Constants.ARG_USER_LOGGED_IN;
import static com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE;
import static com.shareyourproxy.api.rx.RxChannelSync.addChannel;
import static com.shareyourproxy.api.rx.RxChannelSync.deleteChannel;
import static com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener;
import static com.shareyourproxy.app.adapter.ChannelGridRecyclerAdapter.VIEW_TYPE_CONTENT;
import static com.shareyourproxy.app.adapter.ChannelGridRecyclerAdapter.VIEW_TYPE_SECTION;
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
    private ChannelGridRecyclerAdapter _adapter;
    private Target _target;
    private PaletteAsyncListener _paletteListener;
    private User _user;
    private CompositeSubscription _subscriptions;
    private Subscription _channelSyncSubscription;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _user = activity.getIntent().getExtras().getParcelable(ARG_USER_SELECTED_PROFILE);
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
                return _adapter.getItemViewType(position)
                    == VIEW_TYPE_SECTION ? manager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(manager);
        _adapter = ChannelGridRecyclerAdapter.newInstance(_user, this);
        recyclerView.setAdapter(_adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public final void onItemClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
        int viewType = _adapter.getItemViewType(position);

        if (viewType == VIEW_TYPE_SECTION) {
            Timber.v("view section clicked");
        } else if (viewType == VIEW_TYPE_CONTENT) {
            getRxBus().post(new ChannelSelectedEvent(channel));
        } else {
            Timber.e("Unknown ViewType Clicked");
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Channel channel = _adapter.getItemData(position);
        int viewType = _adapter.getItemViewType(position);

        if (viewType == VIEW_TYPE_SECTION) {
            String sectionName = channel.channelSection().getLabel();
            Toast.makeText(getActivity(), sectionName + " section", Toast.LENGTH_SHORT).show();
        } else if (viewType == VIEW_TYPE_CONTENT) {
            if (isLoggedInUser()) {
                EditChannelDialog.newInstance(channel).show(getFragmentManager());
            }
        } else {
            Timber.e("Unknown Viewtype Clicked");
        }
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
                if (event instanceof ChannelAddedEvent) {
                    addUserChannel(((ChannelAddedEvent) event).channel);
                } else if (event instanceof DeleteChannelEvent) {
                    deleteUserChannel(((DeleteChannelEvent) event).channel);
                }
            }
        };
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Channel channel = data.getExtras().getParcelable(ARG_USER_CREATED_CHANNEL);
            if (channel != null && _subscriptions == null) {
                _subscriptions = new CompositeSubscription();
                addUserChannel(channel);
            }
        }
    }

    private void addUserChannel(Channel channel) {
        _channelSyncSubscription = bindFragment(this,
            addChannel(getActivity(), getLoggedInUser(), channel))
            .subscribe(addChannelObserver());
        _subscriptions.add(_channelSyncSubscription);
    }

    private void deleteUserChannel(Channel channel) {
        _channelSyncSubscription = bindFragment(this,
            deleteChannel(getActivity(), getLoggedInUser(), channel))
            .subscribe(deleteChannelObserver());
        _subscriptions.add(_channelSyncSubscription);
    }

    public Observer<Pair<User, Channel>> addChannelObserver() {
        return new JustObserver<Pair<User, Channel>>() {
            @Override
            public void onNext(Pair<User, Channel> userInfo) {
                setLoggedInUser(userInfo.first);
                _adapter.addChannel(userInfo.second);
                _subscriptions.remove(_channelSyncSubscription);
            }

            @Override
            public void onError() {
                ErrorDialog.newInstance("Data Sync Error", "Error saving the channel")
                    .show(getFragmentManager());
                _subscriptions.remove(_channelSyncSubscription);
            }
        };
    }

    public Observer<Pair<User, Channel>> deleteChannelObserver() {
        return new JustObserver<Pair<User, Channel>>() {
            @Override
            public void onNext(Pair<User, Channel> userInfo) {
                setLoggedInUser(userInfo.first);
                _adapter.removeChannel(userInfo.second);
                _subscriptions.remove(_channelSyncSubscription);
            }

            @Override
            public void onError() {
                ErrorDialog.newInstance("Data Sync Error", "Error saving the channel")
                    .show(getFragmentManager());
                _subscriptions.remove(_channelSyncSubscription);
            }
        };
    }
}
