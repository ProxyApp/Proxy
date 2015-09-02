package com.shareyourproxy.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.gson.UserTypeAdapter;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.MainActivity;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchLoginActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.util.ViewUtils.dpToPx;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Handle dispatching a {@link com.shareyourproxy.app.LoginActivity} or a
 * {@link MainActivity} base off the current user.
 */
public class DispatchFragment extends BaseFragment {
    @Bind(R.id.fragment_dispatch_image)
    ImageView imageView;
    private CompositeSubscription _subscriptions;

    /**
     * Constructor.
     */
    public DispatchFragment() {
    }

    /**
     * Return new Fragment instance.
     *
     * @return layouts.fragment
     */
    public static DispatchFragment newInstance() {
        return new DispatchFragment();
    }

    private Observable<User> loginObservable() {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                try {
                    User user = null;
                    String jsonUser = getSharedPrefrences().getString(Constants
                        .KEY_LOGGED_IN_USER, null);
                    if (jsonUser != null) {
                        try {
                            user = UserTypeAdapter.newInstance()
                                .fromJson(jsonUser);
                        } catch (IOException e) {
                            Timber.e(Log.getStackTraceString(e));
                        }
                    }
                    if (user == null) {
                        launchLoginActivity(getActivity());
                        getActivity().finish();
                    } else {
                        setLoggedInUser(user);
                        getRxBus().post(new SyncAllUsersCommand(user.id().value()));
                    }
                    subscriber.onNext(user);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }

        }).retry().subscribeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dispatch, container, false);
        ButterKnife.bind(this, rootView);
        drawLogo();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(getRxBusObserver()));
        _subscriptions.add(loginObservable().subscribe(loginObserver()));
    }

    public JustObserver<User> loginObserver() {
        return new JustObserver<User>() {
            @Override
            public void onError() {
            }

            @Override
            public void onNext(User event) {

            }
        };
    }

    public JustObserver<Object> getRxBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void onError() {
            }

            @Override
            public void onNext(Object event) {
                if (event instanceof SyncAllUsersSuccessEvent) {
                    login();
                } else if (event instanceof SyncAllUsersErrorEvent) {
                    login();
                }
            }
        };
    }

    private void login() {
        launchMainActivity(getActivity(),
            MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        getActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    /**
     * Set the Logo image.drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        ViewCompat.setLayerType(imageView, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setElevation(imageView, getElevation());
        imageView.setImageDrawable(svgToBitmapDrawable(getActivity(),
            R.raw.ic_proxy_logo, (int) getResourceDimension(getActivity())));
    }

    /**
     * Get a big icon dimension size.
     *
     * @param activity context
     * @return resource dimension
     */
    private float getResourceDimension(Activity activity) {
        return dpToPx(activity.getResources(), R.dimen.common_svg_ultra);
    }

    /**
     * Get the elevation resource for FAB.
     *
     * @return diemnsion of elevation
     */
    private float getElevation() {
        return dpToPx(getActivity().getResources(), R.dimen.common_fab_elevation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
