/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proxy.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import timber.log.Timber;


/**
 * A {@link View.OnTouchListener} that makes any {@link View} dismissible when the user swipes
 * (drags her finger) horizontally across the view.
 *
 * @author Roman Nurik
 */
public class SwipeDeleteAnimationListener implements View.OnTouchListener {
    public static final int UNIT_SECONDS = 1000;
    public static final int SIXTEEN = 16;
    private final ViewGroup mViewGroup;
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;
    // Fixed properties
    private View mView;
    private DismissCallback mCallback;
    // 1 and not 0 to prevent dividing by zero
    private int mViewWidth = 1;
    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private float mTranslationX;
    private int mChildPosition;
    private boolean mDismissCalled = false;

    /**
     * Constructs a new swipe to delete touch listener. This class makes a call back when the
     * entered view has been fully translated and swiped off screen or "dismissed".
     *
     * @param viewGroup
     * @param view      The dismissible view
     * @param callback  The callback to trigger when the user has swiped to dismiss.
     * @param viewGroup ViewGroup Parent
     */
    public SwipeDeleteAnimationListener(
        ViewGroup viewGroup, View view, DismissCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * SIXTEEN;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = view.getContext().getResources().getInteger(
            android.R.integer.config_shortAnimTime);
        mView = view;
        mViewGroup = viewGroup;
        mCallback = callback;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, 0);
        if (mViewWidth < 2) {
            mViewWidth = mViewGroup.getWidth();
        }
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null) {
                    break;
                }
                return actionMove(motionEvent);
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker == null) {
                    break;
                }
                actionUp(motionEvent);
                break;
            case MotionEvent.ACTION_CANCEL:
                actionCancel();
                break;
            default:
                Timber.d("Default Case called in method onTouch()");
                break;
        }
        return false;
    }

    /**
     * MotionEvent Down.
     *
     * @param motionEvent motion event
     */
    private void actionDown(MotionEvent motionEvent) {
        mChildPosition = getChildPosition(motionEvent);
        mDownX = motionEvent.getRawX();
        mDownY = motionEvent.getRawY();
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionEvent);
    }

    /**
     * MotionEvent MOVE.
     *
     * @param motionEvent the motion event
     * @return boolean false
     */
    private boolean actionMove(MotionEvent motionEvent) {
        mVelocityTracker.addMovement(motionEvent);
        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;

        if (deltaX <= 0 && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
            mSwiping = true;
            if (mView.getParent() != null) {
                mView.getParent().requestDisallowInterceptTouchEvent(true);
            }
            // Cancel listview's touch
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            mView.onTouchEvent(cancelEvent);
            cancelEvent.recycle();
        }
        if (mSwiping && deltaX <= 0) {
            mTranslationX = deltaX;
            mView.setTranslationX(deltaX);
            // TODO: use an ease-out interpolator or such
            mView.setAlpha(Math.max(0f, Math.min(1f,
                1f - Math.abs(deltaX) / mViewWidth)));
            return true;
        }
        return false;
    }

    /**
     * MotionEvent ACTION_UP.
     *
     * @param motionEvent motionevent
     */
    private void actionUp(MotionEvent motionEvent) {
        float deltaX = motionEvent.getRawX() - mDownX;
        mVelocityTracker.addMovement(motionEvent);
        mVelocityTracker.computeCurrentVelocity(UNIT_SECONDS);
        float velocityX = mVelocityTracker.getXVelocity();
        float absVelocityX = Math.abs(velocityX);
        float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
        boolean dismiss = false;

        if ((int) Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
            dismiss = true;
        } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
            && absVelocityY < absVelocityX && mSwiping) {
            // dismiss only if flinging in the same direction as dragging
            dismiss = (velocityX < 0) == (deltaX < 0) && velocityX < 0;
        }
        if (dismiss) {
            // dismissSnackBarAnimation
            Timber.d("calling animation");
            mView.animate()
                .translationX(-mViewWidth)
                .alpha(0)
                .setDuration(mAnimationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!mDismissCalled) {
                            mCallback.onDismiss(mChildPosition);
                            mDismissCalled = true;
                        }
                    }
                });
        } else if (mSwiping) {
            actionCancel();
        }
        resetSwipeFields();
    }

    /**
     * MotionEvent CANCEL.
     */
    private void actionCancel() {
        mView.animate()
            .translationX(0)
            .alpha(1)
            .setDuration(mAnimationTime)
            .setListener(null);
        resetSwipeFields();
    }

    /**
     * reset Swipe fields after an action up or cancel.
     */
    private void resetSwipeFields() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = null;
        mTranslationX = 0;
        mDownX = 0;
        mDownY = 0;
        mDismissCalled = false;
        mSwiping = false;
    }

    /**
     * Get the position of the child being swiped in this touch listener.
     *
     * @param motionEvent touch motion event
     * @return the child's position in the list or null
     */
    private Integer getChildPosition(MotionEvent motionEvent) {
        // Find the child view that was touched (perform a hit test)
        Rect rect = new Rect();
        int childCount = mViewGroup.getChildCount();
        int[] listViewCoords = new int[2];
        mViewGroup.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        View child;
        for (int i = 0; i < childCount; i++) {
            child = mViewGroup.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return null;
    }

    /**
     * The callback interface used by {@link SwipeDeleteAnimationListener} to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    public interface DismissCallback {
        /**
         * Called when the user has indicated they she would like to dismissSnackBarAnimation the
         * view.
         *
         * @param position position of child view
         */
        void onDismiss(final int position);
    }
}
