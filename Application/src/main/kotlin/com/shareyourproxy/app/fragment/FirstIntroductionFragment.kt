package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.util.bindView

/**
 * First introduction slide content.
 */
class FirstIntroductionFragment : BaseIntroductionFragment() {
    internal var logoSize: Int = resources.getDimensionPixelSize(R.dimen.common_svg_xlarge)
    internal var introTitle: String = resources.getString(R.string.slide_one_title)
    internal var introBody: String = resources.getString(R.string.slide_one_body)
    private val imageView: ImageView by bindView(R.id.fragment_introduction_first_imageview)
    private val textView: TextView by bindView(R.id.fragment_introduction_first_textview)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_introduction_first, container, false)
        BaseIntroductionFragment.drawSlide(activity, imageView, textView, R.raw.ic_guide_activity_slide1, logoSize, introTitle, introBody)
        return rootView
    }

    companion object {

        fun newInstance(): FirstIntroductionFragment {
            return FirstIntroductionFragment()
        }
    }
}
