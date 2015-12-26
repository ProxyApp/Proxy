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
 * Second introduction slide content.
 */
class SecondIntroductionFragment : BaseIntroductionFragment() {
    @BindDimen(R.dimen.common_svg_xlarge)
    internal var logoSize: Int = 0
    @BindString(R.string.slide_two_title)
    internal var introTitle: String
    @BindString(R.string.slide_two_body)
    internal var introBody: String
    @Bind(R.id.fragment_introduction_second_imageview)
    internal var imageView: ImageView
    @Bind(R.id.fragment_introduction_second_textview)
    internal var textView: TextView

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_introduction_second, container, false)
        ButterKnife.bind(this, rootView)
        BaseIntroductionFragment.drawSlide(activity, imageView, textView, R.raw.ic_guide_activity_slide2, logoSize,
                introTitle, introBody)
        return rootView
    }

    companion object {

        fun newInstance(): SecondIntroductionFragment {
            return SecondIntroductionFragment()
        }
    }

}
/**
 * Default Constructor.
 */
