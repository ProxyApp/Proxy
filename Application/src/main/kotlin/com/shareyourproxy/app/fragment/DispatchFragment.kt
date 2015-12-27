package com.shareyourproxy.app.fragment

import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat.setTintList
import android.support.v4.graphics.drawable.DrawableCompat.setTintMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.R.id.fragment_dispatch_image
import com.shareyourproxy.R.id.fragment_dispatch_progress
import com.shareyourproxy.R.raw.ic_doge_channels
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView

/**
 * Handle dispatching a [com.shareyourproxy.app.LoginActivity] or a [AggregateFeedActivity] base off the current user.
 */
class DispatchFragment : BaseFragment() {
    internal var logoSize: Int =  resources.getDimensionPixelSize(R.dimen.common_svg_ultra)
    private val textView: TextView by bindView(fragment_dispatch_image)
    private val progressBar: ProgressBar by bindView(fragment_dispatch_progress)
    internal var colorWhite: ColorStateList = resources.getColorStateList(android.R.color.white, null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_dispatch, container, false)
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
        textView.setCompoundDrawablesWithIntrinsicBounds(null, svgToBitmapDrawable(activity, ic_doge_channels, logoSize), null, null)
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
