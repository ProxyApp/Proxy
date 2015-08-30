package com.shareyourproxy.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.SearchClickedEvent;
import com.shareyourproxy.app.MainActivity;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.widget.Toast.makeText;
import static com.shareyourproxy.util.ViewUtils.getMenuIconDark;


/**
 * Search view to insert in the {@link MainActivity}.
 */
public class ContactSearchLayout extends FrameLayout {

    @Bind(R.id.widget_contactsearchlayout_textview)
    protected TextView searchTextView;
    @Bind(R.id.widget_contactsearchlayout_menu_icon)
    protected ImageView menuImageView;
    @BindDimen(R.dimen.common_margin_xxxtiny)
    protected int microMargin;
    @BindDimen(R.dimen.common_margin_xxtiny)
    protected int tinyMargin;
    @BindDimen(R.dimen.common_margin_tiny)
    protected int smallMargin;
    @BindDrawable(R.drawable.selector_contactsearchlayout)
    protected Drawable background;
    private DrawerLayout _drawerLayout;
    private RxBusDriver _rxBus;
    private View _view;

    public ContactSearchLayout(Context context, RxBusDriver rxBus, DrawerLayout drawerLayout) {
        super(context);
        initLayout(context, rxBus, drawerLayout);
    }

    @OnClick(R.id.widget_contactsearchlayout_menu_icon)
    public void onClickMenuIcon() {
        _drawerLayout.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.widget_contactsearchlayout_textview)
    public void onClickSearch() {
        _rxBus.post(new SearchClickedEvent());
    }

    @OnLongClick(R.id.widget_contactsearchlayout_menu_icon)
    public boolean onLongClick(View view) {
        makeText(
            view.getContext(), menuImageView.getContentDescription(), Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Initialize the background color and inflate the custom widget layout.
     *
     * @param context      Context object.
     * @param drawerLayout to open drawer on action
     */
    public void initLayout(Context context, RxBusDriver rxBus, DrawerLayout drawerLayout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
            .LAYOUT_INFLATER_SERVICE);
        _view = inflater.inflate(R.layout.widget_contactsearchlayout, this, true);
        ButterKnife.bind(this, _view);
        _drawerLayout = drawerLayout;
        _rxBus = rxBus;
        initialize(context, _view);
    }

    /**
     * Initialize this search view UI.
     *
     * @param context activity
     * @param view    rootView
     */
    public void initialize(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransitionName(context.getString(R.string.search_container));
        }
        setBackground(background);
        setOnClickListener(onClickBackground());
        MarginLayoutParams lp = new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT,
            MarginLayoutParams.MATCH_PARENT);
        lp.setMargins(tinyMargin, tinyMargin, tinyMargin, microMargin);
        setLayoutParams(lp);
        ViewCompat.setElevation(view, 10f);
        menuImageView.setImageDrawable(getMenuIconDark(context, R.raw.ic_menu));
    }

    /**
     * Background click listener.
     *
     * @return click listener
     */
    public OnClickListener onClickBackground() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                _rxBus.post(new SearchClickedEvent());
            }
        };
    }

    /**
     * Get Search TextView for transition animation.
     *
     * @return view
     */
    public TextView getSearchTextView() {
        return searchTextView;
    }

    /**
     * Get Search ImageView hamburger for transition animation.
     *
     * @return view
     */
    public ImageView getMenuImageView() {
        return menuImageView;
    }

    /**
     * Get background view for transition animation.
     *
     * @return view
     */
    public View getContainerView() {
        return _view;
    }

}


