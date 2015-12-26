package com.shareyourproxy.app.fragment

import android.content.Context
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.text.style.TextAppearanceSpan
import android.widget.ImageView
import android.widget.TextView

import com.shareyourproxy.R

import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Share base methods.
 */
open class BaseIntroductionFragment : BaseFragment() {
    companion object {
        /**
         * * Draw ImageView content, set text formatting and content.
         */
        internal fun drawSlide(
                context: Context, imageView: ImageView, textView: TextView, slideResource: Int, logoSize: Int,
                introTitle: String, introBody: String) {
            //Draw Slide
            imageView.setImageDrawable(svgToBitmapDrawable(context, slideResource, logoSize))

            //Create message content
            val sb = SpannableStringBuilder(introTitle).append(introBody)

            //Title headline text appearance
            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Headline_Inverse),
                    0, introTitle.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            //Title headline spacing
            sb.setSpan(lineHeightSpan, 0, introTitle.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            //Body subhead text appearance
            sb.setSpan(TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Subhead_Inverse),
                    introTitle.length, sb.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            textView.text = sb
        }

        private val lineHeightSpan: LineHeightSpan
            get() = LineHeightSpan { text, start, end, spanstartv, v, fm -> fm.descent += 50 }
    }
}
