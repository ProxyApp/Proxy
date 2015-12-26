package com.shareyourproxy.app.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.shareyourproxy.R
import com.shareyourproxy.app.AggregateFeedActivity

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.ButterKnife

import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.support.v4.graphics.drawable.DrawableCompat.setTintList
import android.support.v4.graphics.drawable.DrawableCompat.setTintMode
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Handle dispatching a [com.shareyourproxy.app.LoginActivity] or a [AggregateFeedActivity] base off the current user.
 */
class DispatchFragment : BaseFragment() {
    @BindDimen(R.dimen.common_svg_ultra)
    internal var logoSize: Int = 0
    @Bind(R.id.fragment_dispatch_image)
    internal var textView: TextView
    @Bind(R.id.fragment_dispatch_progress)
    internal var progressBar: ProgressBar
    @BindColor(android.R.color.white)
    internal var colorWhite: ColorStateList

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_dispatch, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    fun initialize() {
        drawLogo()
        tintProgressBar()
    }

    fun tintProgressBar() {
        setTintMode(progressBar.indeterminateDrawable, SRC_ATOP)
        setTintList(progressBar.indeterminateDrawable, colorWhite)
    }

    /**
     * Set the Logo image.drawable on this activities [ImageView].
     */
    private fun drawLogo() {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, svgToBitmapDrawable(activity,
                R.raw.ic_doge_channels, logoSize), null, null)
    }

    companion object {

        /**
         * Return new Fragment instance.

         * @return layouts.fragment
         */
        fun newInstance(): DispatchFragment {
            return DispatchFragment()
        }
    }
}
/**
 * Constructor.
 */
