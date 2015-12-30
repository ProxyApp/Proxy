package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.R.id.fragment_introduction_second_imageview
import com.shareyourproxy.R.id.fragment_introduction_second_textview
import com.shareyourproxy.R.raw.ic_guide_activity_slide2
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindString
import com.shareyourproxy.util.ButterKnife.bindView

/**
 * Second introduction slide content.
 */
class SecondIntroductionFragment : BaseIntroductionFragment() {
    private val logoSize: Int by bindDimen(R.dimen.common_svg_xlarge)
    private val introTitle: String by bindString(R.string.slide_two_title)
    private val introBody: String by bindString(R.string.slide_two_body)
    private val imageView: ImageView by bindView(fragment_introduction_second_imageview)
    private val textView: TextView by bindView(fragment_introduction_second_textview)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_introduction_second, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        drawSlide(activity, imageView, textView, ic_guide_activity_slide2, logoSize, introTitle, introBody)
    }
}
