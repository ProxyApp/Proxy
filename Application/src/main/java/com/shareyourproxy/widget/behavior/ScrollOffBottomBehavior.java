package com.shareyourproxy.widget.behavior;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.util.List;

/**
 * Created by Evan on 10/21/15.
 */
public class ScrollOffBottomBehavior extends FloatingActionButton.Behavior {

    private final Interpolator _animationInterpolator;
    private int _viewHeight;
    private ObjectAnimator _animator;
    private float _fabTranslationY;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScrollOffBottomBehavior(Context context, AttributeSet attrs) {
        super();
        _animationInterpolator = new FastOutSlowInInterpolator();
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        int margin = lp.bottomMargin;
        _viewHeight = child.getHeight() + margin;
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public void onNestedScroll(
        CoordinatorLayout coordinatorLayout, FloatingActionButton child,
        View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed);

        if (_animator == null || !_animator.isRunning()) {
            int totalScroll = (dyConsumed + dyUnconsumed);
            int targetTranslation = totalScroll > 0 ? _viewHeight :
                (int) getFabTranslationYForSnackbar(coordinatorLayout, child);

            _animator = ObjectAnimator.ofFloat(child, "translationY", targetTranslation);
            _animator.start();
        }
    }

    @Override
    public boolean onStartNestedScroll(
        CoordinatorLayout coordinatorLayout, FloatingActionButton child,
        View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
            super.onStartNestedScroll(
                coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public boolean onDependentViewChanged(
        CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child);
        }
        return false;
    }

    private void updateFabTranslationForSnackbar(
        CoordinatorLayout parent, FloatingActionButton fab) {
        if (fab.getVisibility() != View.VISIBLE) {
            return;
        }

        final float targetTransY = getFabTranslationYForSnackbar(parent, fab);
        if (_fabTranslationY == targetTransY) {
            // We're already at (or currently animating to) the target value, return...
            return;
        }

        _fabTranslationY = targetTransY;
        final float currentTransY = ViewCompat.getTranslationY(fab);
        final float dy = currentTransY - targetTransY;

        if (Math.abs(dy) > (fab.getHeight() * 0.667f)) {
            // If the FAB will be travelling by more than 2/3 of it's height, let's animate
            // it instead
            ViewCompat.animate(fab)
                .translationY(targetTransY)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setInterpolator(_animationInterpolator)
                .setListener(null);
        } else {
            // Make sure that any current animation is cancelled
            ViewCompat.animate(fab).cancel();
            // Now update the translation Y
            ViewCompat.setTranslationY(fab, targetTransY);
        }
    }

    private float getFabTranslationYForSnackbar(
        CoordinatorLayout parent, FloatingActionButton fab) {
        float minOffset = 0f;
        List dependencies = parent.getDependencies(fab);
        int i = 0;
        for (int z = dependencies.size(); i < z; ++i) {
            View view = (View) dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout) {
                minOffset = Math.min(minOffset,
                    ViewCompat.getTranslationY(view) - (float) view.getHeight());
            }
        }
        return minOffset;
    }

}