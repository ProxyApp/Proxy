package com.shareyourproxy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.NotificationCardActionEvent;
import com.shareyourproxy.api.rx.event.NotificationCardDismissEvent;
import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter;
import com.shareyourproxy.app.adapter.BaseViewHolder;
import com.shareyourproxy.app.adapter.NotificationRecyclerAdapter.HeaderViewHolder;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.v4.content.ContextCompat.getColor;
import static com.shareyourproxy.Constants.KEY_DISMISSED_CUSTOM_URL;
import static com.shareyourproxy.Constants.KEY_DISMISSED_INVITE_FRIENDS;
import static com.shareyourproxy.Constants.KEY_DISMISSED_MAIN_GROUP;
import static com.shareyourproxy.Constants.KEY_DISMISSED_PUBLIC_GROUP;
import static com.shareyourproxy.Constants.KEY_DISMISSED_SAFE_INFO;
import static com.shareyourproxy.Constants.KEY_DISMISSED_SHARE_PROFILE;
import static com.shareyourproxy.Constants.KEY_DISMISSED_WHOOPS;
import static com.shareyourproxy.R.styleable.DismissibleNotificationCard_notification;
import static com.shareyourproxy.R.styleable.DismissibleNotificationCard_showAction;
import static com.shareyourproxy.R.styleable.DismissibleNotificationCard_showDismiss;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;
import static com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.get;

/**
 * Dismissable Notification card for recycler view headers.
 */
public class DismissibleNotificationCard extends FrameLayout {

    @Bind(R.id.widget_notification_container)
    RelativeLayout container;
    @Bind(R.id.widget_notification_content_title)
    TextView title;
    @Bind(R.id.widget_notification_content_message)
    TextView message;
    @Bind(R.id.widget_notification_imageview)
    ImageView imageView;
    @Bind(R.id.widget_notification_dismiss_text)
    TextView dismissTextView;
    @Bind(R.id.widget_notification_action_text)
    TextView actionTextView;
    @BindDimen(R.dimen.common_margin_huge)
    int dimenPadding;
    @BindDimen(R.dimen.common_svg_null_screen_mini)
    int dimenSvgNullSmall;
    private NotificationCard _notificationCard = NotificationCard.WHOOPS;
    private boolean _showDismiss = false;
    private boolean _showAction = false;
    private RxBusDriver _rxBus = RxBusDriver.INSTANCE;
    private BaseViewHolder _holder;
    private BaseRecyclerViewAdapter _adapter;

    public DismissibleNotificationCard(Context context) {
        super(context);
        initializeView(context);
        initializeCardView(context);
    }

