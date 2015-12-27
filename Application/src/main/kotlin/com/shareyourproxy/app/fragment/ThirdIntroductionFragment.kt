package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.shareyourproxy.R
import com.shareyourproxy.R.dimen.common_svg_xxlarge
import com.shareyourproxy.R.id.fragment_introduction_third_imageview
import com.shareyourproxy.R.id.fragment_introduction_third_textview
import com.shareyourproxy.R.layout.fragment_introduction_third
import com.shareyourproxy.R.string.slide_three_body
import com.shareyourproxy.R.string.slide_three_title

/**
 * Third introduction slide content.
 */
class ThirdIntroductionFragment : BaseIntroductionFragment() {
    internal var logoSize: Int =  resources.getDimensionPixelSize(common_svg_xxlarge)
    internal var introTitle: String = resources.getString(slide_three_title)
    internal var introBody: String = resources.getString(slide_three_body)
    private val imageView: ImageView by bindView(fragment_introduction_third_imageview)
    private val textView: TextView by bindView(fragment_introduction_third_textview)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(fragment_introduction_third, container, false)
        BaseIntroductionFragment.drawSlide(activity, imageView, textView, R.raw.ic_guide_activity_slide3, logoSize, introTitle, introBody)
        return rootView
    }

    companion object {
        fun newInstance(): ThirdIntroductionFragment {
            return ThirdIntroductionFragment()
        }
    }
}
