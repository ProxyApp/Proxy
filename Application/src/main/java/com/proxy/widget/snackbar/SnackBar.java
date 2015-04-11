/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 William Mora
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.proxy.widget.snackbar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.proxy.R;


/**
 * This creates a custom SnackBar with at least a message context. To create a default SnackBar call
 * {SnackBar.newInstance(activityContext, "message").show()}. You may choose to add further content
 * with {@link #setActionText(CharSequence)}, {@link #setActionColorResource(int)}.
 */
public final class SnackBar extends SnackBarLayout {
    //views
    TextView mSnackBarMessageText;
    TextView mSnackBarActionText;
    //Objects and primitives
    private static final int TEN_PERCENT_ALPHA = 16;
    private static final int HALF_ALPHA = 128;
    private final Animation mAnimationSlideOut = getAnimationSlideOut();
    private final Animation mAnimationSlideIn = getAnimationSlideIn();
    private CharSequence mMessage;
    private CharSequence mActionText;
    private int mActionColor = getResources().getColor(R.color.common_text_inverse);
    private SnackBarDuration mSnackBarDuration = SnackBarDuration.LENGTH_LONG;
    private SnackListener mSnackListener;
    private boolean mIsDismissing = false;
    private boolean mActionClicked = false;
    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismissSnackBarAnimation();
        }
    };

    /**
     * SnackBar Events.
     */
    public interface SnackListener {
        /**
         * Called when a {@link SnackBar}'s action textview has been clicked.
         *
         * @param snackBar snacks
         */
        void onActionClicked(SnackBar snackBar);

        /**
         * Called when a {@link SnackBar} is fully shown.
         *
         * @param snackBar the {@link SnackBar} that's being shown
         */
        void onShown(SnackBar snackBar);

        /**
         * Called when a {@link SnackBar} had just been dismissed.
         *
         * @param snackBar the {@link SnackBar} that's being dismissed
         */
        void onDismissed(SnackBar snackBar);
    }

    /**
     * Get SnackBar duration.
     *
     * @return duration of visibility
     */
    private long getSnackBarDuration() {
        return mSnackBarDuration.getDuration();
    }

    /**
     * @return the animation resource used by this {@link SnackBar} instance to enter the view
     */
    @AnimRes
    private static int getInAnimationResource() {
        return R.anim.snackbar_animate_in;
    }

    /**
     * @return the animation resource used by this {@link SnackBar} instance to exit the view
     */
    @AnimRes
    private static int getAnimationOutResource() {
        return R.anim.snackbar_animate_out;
    }

    /**
     * Get the slide in animation.
     *
     * @return slide in animation
     */
    private Animation getAnimationSlideIn() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), getInAnimationResource());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mSnackListener != null) {
                    mSnackListener.onShown(SnackBar.this);
                    // we are showing the SnackBar for the first time,
                    // allow action clicks to take place
                    mActionClicked = false;
                }
                //post Dismiss Runnable that can be stopped later with the swipelistener
                startDismissTimer();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    /**
     * Get the slide out animation.
     *
     * @return slide out animation
     */
    private Animation getAnimationSlideOut() {
        Animation animation = AnimationUtils.loadAnimation(getContext(),
            getAnimationOutResource());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                removeSnackBarView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    /**
     * Make sure we pause dismissing the snackbar with an animation until a full click.
     */
    private final OnTouchListener mActionTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    pauseDismiss();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    startDismissTimer();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * Dismiss SnackBar after a click.
     */
    private final OnClickListener mActionClickListener = new OnClickListener() {
        public void onClick(View v) {
            // if the listener isn't null and the action button hasn't been clicked
            if (mSnackListener != null && !mActionClicked) {
                mSnackListener.onActionClicked(SnackBar.this);
                mActionClicked = true;
            }
            dismissSnackBarAnimation();
        }
    };

    /**
     * Swipe Dismiss Listener.
     */
    private final OnTouchListener mSwipeListener = new SwipeDismissTouchListener(this, null,
        new SwipeDismissTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(Object token) {
                return true;
            }

            @Override
            public void onDismiss(View view, Object token) {
                removeSnackBar();
            }

            @Override
            public void pauseTimer(boolean shouldPause) {
                if (shouldPause) {
                    pauseDismiss();
                } else {
                    startDismissTimer();
                }
            }
        });

    /**
     * Lint Required Constructor.
     *
     * @param context activity context
     */
    @SuppressWarnings("unused")
    private SnackBar(@NonNull Context context) {
        super(context);
    }

    /**
     * Constructor forces message creation in newInstance.
     *
     * @param context activity context
     * @param message message text
     */
    private SnackBar(@NonNull Context context, @NonNull CharSequence message) {
        super(context);
        mMessage = message;
    }

    /**
     * Create a new instance of a SnackBar with a message.
     *
     * @param context app context
     * @param message message text
     * @return snackbar
     */
    @SuppressWarnings("unused")
    public static SnackBar newInstance(@NonNull Context context, @NonNull CharSequence message) {
        return new SnackBar(context, message);
    }

    /**
     * Sets the action text to be displayed, if any. Note that if this is not set, the action button
     * will not be displayed
     *
     * @param actionText label
     * @return snackbar
     */
    @SuppressWarnings("unused")
    public SnackBar setActionText(@NonNull CharSequence actionText) {
        mActionText = actionText;
        return this;
    }

    /**
     * Sets the action text color. Note that if this is not set, the action button will be displayed
     * green.
     *
     * @param color action text color and hightlight
     * @return snackbar
     */
    @SuppressWarnings("unused")
    public SnackBar setActionColorResource(@ColorRes int color) {
        mActionColor = getResources().getColor(color);
        return this;
    }

    /**
     * Sets a SnackBar Event Listener to this SnackBar.
     *
     * @param listener listener
     * @return SnackBar
     */
    @SuppressWarnings("unused")
    public SnackBar setSnackListener(SnackListener listener) {
        mSnackListener = listener;
        return this;
    }

    /**
     * Sets the visible setDuration of this {@link SnackBar}.
     *
     * @param duration time
     * @return SnackBar
     */
    @SuppressWarnings("unused")
    public SnackBar setDuration(SnackBarDuration duration) {
        mSnackBarDuration = duration;
        return this;
    }

    /**
     * final "builder" call to actually show the SnackBar.
     *
     * @return the SnackBar shown
     */
    @SuppressWarnings("unused")
    public SnackBar show() {
        return SnackBarManager.show(this);
    }

    /**
     * Displays the {@link SnackBar} at the bottom of the {@link Activity} provided.
     *
     * @param targetActivity activity
     */
    protected void showNewSnackBar(@NonNull Activity targetActivity) {
        buildSnackBar(targetActivity);
        startAnimation(mAnimationSlideIn);
    }

    /**
     * Create a SnackBar and bring the view to the font of the parent activities ViewRoot.
     *
     * @param targetActivity the activity requesting this SnackBar
     */
    private void buildSnackBar(@NonNull Activity targetActivity) {
        // getViewRoot
        ViewGroup root = (ViewGroup) targetActivity.findViewById(android.R.id.content);
        // create a snackbar and get its layout parameters
        FrameLayout.LayoutParams params = initializeLayout(targetActivity);
        //if the navigation bar is showing account of the offset
        if (isNavigationBarHidden(root)) {
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                params.bottomMargin = resources.getDimensionPixelSize(resourceId);
            }
        }
        // add the view and bring the child to the front
        root.addView(this, params);
        bringToFront();

        // Prior to KITKAT child.bringToFront() should be followed by calls to requestLayout() and 
        // invalidate() on the view's parent to force the parent to redraw newInstance the new 
        // child 
        // ordering.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            root.requestLayout();
            root.invalidate();
        }
        startAnimation(mAnimationSlideIn);
    }

    /**
     * Check to see if the screens navigation bar is hidden.
     *
     * @param root parent view group newInstance navigation bar
     * @return boolean is navigation bar hidden
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isNavigationBarHidden(ViewGroup root) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }
        int viewFlags = root.getWindowSystemUiVisibility();
        return (viewFlags & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            == View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    }

    /**
     * Initialize a SnackBar and return its layout parameters. This also creates the message and
     * action text content of the SnackBar.
     *
     * @param activity view parent
     * @return frame layout parameters.
     */
    private FrameLayout.LayoutParams initializeLayout(@NonNull Activity activity) {
        createSnackBarContent(activity);
        return createSnackBarLayoutParams();
    }

    /**
     * Find out if the device is a phone or a tablet from resources and generate layout parameters.
     *
     * @return layout parameters
     */
    private FrameLayout.LayoutParams createSnackBarLayoutParams() {
        FrameLayout.LayoutParams params;
        Resources res = getResources();
        if (res.getBoolean(R.bool.common_is_device_phone)) {
            // Phone Layout Params
            params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        } else {
            // Tablet Layout Params
            int offset = res.getDimensionPixelOffset(R.dimen.snackbar_offset);
            params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            //Set south west offset
            params.leftMargin = offset;
            params.bottomMargin = offset;
        }
        params.gravity = Gravity.BOTTOM;
        return params;
    }

    /**
     * inflate and create a SnackBar LinearLayout.
     *
     * @param activity parent activity
     * @return SnackBar LinearLayout
     */
    private SnackBarLayout createSnackBarContent(@NonNull Activity activity) {
        Resources res = getResources();
        SnackBar layout = (SnackBar) LayoutInflater.from(activity)
            .inflate(R.layout.sackbar_layout, this, true);
        //Set Min/Max Width and Max Height
        layout.setMinimumWidth(res.getDimensionPixelSize(R.dimen.snackbar_min_width));
        layout.setMaxWidth(res.getDimensionPixelSize(R.dimen.snackbar_max_width));
        layout.setMaxHeight(res.getBoolean(R.bool.common_is_device_phone) ? res
            .getDimensionPixelSize(R.dimen.snackbar_two_line_height) : res
            .getDimensionPixelSize(R.dimen.snackbar_one_line_height));
        //Set Background color
        layout.setBackgroundResource(R.drawable.snackbar_background);
        mSnackBarMessageText = (TextView) layout.findViewById(R.id.snackbar_message);
        mSnackBarActionText = (TextView) layout.findViewById(R.id.snackbar_action_text);
        // style the message content. For now message text is only white.
        setTextStyleAttributes();
        setSwipeDismissListener();
        return layout;
    }

    /**
     * Set a SwipeDismissListener if the device is a tablet.
     */
    private void setSwipeDismissListener() {
        // The boolean resource below is controlled by the android resource system. If this
        // device is using the default values folder (phone), the snack bar will not be swipeable.
        // If the resource configuration uses valus-sw-600dp or values-large (tablet) the 
        // snackbar will be swipeable.
        if (getResources().getBoolean(R.bool.snackbar_is_swipeable)) {
            //Allow this SnackBar to be clickable and therefore swipeable
            setClickable(true);
            setOnTouchListener(mSwipeListener);
        }
    }

    /**
     * Set the message and action text content,color, and effect background.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setTextStyleAttributes() {
        Resources res = getResources();
        mSnackBarMessageText.setText(mMessage);
        // Max lines (Phone : Tablet)
        mSnackBarMessageText.setMaxLines(res.getBoolean(R.bool.common_is_device_phone) ? 2 : 1);
        mSnackBarMessageText.setTextColor(res.getColor(R.color.common_text_inverse));
        mSnackBarActionText.setText(mActionText);

        if (!TextUtils.isEmpty(mActionText)) {
            // Max lines (Phone : Tablet)
            mSnackBarActionText.setMaxLines(res.getBoolean(R.bool.common_is_device_phone) ? 2 : 1);
            mSnackBarActionText.setTextColor(mActionColor);
            mSnackBarActionText.setOnTouchListener(mActionTouchListener);
            mSnackBarActionText.setOnClickListener(mActionClickListener);

            // create a statelist drawable for kitkat and below. Else create a Ripple effect for 
            // Lollipop.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //Create a statelist drawable
                StateListDrawable stateListDrawable = new StateListDrawable();
                ColorDrawable colorDrawable = new ColorDrawable(mActionColor);
                ColorDrawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
                stateListDrawable.addState(new int[]{ android.R.attr.state_pressed },
                    colorDrawable);
                stateListDrawable.addState(new int[]{ -android.R.attr.state_pressed },
                    transparentDrawable);
                stateListDrawable.setAlpha(HALF_ALPHA);
                mSnackBarActionText.setBackground(stateListDrawable);
            } else {
                //Create a ripple drawable
                int[][] states = new int[][]{
                    new int[]{ -android.R.attr.state_pressed },
                    new int[]{ android.R.attr.state_pressed }
                };
                int[] colors = new int[]{
                    Color.TRANSPARENT,
                    mActionColor
                };
                ColorStateList colorStateList = new ColorStateList(states, colors);
                RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, null, null);
                rippleDrawable.setAlpha(TEN_PERCENT_ALPHA);
                mSnackBarActionText.setBackground(rippleDrawable);
            }
        } else {
            // if there is no action text, then hide the view
            mSnackBarActionText.setVisibility(GONE);
        }
    }

    /**
     * Start a dismissal timer.
     */
    private void startDismissTimer() {
        postDelayed(mDismissRunnable, getSnackBarDuration());
    }

    /**
     * Stop the dismissal timer.
     *
     * @return the runnable to stop
     */
    private Runnable stopDismissTimer() {
        return mDismissRunnable;
    }

    /**
     * Pause dismissing a SnackBar.
     */
    private void pauseDismiss() {
        removeCallbacks(stopDismissTimer());
    }

    /**
     * Dismiss newInstance with an animation.
     */
    public void dismissSnackBarAnimation() {
        // catch for multiple dismissals
        if (mIsDismissing) {
            return;
        }
        mIsDismissing = true;
        startAnimation(mAnimationSlideOut);
    }

    /**
     * Remove the SnackBar from view immediately.
     */
    public void removeSnackBar() {
        // catch for multiple dismissals
        if (mIsDismissing) {
            return;
        }
        mIsDismissing = true;
        removeSnackBarView();
    }

    /**
     * Clear the animation, remove the SnackBar view from the parent, call onDismissed in the Event
     * listener.
     */
    private void removeSnackBarView() {
        clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mSnackListener != null) {
            mSnackListener.onDismissed(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(stopDismissTimer());
    }
}
