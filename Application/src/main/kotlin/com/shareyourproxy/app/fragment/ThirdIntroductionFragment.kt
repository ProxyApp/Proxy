package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.shareyourproxy.R

import butterknife.Bind
import butterknife.BindDimen
import butterknife.BindString
import butterknife.ButterKnife

/**
 * Third introduction slide content.
 */
class ThirdIntroductionFragment : BaseIntroductionFragment() {
    @BindDimen(R.dimen.common_svg_xxlarge)
    internal var logoSize: Int = 0
    @BindString(R.string.slide_three_title)
    internal var introTitle: String
    @BindString(R.string.slide_three_body)
    internal var introBody: String
    @Bind(R.id.fragment_introduction_third_imageview)
    internal var imageView: ImageView
    @Bind(R.id.fragment_introduction_third_textview)
    internal var textView: TextView

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_introduction_third, container, false)
        ButterKnife.bind(this, rootView)
        BaseIntroductionFragment.drawSlide(activity, imageView, textView, R.raw.ic_guide_activity_slide3, logoSize,
                introTitle, introBody)
        return rootView
    }

    companion object {

        fun newInstance(): ThirdIntroductionFragment {
            return ThirdIntroductionFragment()
        }
    }

}
/**
 * Default Constructor.
 */
