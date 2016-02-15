package com.shareyourproxy.widget.behavior

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewCompat.getTranslationY
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.View.VISIBLE
import android.view.animation.Interpolator
import kotlin.comparisons.compareValues

/**
 * Behavior for App bar layout.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal final class SnackbarMarginBehavior() : AppBarLayout.ScrollingViewBehavior() {
    private val animationInterpolator: Interpolator = FastOutSlowInInterpolator()
    private var viewTranslationY: Float = 0F

    @Suppress("UNUSED_PARAMETER")
    public constructor(context: Context, attrs: AttributeSet) : this() {
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View?, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child)
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    private fun updateFabTranslationForSnackbar(parent: CoordinatorLayout, view: View?) {
        if (view?.visibility != VISIBLE) return
        val targetTransY = getFabTranslationYForSnackbar(parent, view)
        // We're already at (or currently animating to) the target value, return...
        if (viewTranslationY == targetTransY) return

        viewTranslationY = targetTransY
        val currentTransY = getTranslationY(view)
        val dy = currentTransY - targetTransY
        val threshold: Float? = view?.height?.times(.667f)

        if (compareValues(Math.abs(dy), threshold) > 0) {
            // If the view will be travelling by more than 2/3 of it's height, let's animate it instead
            ViewCompat.animate(view).translationY(targetTransY).scaleX(1f).scaleY(1f).alpha(1f).setInterpolator(animationInterpolator).setListener(null)
        } else {
            // Make sure that any current animation is cancelled
            ViewCompat.animate(view).cancel()
            // Now update the translation Y
            ViewCompat.setTranslationY(view, targetTransY)
        }
    }

    private fun getFabTranslationYForSnackbar(parent: CoordinatorLayout, view: View?): Float {
        var minOffset = 0f
        val dependencies = parent.getDependencies(view)
        (0..dependencies.size).forEach {
            if (dependencies[it] is Snackbar.SnackbarLayout) {
                val translation = getTranslationY(dependencies[it]).minus(dependencies[it].height.toFloat())
                minOffset = Math.min(minOffset, translation)
            }
        }
        return minOffset
    }
}
