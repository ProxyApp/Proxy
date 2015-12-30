package com.shareyourproxy.app.fragment

import android.R.color.black
import android.R.color.white
import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat.setAlpha
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.R
import com.shareyourproxy.R.color.common_proxy_zoidberg
import com.shareyourproxy.R.dimen.common_svg_large
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.ic_chevron_right
import com.shareyourproxy.app.fragment.AggregateFeedFragment.Companion.ARG_SELECT_PROFILE_TAB
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.util.bindColor
import com.shareyourproxy.util.bindColorStateList
import com.shareyourproxy.util.bindDimen
import com.shareyourproxy.util.bindView
import com.viewpagerindicator.CirclePageIndicator
import org.jetbrains.anko.onClick
import java.util.Arrays.asList

/**
 * The main introduction fragment.
 */
class MainIntroductionFragment : BaseFragment() {
    private val viewPager: ViewPager by bindView(fragment_introduction_main_viewpager)
    private val pageIndicator: CirclePageIndicator by bindView(fragment_introduction_main_pageindicator)
    private val floatingActionButton: FloatingActionButton by bindView(fragment_introduction_main_fab)
    private val colorTransparent: ColorStateList by bindColorStateList(black)
    private val colorSelected: ColorStateList by bindColorStateList(common_proxy_zoidberg)
    private val colorWhite: Int by bindColor(white)
    private val marginSVGLarge: Int by bindDimen(common_svg_large)
    private val fragmentArray: List<BaseFragment> = asList(FirstIntroductionFragment(), SecondIntroductionFragment(), ThirdIntroductionFragment())
    private val adapter: BaseFragment.BasePagerAdapter = BasePagerAdapter(fragmentArray, childFragmentManager)
    private val onClickFab: View.OnClickListener = View.OnClickListener {
        if (selectedPage == (adapter.count - 1)) {
            launchMainActivity(activity, ARG_SELECT_PROFILE_TAB, false, null)
            sharedPreferences.edit().putBoolean(KEY_PLAY_INTRODUCTION, false).commit()
            activity.finish()
        } else {
            viewPager.setCurrentItem(++selectedPage, true)
        }
    }
    private val fabDrawableListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
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
    private var selectedPage = 0

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
}
