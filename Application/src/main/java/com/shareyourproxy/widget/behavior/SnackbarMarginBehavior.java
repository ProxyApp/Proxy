package com.shareyourproxy.widget.behavior;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.List;

import static android.support.v4.view.ViewCompat.getTranslationY;

/**
 * Created by Evan on 11/15/15.
 */
public class SnackbarMarginBehavior extends AppBarLayout.ScrollingViewBehavior {
    private final Interpolator _animationInterpolator;
    private float _viewTranslationY;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SnackbarMarginBehavior(Context context, AttributeSet attrs) {
        super();
        _animationInterpolator = new FastOutSlowInInterpolator();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child);
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }

    private void updateFabTranslationForSnackbar(
        CoordinatorLayout parent, View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }

        final float targetTransY = getFabTranslationYForSnackbar(parent, view);
        if (_viewTranslationY == targetTransY) {
            // We're already at (or currently animating to) the target value, return...
            return;
        }

        _viewTranslationY = targetTransY;
        final float currentTransY = getTranslationY(view);
        final float dy = currentTransY - targetTransY;

        if (Math.abs(dy) > (view.getHeight() * 0.667f)) {
            // If the view will be travelling by more than 2/3 of it's height, let's animate
            // it instead
            ViewCompat.animate(view)
                .translationY(targetTransY)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setInterpolator(_animationInterpolator)
                .setListener(null);
        } else {
            // Make sure that any current animation is cancelled
            ViewCompat.animate(view).cancel();
            // Now update the translation Y
            ViewCompat.setTranslationY(view, targetTransY);
        }
    }

    private float getFabTranslationYForSnackbar(
        CoordinatorLayout parent, View view) {
        float minOffset = 0f;
        List dependencies = parent.getDependencies(view);
        int i = 0;
        for (int z = dependencies.size(); i < z; ++i) {
            View childView = (View) dependencies.get(i);
            if (childView instanceof Snackbar.SnackbarLayout) {
                minOffset = Math.min(minOffset,
                    getTranslationY(childView) - (float) childView.getHeight());
            }
        }
        return minOffset;
    }
}
