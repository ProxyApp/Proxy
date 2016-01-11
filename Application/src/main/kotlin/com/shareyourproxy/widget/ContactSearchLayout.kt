package com.shareyourproxy.widget

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import com.shareyourproxy.R
import com.shareyourproxy.R.drawable.selector_contactsearchlayout
import com.shareyourproxy.R.id.widget_contactsearchlayout_menu_icon
import com.shareyourproxy.R.id.widget_contactsearchlayout_textview
import com.shareyourproxy.R.layout.widget_contactsearchlayout
import com.shareyourproxy.R.string.search_container
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.event.OnMenuPressedEvent
import com.shareyourproxy.api.rx.event.SearchClickedEvent
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindDrawable
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils


/**
 * Search view to insert in the [AggregateFeedActivity].
 */
internal final class ContactSearchLayout : FrameLayout {
    private val marginMicro: Int by bindDimen(R.dimen.common_margin_xxxtiny)
    private val marginTiny: Int by bindDimen(R.dimen.common_margin_xxtiny)
    private val drawableBackground: Drawable by bindDrawable(selector_contactsearchlayout)
    private val onClickMenuIcon: OnClickListener by LazyVal { View.OnClickListener { post(OnMenuPressedEvent()) } }
    private val onClickSearch: OnClickListener by LazyVal { View.OnClickListener { post(SearchClickedEvent()) } }
    private val onLongClick: OnLongClickListener by LazyVal {
        View.OnLongClickListener {
            makeText(context, menuImageView.contentDescription, LENGTH_SHORT).show()
            false
        }
    }
    internal val searchTextView: TextView by bindView(widget_contactsearchlayout_textview)
    internal val menuImageView: ImageView by bindView(widget_contactsearchlayout_menu_icon)
    internal val containerView: View by LazyVal { this }

    constructor(context: Context) : super(context) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(widget_contactsearchlayout, this, true)
        initialize(context)
    }

    /**
     * Initialize this search view UI.
     * @param context activity
     */
    private fun initialize(context: Context) {
        if (SDK_INT >= LOLLIPOP) {
            transitionName = context.getString(search_container)
        }
        //this view
        background = drawableBackground
        setOnClickListener(onClickSearch)
        //ViewGroup members
        searchTextView.setOnClickListener(onClickSearch)
        menuImageView.setOnClickListener(onClickMenuIcon)
        menuImageView.setOnLongClickListener(onLongClick)
        menuImageView.setImageDrawable(ViewUtils.getMenuIconDark(context, R.raw.ic_menu))
        //layout params
        setLayoutParams()
    }

    private fun setLayoutParams() {
        val lp = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)
        lp.setMargins(marginTiny, marginTiny, marginTiny, marginMicro)
        layoutParams = lp
    }
}


