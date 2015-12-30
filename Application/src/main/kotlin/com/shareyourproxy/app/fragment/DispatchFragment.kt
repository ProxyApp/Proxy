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
import com.shareyourproxy.util.ButterKnife.bindColorStateList
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Handle dispatching a [LoginActivity] or a [AggregateFeedActivity] base off the current user.
 */
class DispatchFragment() : BaseFragment() {
    private val textView: TextView by bindView(fragment_dispatch_image)
    private val progressBar: ProgressBar by bindView(fragment_dispatch_progress)
    private val colorWhite: ColorStateList  by bindColorStateList(android.R.color.white)
    private val logoSize: Int by bindDimen(R.dimen.common_svg_ultra)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dispatch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        drawLogo()
        tintProgressBar()
    }

    private fun tintProgressBar() {
        setTintMode(progressBar.indeterminateDrawable, SRC_ATOP)
        setTintList(progressBar.indeterminateDrawable, colorWhite)
    }

    /**
     * Set the Logo image.drawable on this activities [ImageView].
     */
    private fun drawLogo() {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, svgToBitmapDrawable(activity, ic_doge_channels, logoSize), null, null)
    }

}
