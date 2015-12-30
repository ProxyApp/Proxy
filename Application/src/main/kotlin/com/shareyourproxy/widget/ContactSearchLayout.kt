package com.shareyourproxy.widget

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.support.v4.content.res.ResourcesCompat.getDrawable
import android.support.v4.view.GravityCompat.START
import android.support.v4.widget.DrawerLayout
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
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.event.SearchClickedEvent
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.getMenuIconDark


/**
 * Search view to insert in the [AggregateFeedActivity].
 */
class ContactSearchLayout : FrameLayout {

    //Search TextView for transition animation.
    val searchTextView: TextView by bindView(R.id.widget_contactsearchlayout_textview)
    //Search ImageView hamburger for transition animation.
    val menuImageView: ImageView by bindView(R.id.widget_contactsearchlayout_menu_icon)
    private val marginMicro: Int = resources.getDimensionPixelSize(R.dimen.common_margin_xxxtiny)
    private val marginTiny: Int = resources.getDimensionPixelSize(R.dimen.common_margin_xxtiny)
    private val drawableBackground: Drawable = getDrawable(resources, selector_contactsearchlayout, null)
    private val onClickMenuIcon: OnClickListener get() = OnClickListener { drawerLayout?.openDrawer(START) }
    private val onClickSearch: OnClickListener get() = OnClickListener { post(SearchClickedEvent()) }
    private val onLongClick: OnLongClickListener get() = OnLongClickListener {
        makeText(context, menuImageView.contentDescription, LENGTH_SHORT).show()
        false
    }
    private var drawerLayout: DrawerLayout? = null
    //background view for transition animation.
    var containerView: View? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, drawerLayout: DrawerLayout) : super(context) {
        initLayout(context, drawerLayout)
    }


    /**
     * Initialize the background color and inflate the custom widget layout.
     * @param context      Context object.
     * @param drawerLayout to open drawer on action
     */
    private fun initLayout(context: Context, drawerLayout: DrawerLayout) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        containerView = inflater.inflate(R.layout.widget_contactsearchlayout, this, true)
        this.drawerLayout = drawerLayout
        initialize(context)
    }

    /**
     * Initialize this search view UI.
     * @param context activity
     */
    private fun initialize(context: Context) {
        if (SDK_INT >= LOLLIPOP) {
            transitionName = context.getString(R.string.search_container)
        }
        //this view
        background = drawableBackground
        setOnClickListener(onClickSearch)
        //ViewGroup members
        menuImageView.setOnClickListener(onClickMenuIcon)
        menuImageView.setOnLongClickListener(onLongClick)
        searchTextView.setOnClickListener(onClickSearch)
        setLayoutParams()
        menuImageView.setImageDrawable(getMenuIconDark(context, R.raw.ic_menu))
    }

    private fun setLayoutParams() {
        val lp = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)
        lp.setMargins(marginTiny, marginTiny, marginTiny, marginMicro)
        layoutParams = lp
    }

}


