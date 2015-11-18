package com.shareyourproxy.app.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LineHeightSpan;
import android.text.style.TextAppearanceSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;

import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Share base methods.
 */
public class BaseIntroductionFragment extends BaseFragment {
    /**
     * * Draw ImageView content, set text formatting and content.
     */
    static void drawSlide(
        Context context, ImageView imageView, TextView textView, int slideResource, int logoSize,
        String introTitle, String introBody) {
        //Draw Slide
        imageView.setImageDrawable(svgToBitmapDrawable(context, slideResource, logoSize));

        //Create message content
        SpannableStringBuilder sb = new SpannableStringBuilder(introTitle).append(introBody);

        //Title headline text appearance
        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Headline_Inverse),
            0, introTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        //Title headline spacing
        sb.setSpan(getLineHeightSpan(), 0, introTitle.length(),
            Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        //Body subhead text appearance
        sb.setSpan(new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Subhead_Inverse),
            introTitle.length(), sb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(sb);
    }

    private static LineHeightSpan getLineHeightSpan() {
        return new LineHeightSpan() {
            @Override
            public void chooseHeight(
                CharSequence text, int start, int end, int spanstartv, int v,
                Paint.FontMetricsInt fm) {
                fm.descent += 50;
            }
        };
    }
}
