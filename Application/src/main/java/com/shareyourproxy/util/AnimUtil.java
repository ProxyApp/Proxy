/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.shareyourproxy.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/**
 * Animation Utility.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("unused")
public class AnimUtil {
    public static final int DEFAULT_DURATION = -1;
    public static final int NO_DELAY = 0;

    public static final Interpolator EASE_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);
    public static final Interpolator EASE_OUT = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator EASE_OUT_EASE_IN = new PathInterpolator(0.4f, 0, 0.2f, 1);

    /**
     * Constructor.
     */
    private AnimUtil() {
    }

    /**
     * Cross fade the entered views.
     *
     * @param fadeInView  view to fade in
     * @param fadeOutView view to fade out
     * @param duration    of animation
     */
    public static void crossFadeViews(View fadeInView, View fadeOutView, int duration) {
        fadeIn(fadeInView, duration);
        fadeOut(fadeOutView, duration);
    }

    /**
     * Fade out view.
     *
     * @param view     to fade out
     * @param duration of fade out animation
     */
    public static void fadeOut(View view, int duration) {
        fadeOut(view, duration, null);
    }

    /**
     * Fade out view.
     *
     * @param view       to fade out
     * @param durationMs of fade out animation
     * @param callback   animation callback
     */
    public static void fadeOut(
        final View view, int durationMs,
        final AnimationCallback callback) {
        view.setAlpha(1);
        final ViewPropertyAnimator animator = view.animate();
        animator.cancel();
        animator.alpha(0).withLayer().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.GONE);
                view.setAlpha(0);
                if (callback != null) {
                    callback.onAnimationCancel();
                }
            }
        });
        if (durationMs != DEFAULT_DURATION) {
            animator.setDuration(durationMs);
        }
        animator.start();
    }

    /**
     * Fade in view.
     *
     * @param view       to fade in
     * @param durationMs fade in duration
     */
    public static void fadeIn(View view, int durationMs) {
        fadeIn(view, durationMs, NO_DELAY, null);
    }

    /**
     * Fade In view.
     *
     * @param view       to fade in
     * @param durationMs fade in duration
     * @param delay      fade in animation delay
     * @param callback   animation callback
     */
    public static void fadeIn(
        final View view, int durationMs, int delay,
        final AnimationCallback callback) {
        view.setAlpha(0);
        final ViewPropertyAnimator animator = view.animate();
        animator.cancel();

        animator.setStartDelay(delay);
        animator.alpha(1).withLayer().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setAlpha(1);
                if (callback != null) {
                    callback.onAnimationCancel();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }
        });
        if (durationMs != DEFAULT_DURATION) {
            animator.setDuration(durationMs);
        }
        animator.start();
    }

    /**
     * Scales in the view from scale of 0 to actual dimensions.
     *
     * @param view         The view to scale.
     * @param durationMs   The duration of the scaling in milliseconds.
     * @param startDelayMs The delay to applying the scaling in milliseconds.
     */
    public static void scaleIn(final View view, int durationMs, int startDelayMs) {
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setScaleX(1);
                view.setScaleY(1);
            }
        };
        scaleInternal(view, 0 /* startScaleValue */, 1 /* endScaleValue */, durationMs,
            startDelayMs, listener, EASE_IN);
    }

    /**
     * Scales out the view from actual dimensions to 0.
     *
     * @param view       The view to scale.
     * @param durationMs The duration of the scaling in milliseconds.
     */
    public static void scaleOut(final View view, int durationMs) {
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.GONE);
                view.setScaleX(0);
                view.setScaleY(0);
            }
        };

        scaleInternal(view, 1 /* startScaleValue */, 0 /* endScaleValue */, durationMs,
            NO_DELAY, listener, EASE_OUT);
    }

    /**
     * Scale inserted view.
     *
     * @param view            to scale
     * @param startScaleValue start value
     * @param endScaleValue   end value
     * @param durationMs      duration in milliseconds
     * @param startDelay      delay from start
     * @param listener        animation callback
     * @param interpolator    view interpolator
     */
    private static void scaleInternal(
        final View view, int startScaleValue, int endScaleValue,
        int durationMs, int startDelay, AnimatorListenerAdapter listener,
        Interpolator interpolator) {
        view.setScaleX(startScaleValue);
        view.setScaleY(startScaleValue);

        final ViewPropertyAnimator animator = view.animate();
        animator.cancel();

        animator.setInterpolator(interpolator)
            .scaleX(endScaleValue)
            .scaleY(endScaleValue)
            .setListener(listener)
            .withLayer();

        if (durationMs != DEFAULT_DURATION) {
            animator.setDuration(durationMs);
        }
        animator.setStartDelay(startDelay);

        animator.start();
    }

    /**
     * Animates a view to the new specified dimensions.
     *
     * @param view      The view to change the dimensions of.
     * @param newWidth  The new width of the view.
     * @param newHeight The new height of the view.
     */
    public static void changeDimensions(final View view, final int newWidth, final int newHeight) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

        final int oldWidth = view.getWidth();
        final int oldHeight = view.getHeight();
        final int deltaWidth = newWidth - oldWidth;
        final int deltaHeight = newHeight - oldHeight;

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Float value = (Float) animator.getAnimatedValue();

                view.getLayoutParams().width = (int) (value * deltaWidth + oldWidth);
                view.getLayoutParams().height = (int) (value * deltaHeight + oldHeight);
                view.requestLayout();
            }
        });
        animator.start();
    }

    /**
     * Callback for animation state.
     */
    public static class AnimationCallback {

        /**
         * Animation Ended.
         */
        public void onAnimationEnd() {
        }

        /**
         * Animation Canceled.
         */
        public void onAnimationCancel() {
        }
    }
}
