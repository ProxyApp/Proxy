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
    private final ViewGroup _viewGroup;
    // Cached ViewConfiguration and system-wide constant values
    private int _minFlingVelocity;
    private int _maxFlingVelocity;
    private long _animationTime;
    // Fixed properties
    private View _view;
    private DismissCallback _callback;
    // 1 and not 0 to prevent dividing by zero
    private int _viewWidth = 1;
    // Transient properties
    private float _downX;
    private float _downY;
    private boolean _swiping;
    private VelocityTracker _velocityTracker;
    private float _translationX;
    private int _childPosition;
    private boolean _dismissCalled = false;

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
        _minFlingVelocity = vc.getScaledMinimumFlingVelocity() * SIXTEEN;
        _maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        _animationTime = view.getContext().getResources().getInteger(
            android.R.integer.config_shortAnimTime);
        _view = view;
        _viewGroup = viewGroup;
        _callback = callback;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(_translationX, 0);
        if (_viewWidth < 2) {
            _viewWidth = _viewGroup.getWidth();
        }
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                if (_velocityTracker == null) {
                    break;
                }
                return actionMove(motionEvent);
            case MotionEvent.ACTION_UP:
                if (_velocityTracker == null) {
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
        _childPosition = getChildPosition(motionEvent);
        _downX = motionEvent.getRawX();
        _downY = motionEvent.getRawY();
        _velocityTracker = VelocityTracker.obtain();
        _velocityTracker.addMovement(motionEvent);
    }

    /**
     * MotionEvent MOVE.
     *
     * @param motionEvent the motion event
     * @return boolean false
     */
    private boolean actionMove(MotionEvent motionEvent) {
        _velocityTracker.addMovement(motionEvent);
        float deltaX = motionEvent.getRawX() - _downX;
        float deltaY = motionEvent.getRawY() - _downY;

        if (deltaX <= 0 && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
            _swiping = true;
            if (_view.getParent() != null) {
                _view.getParent().requestDisallowInterceptTouchEvent(true);
            }
            // Cancel listview's touch
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            _view.onTouchEvent(cancelEvent);
            cancelEvent.recycle();
        }
        if (_swiping && deltaX <= 0) {
            _translationX = deltaX;
            _view.setTranslationX(deltaX);
            // TODO: use an ease-out interpolator or such
            _view.setAlpha(Math.max(0f, Math.min(1f,
                1f - Math.abs(deltaX) / _viewWidth)));
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
        float deltaX = motionEvent.getRawX() - _downX;
        _velocityTracker.addMovement(motionEvent);
        _velocityTracker.computeCurrentVelocity(UNIT_SECONDS);
        float velocityX = _velocityTracker.getXVelocity();
        float absVelocityX = Math.abs(velocityX);
        float absVelocityY = Math.abs(_velocityTracker.getYVelocity());
        boolean dismiss = false;

        if ((int) Math.abs(deltaX) > _viewWidth / 2 && _swiping) {
            dismiss = true;
        } else if (_minFlingVelocity <= absVelocityX && absVelocityX <= _maxFlingVelocity
            && absVelocityY < absVelocityX && _swiping) {
            // dismiss only if flinging in the same direction as dragging
            dismiss = (velocityX < 0) == (deltaX < 0) && velocityX < 0;
        }
        if (dismiss) {
            // dismissSnackBarAnimation
            Timber.d("calling animation");
            _view.animate()
                .translationX(-_viewWidth)
                .alpha(0)
                .setDuration(_animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!_dismissCalled) {
                            _callback.onDismiss(_childPosition);
                            _dismissCalled = true;
                        }
                    }
                });
        } else if (_swiping) {
            actionCancel();
        }
        resetSwipeFields();
    }

    /**
     * MotionEvent CANCEL.
     */
    private void actionCancel() {
        _view.animate()
            .translationX(0)
            .alpha(1)
            .setDuration(_animationTime)
            .setListener(null);
        resetSwipeFields();
    }

    /**
     * reset Swipe fields after an action up or cancel.
     */
    private void resetSwipeFields() {
        if (_velocityTracker != null) {
            _velocityTracker.recycle();
        }
        _velocityTracker = null;
        _translationX = 0;
        _downX = 0;
        _downY = 0;
        _dismissCalled = false;
        _swiping = false;
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
        int childCount = _viewGroup.getChildCount();
        int[] listViewCoords = new int[2];
        _viewGroup.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        View child;
        for (int i = 0; i < childCount; i++) {
            child = _viewGroup.getChildAt(i);
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
