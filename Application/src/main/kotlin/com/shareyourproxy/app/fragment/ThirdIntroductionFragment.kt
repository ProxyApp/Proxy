package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R.dimen.common_svg_xxlarge
import com.shareyourproxy.R.id.fragment_introduction_third_imageview
import com.shareyourproxy.R.id.fragment_introduction_third_textview
import com.shareyourproxy.R.layout.fragment_introduction_third
import com.shareyourproxy.R.raw.ic_guide_activity_slide3
import com.shareyourproxy.R.string.slide_three_body
import com.shareyourproxy.R.string.slide_three_title
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView

/**
 * Third introduction slide content.
 */
class ThirdIntroductionFragment() : BaseIntroductionFragment() {
    private val logoSize: Int by bindDimen(common_svg_xxlarge)
    private val introTitle: String by bindString(slide_three_title)
    private val introBody: String by bindString(slide_three_body)
    private val imageView: ImageView by bindView(fragment_introduction_third_imageview)
    private val textView: TextView by bindView(fragment_introduction_third_textview)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(fragment_introduction_third, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        drawSlide(activity, imageView, textView, ic_guide_activity_slide3, logoSize, introTitle, introBody)
    }
}
