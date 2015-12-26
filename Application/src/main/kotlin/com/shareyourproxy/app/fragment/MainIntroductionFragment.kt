package com.shareyourproxy.app.fragment

import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.shareyourproxy.R
import com.viewpagerindicator.CirclePageIndicator

import java.util.Arrays

import butterknife.Bind
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.ButterKnife
import butterknife.OnClick

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchMainActivity
import com.shareyourproxy.app.fragment.AggregateFeedFragment.ARG_SELECT_PROFILE_TAB
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable

/**
 * Created by Evan on 9/21/15.
 */
class MainIntroductionFragment : BaseFragment() {
    @Bind(R.id.fragment_introduction_main_viewpager)
    internal var viewPager: ViewPager
    @Bind(R.id.fragment_introduction_main_pageindicator)
    internal var pageIndicator: CirclePageIndicator
    @Bind(R.id.fragment_introduction_main_fab)
    internal var floatingActionButton: FloatingActionButton
    @BindColor(android.R.color.black)
    internal var colorTransparent: ColorStateList
    @BindColor(R.color.common_proxy_zoidberg)
    internal var colorSelected: ColorStateList
    @BindColor(android.R.color.white)
    internal var colorWhite: Int = 0
    @BindDimen(R.dimen.common_svg_large)
    internal var marginSVGLarge: Int = 0
    private var _selectedPage = 0
    private var _adapter: BaseFragment.BasePagerAdapter? = null

    @OnClick(R.id.fragment_introduction_main_fab)
    fun onClickFab() {
        if (_selectedPage == (_adapter!!.count - 1)) {
            launchMainActivity(activity, ARG_SELECT_PROFILE_TAB, false, null)
            sharedPreferences.edit().putBoolean(KEY_PLAY_INTRODUCTION, false).commit()
            activity.finish()
        } else {
            viewPager.setCurrentItem(++_selectedPage, true)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_introduction_main, container, false)
        ButterKnife.bind(this, rootView)
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
        val fragmentArray = Arrays.asList<BaseFragment>(
                FirstIntroductionFragment.newInstance(),
                SecondIntroductionFragment.newInstance(),
                ThirdIntroductionFragment.newInstance())

        _adapter = BaseFragment.BasePagerAdapter.newInstance(fragmentArray, childFragmentManager)
        viewPager.adapter = _adapter
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
                _selectedPage = position
                if (position == (_adapter!!.count - 1)) {
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
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(activity, R.raw.ic_done,
                marginSVGLarge, colorWhite))
        ViewCompat.setAlpha(floatingActionButton, 1f)
        if (SDK_INT >= LOLLIPOP) {
            floatingActionButton.backgroundTintList = colorSelected
        }
    }

    @TargetApi(LOLLIPOP)
    private fun drawNextButton() {
        floatingActionButton.setImageDrawable(svgToBitmapDrawable(activity,
                R.raw.ic_chevron_right, marginSVGLarge, colorWhite))
        ViewCompat.setAlpha(floatingActionButton, .3f)
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
/**
 * Default Constructor.
 */
