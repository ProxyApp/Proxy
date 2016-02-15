/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shareyourproxy.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewConfigurationCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import com.shareyourproxy.R
import com.shareyourproxy.util.ButterKnife.bindBool
import com.shareyourproxy.util.ButterKnife.bindColor
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindInt

/**
 * Draws circles (one for each view). The current view position is filled and others are only stroked.
 */
class CirclePageIndicator : View, PageIndicator {
    private val PaintPageFill = Paint(ANTI_ALIAS_FLAG)
    private val paintStroke = Paint(ANTI_ALIAS_FLAG)
    private val paintFill = Paint(ANTI_ALIAS_FLAG)
    var radius: Float = 0.toFloat()
        set(radius) {
            this.radius = radius
            invalidate()
        }
    private var viewPager: ViewPager? = null
    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var currentPage: Int = 0
    private var snapPage: Int = 0
    private var pageOffset: Float = 0.toFloat()
    private var scrollState: Int = 0
    var orientation: Int = 0
        set(orientation) = when (orientation) {
            HORIZONTAL, VERTICAL -> {
                this.orientation = orientation
                requestLayout()
            }

            else -> throw IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.")
        }
    var isCentered: Boolean = false
        set(centered) {
            this.isCentered = centered
            invalidate()
        }
    var isSnap: Boolean = false
        set(snap) {
            this.isSnap = snap
            invalidate()
        }

    private var touchSlop: Int = 0
    private var lastMotionX = -1f
    private var activePointerId = INVALID_POINTER
    private var isDragging: Boolean = false

    private val defaultPageColor by bindColor(R.color.default_circle_indicator_page_color)
    private val defaultFillColor by bindColor(R.color.default_circle_indicator_fill_color)
    private val defaultOrientation by bindInt(R.integer.default_circle_indicator_orientation)
    private val defaultStrokeColor by bindColor(R.color.default_circle_indicator_stroke_color)
    private val defaultStrokeWidth by bindDimen(R.dimen.default_circle_indicator_stroke_width)
    private val defaultRadius by bindDimen(R.dimen.default_circle_indicator_radius)
    private val defaultCentered by bindBool(R.bool.default_circle_indicator_centered)
    private val defaultSnap by bindBool(R.bool.default_circle_indicator_snap)

    @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(context, attrs, defStyle) {
        initialize(attrs, defStyle)
    }

    constructor(context: Context) : super(context, null, 0) {
        initialize(null, 0)
    }

    private fun initialize(attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode) return

        //Retrieve styles attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0)

        isCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered)
        orientation = a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation)
        PaintPageFill.style = Style.FILL
        PaintPageFill.color = a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor)
        paintStroke.style = Style.STROKE
        paintStroke.color = a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor)
        paintStroke.strokeWidth = a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth.toFloat())
        paintFill.style = Style.FILL
        paintFill.color = a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor)
        radius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius.toFloat())
        isSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap)

        val background = a.getDrawable(R.styleable.CirclePageIndicator_android_background)
        if (background != null) {
            setBackground(background)
        }

        a.recycle()

        val configuration = ViewConfiguration.get(context)
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (viewPager == null) {
            return
        }
        val count = viewPager!!.adapter.count
        if (count == 0) {
            return
        }

        if (currentPage >= count) {
            setCurrentItem(count - 1)
            return
        }

        val longSize: Int
        val longPaddingBefore: Int
        val longPaddingAfter: Int
        val shortPaddingBefore: Int
        if (orientation == HORIZONTAL) {
            longSize = width
            longPaddingBefore = paddingLeft
            longPaddingAfter = paddingRight
            shortPaddingBefore = paddingTop
        } else {
            longSize = height
            longPaddingBefore = paddingTop
            longPaddingAfter = paddingBottom
            shortPaddingBefore = paddingLeft
        }

        val threeRadius = radius * 3
        val shortOffset = shortPaddingBefore + radius
        var longOffset = longPaddingBefore + radius
        if (isCentered) {
            longOffset += (longSize - longPaddingBefore - longPaddingAfter) / 2.0f - count * threeRadius / 2.0f
        }

        var dX: Float
        var dY: Float

        var pageFillRadius = radius
        if (paintStroke.strokeWidth > 0) {
            pageFillRadius -= paintStroke.strokeWidth / 2.0f
        }

        //Draw stroked circles
        for (iLoop in 0..count - 1) {
            val drawLong = longOffset + iLoop * threeRadius
            if (orientation == HORIZONTAL) {
                dX = drawLong
                dY = shortOffset
            } else {
                dX = shortOffset
                dY = drawLong
            }
            // Only paint fill if not completely transparent
            if (PaintPageFill.alpha > 0) {
                canvas.drawCircle(dX, dY, pageFillRadius, PaintPageFill)
            }

            // Only paint stroke if a stroke width was non-zero
            if (pageFillRadius != radius) {
                canvas.drawCircle(dX, dY, radius, paintStroke)
            }
        }

        //Draw the filled circle according to the current scroll
        var cx = (if (isSnap) snapPage else currentPage) * threeRadius
        if (!isSnap) {
            cx += pageOffset * threeRadius
        }
        if (orientation == HORIZONTAL) {
            dX = longOffset + cx
            dY = shortOffset
        } else {
            dX = shortOffset
            dY = longOffset + cx
        }
        canvas.drawCircle(dX, dY, radius, paintFill)
    }

    override fun onTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (super.onTouchEvent(ev)) {
            return true
        }
        if (viewPager == null || viewPager!!.adapter.count == 0) {
            return false
        }

        val action = ev.action and MotionEventCompat.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = MotionEventCompat.getPointerId(ev, 0)
                lastMotionX = ev.x
            }

            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)
                val x = MotionEventCompat.getX(ev, activePointerIndex)
                val deltaX = x - lastMotionX

                if (!isDragging) {
                    if (Math.abs(deltaX) > touchSlop) {
                        isDragging = true
                    }
                }

                if (isDragging) {
                    lastMotionX = x
                    if (viewPager!!.isFakeDragging || viewPager!!.beginFakeDrag()) {
                        viewPager!!.fakeDragBy(deltaX)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val count = viewPager!!.adapter.count
                    val width = width
                    val halfWidth = width / 2f
                    val sixthWidth = width / 6f

                    if (currentPage > 0 && ev.x < halfWidth - sixthWidth) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            viewPager!!.currentItem = currentPage - 1
                        }
                        return true
                    } else if (currentPage < count - 1 && ev.x > halfWidth + sixthWidth) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            viewPager!!.currentItem = currentPage + 1
                        }
                        return true
                    }
                }

                isDragging = false
                activePointerId = INVALID_POINTER
                if (viewPager!!.isFakeDragging) viewPager!!.endFakeDrag()
            }

            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                lastMotionX = MotionEventCompat.getX(ev, index)
                activePointerId = MotionEventCompat.getPointerId(ev, index)
            }

            MotionEventCompat.ACTION_POINTER_UP -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                }
                lastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, activePointerId))
            }
        }

        return true
    }

    override fun setViewPager(view: ViewPager) {
        if (viewPager === view) {
            return
        }
        if (viewPager != null) {
            viewPager!!.addOnPageChangeListener(null)
        }
        if (view.adapter == null) {
            throw IllegalStateException("ViewPager does not have adapter instance.")
        }
        viewPager = view
        invalidate()
    }

    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        setViewPager(view)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(item: Int) {
        if (viewPager == null) {
            throw IllegalStateException("ViewPager has not been bound.")
        }
        viewPager!!.currentItem = item
        currentPage = item
        invalidate()
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    override fun onPageScrollStateChanged(state: Int) {
        scrollState = state

        if (onPageChangeListener != null) {
            onPageChangeListener!!.onPageScrollStateChanged(state)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        currentPage = position
        pageOffset = positionOffset
        invalidate()

        if (onPageChangeListener != null) {
            onPageChangeListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }

    override fun onPageSelected(position: Int) {
        if (isSnap || scrollState == ViewPager.SCROLL_STATE_IDLE) {
            currentPage = position
            snapPage = position
            invalidate()
        }

        if (onPageChangeListener != null) {
            onPageChangeListener!!.onPageSelected(position)
        }
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        onPageChangeListener = listener
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (orientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec))
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec))
        }
    }

    /**
     * Determines the width of this view

     * @param measureSpec A measureSpec packed into an int
     * *
     * @return The width of the view, honoring constraints from measureSpec
     */
    private fun measureLong(measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY || viewPager == null) {
            //We were told how big to be
            result = specSize
        } else {
            //Calculate the width according the views count
            val count = viewPager!!.adapter.count
            result = (paddingLeft.toFloat() + paddingRight.toFloat()
                    + count.toFloat() * 2f * radius + (count - 1) * radius + 1f).toInt()
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    /**
     * Determines the height of this view

     * @param measureSpec A measureSpec packed into an int
     * *
     * @return The height of the view, honoring constraints from measureSpec
     */
    private fun measureShort(measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize
        } else {
            //Measure the height
            result = (2 * radius + paddingTop.toFloat() + paddingBottom.toFloat() + 1f).toInt()
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        currentPage = savedState.currentPage
        snapPage = savedState.currentPage
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPage = currentPage
        return savedState
    }

    internal class SavedState : View.BaseSavedState {
        var currentPage: Int = 0

        constructor(superState: Parcelable) : super(superState) {
        }

        private constructor(`in`: Parcel) : super(`in`) {
            currentPage = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPage)
        }

        companion object {
            @SuppressWarnings("UnusedDeclaration")
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private val INVALID_POINTER = -1
    }
}