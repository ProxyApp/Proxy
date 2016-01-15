package com.shareyourproxy.widget.behavior

import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator

/**
 * Behavior for floating aciton buttons.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal final class ScrollOffBottomBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {
    private val animationInterpolator: Interpolator = FastOutSlowInInterpolator()
    private var fabTranslationY: Float = 0F
    private var viewHeight: Int = 0
    private var animator: ObjectAnimator? = null

    override fun onLayoutChild(parent: CoordinatorLayout, child: FloatingActionButton, layoutDirection: Int): Boolean {
        val lp = child.layoutParams as ViewGroup.MarginLayoutParams
        val margin = lp.bottomMargin
        viewHeight = child.height + margin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        if (animator == null || !animator!!.isRunning) {
            val totalScroll = (dyConsumed + dyUnconsumed)
            val targetTranslation = if (totalScroll > 0) viewHeight else getFabTranslationYForSnackbar(coordinatorLayout, child).toInt()
            animator = ObjectAnimator.ofFloat(child, "translationY", targetTranslation.toFloat())
            animator?.start()
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child)
        }
        return false
    }

    private fun updateFabTranslationForSnackbar(parent: CoordinatorLayout, fab: FloatingActionButton) {
        if (fab.visibility != View.VISIBLE) return
        val targetTransY = getFabTranslationYForSnackbar(parent, fab)
        // We're already at (or currently animating to) the target value, return...
        if (fabTranslationY == targetTransY) return
        fabTranslationY = targetTransY
        val currentTransY = ViewCompat.getTranslationY(fab)
        val dy = currentTransY - targetTransY
        // If the FAB will be travelling by more than 2/3 of it's height, let's animate it instead
        if (Math.abs(dy) > (fab.height * 0.667f)) {
            ViewCompat.animate(fab).translationY(targetTransY).scaleX(1f).scaleY(1f).alpha(1f).setInterpolator(animationInterpolator).setListener(null)
        } else {
            // Make sure that any current animation is cancelled
            ViewCompat.animate(fab).cancel()
            // Now update the translation Y
            ViewCompat.setTranslationY(fab, targetTransY)
        }
    }

    private fun getFabTranslationYForSnackbar(parent: CoordinatorLayout, view: View?): Float {
        var minOffset = 0f
        val dependencies = parent.getDependencies(view)
        if(!dependencies.isEmpty()) {
            (0..dependencies.size).forEach {
                if (dependencies[it] is Snackbar.SnackbarLayout) {
                    val translation = ViewCompat.getTranslationY(dependencies[it]).minus(dependencies[it].height.toFloat())
                    minOffset = Math.min(minOffset, translation)
                }
            }
        }
        return minOffset
    }

}