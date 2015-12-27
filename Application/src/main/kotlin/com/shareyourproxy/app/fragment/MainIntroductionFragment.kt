package com.shareyourproxy.app.fragment

import android.R.color.black
import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat.getColor
import android.support.v4.view.ViewCompat.setAlpha
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_proxy_zoidberg
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_chevron_right
import com.shareyourproxy.app.fragment.AggregateFeedFragment.Companion.ARG_SELECT_PROFILE_TAB
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindView
import com.viewpagerindicator.CirclePageIndicator
import org.jetbrains.anko.onClick
import java.util.*

/**
 * The main introduction fragment.
 */
class MainIntroductionFragment : BaseFragment() {
    private val viewPager: ViewPager by bindView(fragment_introduction_main_viewpager)
    private val pageIndicator: CirclePageIndicator by bindView(fragment_introduction_main_pageindicator)
    private val floatingActionButton: FloatingActionButton by bindView(fragment_introduction_main_fab)
    internal var colorTransparent: ColorStateList = resources.getColorStateList(black, null)
    internal var colorSelected: ColorStateList = resources.getColorStateList(common_proxy_zoidberg, null)
    internal var colorWhite: Int = getColor(context, android.R.color.white)
    internal var marginSVGLarge: Int = resources.getDimensionPixelSize(R.dimen.common_svg_large)
    private var selectedPage = 0
    private val fragmentArray = Arrays.asList<BaseFragment>(FirstIntroductionFragment.newInstance(), SecondIntroductionFragment.newInstance(), ThirdIntroductionFragment.newInstance())
    private var adapter: BaseFragment.BasePagerAdapter = BaseFragment.BasePagerAdapter.newInstance(fragmentArray, childFragmentManager)

    private val onClickFab: View.OnClickListener = View.OnClickListener {
        if (selectedPage == (adapter.count - 1)) {
            launchMainActivity(activity, ARG_SELECT_PROFILE_TAB, false, null)
            sharedPreferences.edit().putBoolean(KEY_PLAY_INTRODUCTION, false).commit()
            activity.finish()
        } else {
            viewPager.setCurrentItem(++selectedPage, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_introduction_main, container, false)
        initialize()
        return rootView
    }

    private fun initialize() {
        initializeFragments()
    }

    /**
     * Add fragments to the List backing the [AggregateFeedFragment.slidingTabLayout].
     */
    private fun initializeFragments() {
        floatingActionButton.onClick { onClickFab }
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(fabDrawableListener)
        pageIndicator.setViewPager(viewPager)
        drawNextButton()
    }

    private val fabDrawableListener: ViewPager.OnPageChangeListener
        get() = object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                    position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                selectedPage = position
                if (position == (adapter.count - 1)) {
                    drawDoneButton()
                } else {
                    drawNextButton()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        }

    @TargetApi(LOLLIPOP)
    private fun drawDoneButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(activity, R.raw.ic_done, marginSVGLarge, colorWhite))
        setAlpha(floatingActionButton, 1f)
        if (SDK_INT >= LOLLIPOP) {
            floatingActionButton.backgroundTintList = colorSelected
        }
    }

    @TargetApi(LOLLIPOP)
    private fun drawNextButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(activity, ic_chevron_right, marginSVGLarge, colorWhite))
        setAlpha(floatingActionButton, .3f)
        if (SDK_INT >= LOLLIPOP) {
            floatingActionButton.backgroundTintList = colorTransparent
        }
    }

    companion object {
        fun newInstance(): MainIntroductionFragment {
            return MainIntroductionFragment()
        }
    }
}