    public DismissibleNotificationCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
        initializeAttributeValues(context, attrs);
        initializeCardView(context);
    }

    public DismissibleNotificationCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
        initializeAttributeValues(context, attrs);
        initializeCardView(context);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.widget_notification_dismiss_text)
    public void onClickDismiss() {
        setVisibility(View.GONE);
        switch (_notificationCard) {
            case WHOOPS:
                //nada
                break;
            case SAFE_INFO:
            case SHARE_PROFILE:
            case INVITE_FRIENDS:
            case CUSTOM_URL:
            case PUBLIC_GROUPS:
            case MAIN_GROUPS:
                _rxBus.post(new NotificationCardDismissEvent(_adapter, _holder, _notificationCard,
                    isHeaderOrFooter()));
                break;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.widget_notification_action_text)
    public void onClickAction() {
        switch (_notificationCard) {
            case WHOOPS:
                //nada
                break;
            case SAFE_INFO:
            case SHARE_PROFILE:
            case INVITE_FRIENDS:
            case CUSTOM_URL:
            case PUBLIC_GROUPS:
            case MAIN_GROUPS:
                _rxBus.post(new NotificationCardActionEvent(_adapter, _holder, _notificationCard,
                    isHeaderOrFooter()));
                break;
        }
    }

    private boolean isHeaderOrFooter() {
        return _holder instanceof HeaderViewHolder;
    }

    public void createNotificationCard(
        BaseRecyclerViewAdapter adapter,
        BaseViewHolder holder, NotificationCard notificationCard,
        boolean showDismiss, boolean showAction) {
        _adapter = adapter;
        _holder = holder;
        _notificationCard = notificationCard;
        _showDismiss = showDismiss;
        _showAction = showAction;
        initializeCardView(holder.view.getContext());
        invalidate();
    }

    private void initializeAttributeValues(Context context, AttributeSet attrs) {
        // Initialize view params
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.DismissibleNotificationCard, 0, 0);
        try {
            _showDismiss = a.getBoolean(DismissibleNotificationCard_showDismiss, false);
            _showAction = a.getBoolean(DismissibleNotificationCard_showAction, false);
            _notificationCard = get(a.getInteger(DismissibleNotificationCard_notification, 0));
        } finally {
            a.recycle();
        }
    }

    private void initializeView(Context context) {
        View view = inflate(context, R.layout.notification_card, this);
        ButterKnife.bind(this, view);
    }

    private void initializeCardView(Context context) {
        initializeContent(context);
        dismissTextView.setVisibility(_showDismiss ? View.VISIBLE : View.GONE);
        actionTextView.setVisibility(_showAction ? setVisible(context) : View.GONE);
        container.setBackgroundColor(getColor(context, _notificationCard.getColorRes()));
    }

    /**
     * Set the message text if we're going to show this view
     */
    public int setVisible(Context context) {
        actionTextView.setText(context.getString(_notificationCard.getActionRes()));
        return View.VISIBLE;
    }

    private void initializeContent(Context context) {
        String title = context.getString(_notificationCard.getTitleRes());
        String message = context.getString(_notificationCard.getMessageRes());

        this.title.setText(title);
        this.message.setText(message);

        // Set custom drawable
        imageView.setImageDrawable(svgToBitmapDrawable
            (context, _notificationCard.getIconRes(), dimenSvgNullSmall));
    }

    public enum NotificationCard {
        WHOOPS(0, KEY_DISMISSED_WHOOPS, R.string.notification_safe_info_title,
            R.string.notification_safe_info_message, R.raw.ic_alien,
            R.color.common_deep_purple, R.string.ok),

        SAFE_INFO(1, KEY_DISMISSED_SAFE_INFO, R.string.notification_safe_info_title,
            R.string.notification_safe_info_message, R.raw.ic_chameleon_framed,
            R.color.common_proxy_purple, R.string.ok),

        SHARE_PROFILE(2, KEY_DISMISSED_SHARE_PROFILE, R.string.notification_share_profile_title,
            R.string.notification_share_profile_message, R.raw.ic_carroll_share,
            R.color.common_blue, R.string.ok),

        CUSTOM_URL(3, KEY_DISMISSED_CUSTOM_URL, R.string.notification_custom_url_title,
            R.string.notification_custom_url_message, R.raw.ic_sexbot_custom, R.color
            .common_blue, R.string.ok),

        INVITE_FRIENDS(4, KEY_DISMISSED_INVITE_FRIENDS, R.string.notification_invite_friends_title,
            R.string.notification_invite_friends_message, R.raw.ic_jamal,
            R.color.common_light_blue, R.string.send_invite),

        PUBLIC_GROUPS(5, KEY_DISMISSED_PUBLIC_GROUP, R.string.notification_public_groups_title,
            R.string.notification_public_groups_message, R.raw.ic_chameleon_framed,
            R.color.common_proxy_purple, R.string.ok),

        MAIN_GROUPS(6, KEY_DISMISSED_MAIN_GROUP, R.string.notification_main_groups_title,
            R.string.notification_main_groups_message, R.raw.ic_owl,
            R.color.common_proxy_orange, R.string.ok);

        private int value;
        private String key;
        private int titleRes;
        private int messageRes;
        private int iconRes;
        private int colorRes;
        private int actionRes;

        NotificationCard(
            int value, String key, int titleRes, int messageRes, int iconRes, int colorRes,
            int actionRes) {
            this.value = value;
            this.key = key;
            this.titleRes = titleRes;
            this.messageRes = messageRes;
            this.iconRes = iconRes;
            this.colorRes = colorRes;
            this.actionRes = actionRes;
        }

        public static NotificationCard get(int intValue) {
            for (NotificationCard notificationCard : NotificationCard.values()) {
                if (notificationCard.value == intValue) {
                    return notificationCard;
                }
            }
            return NotificationCard.WHOOPS;
        }

        public int getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }

        public int getTitleRes() {
            return titleRes;
        }

        public int getMessageRes() {
            return messageRes;
        }

        public int getIconRes() {
            return iconRes;
        }

        public int getColorRes() {
            return colorRes;
        }

        public int getActionRes() {
            return actionRes;
        }

    }
}
