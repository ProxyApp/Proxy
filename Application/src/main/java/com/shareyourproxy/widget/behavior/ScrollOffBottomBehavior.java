package com.shareyourproxy.widget.behavior;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import timber.log.Timber;

/**
 * Created by Evan on 10/21/15.
 */
@CoordinatorLayout.DefaultBehavior(ScrollOffBottomBehavior.class)
public class ScrollOffBottomBehavior extends FloatingActionButton.Behavior {

    private int _viewHeight;
    private ObjectAnimator _animator;

    public ScrollOffBottomBehavior(Context context, AttributeSet attrs) {
        super();
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
                (int) getFabTranslationYForSnackbar(coordinatorLayout,child);

            _animator = ObjectAnimator.ofFloat(child, "translationY", targetTranslation);
            _animator.start();
        }
    }

    @Override
    public boolean onStartNestedScroll(
        CoordinatorLayout coordinatorLayout, FloatingActionButton
        child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
            super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                nestedScrollAxes);
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionButton fab) {
        float minOffset = 0f;
        List dependencies = parent.getDependencies(fab);
        int i = 0;
        for(int z = dependencies.size(); i < z; ++i) {
            View view = (View)dependencies.get(i);
            if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset,
                    ViewCompat.getTranslationY(view) - (float)view.getHeight());
            }
        }
        Timber.e("Offset:%1$s",minOffset);
        return minOffset;
    }

}