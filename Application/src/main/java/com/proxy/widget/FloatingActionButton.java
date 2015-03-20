/*
* Copyright 2014, The Android Open Source Project
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
*
*/

package com.proxy.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Checkable;
import android.widget.FrameLayout;

import com.proxy.R;


/**
 * A Floating Action Button is a {@link android.widget.Checkable} view distinguished by a circled
 * icon floating above the UI, with special motion behaviors.
 */
@SuppressLint("NewApi")
public class FloatingActionButton extends FrameLayout implements Checkable {

    /**
     * An array of states.
     */
    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };
    // Class tag.
    private int mDrawableSize;
    private int mColorNormal;
    private int mColorPressed;
    // A boolean that tells if the FAB is checked or not.
    private boolean mChecked;

    /**
     * Constructor.
     *
     * @param context Context object.
     */
    public FloatingActionButton(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor.
     *
     * @param context Context object.
     * @param attrs   Attributes.
     */
    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor.
     *
     * @param context      Context object.
     * @param attrs        Attributes.
     * @param defStyleAttr Default Style Attributes.
     */
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initialize values.
     *
     * @param context      view context
     * @param attributeSet the attributes added in xml
     */
    public void init(Context context, AttributeSet attributeSet) {
        setClickable(true);

        // initial attribute values
        if (attributeSet != null) {
            TypedArray attr = context.obtainStyledAttributes(attributeSet,
                R.styleable.FloatingActionButton, 0, 0);
            mColorNormal = attr.getColor(R.styleable.FloatingActionButton_colorNormal,
                getResources().getColor(android.R.color.holo_blue_dark));
            mColorPressed = attr.getColor(R.styleable.FloatingActionButton_colorPressed,
                getResources().getColor(android.R.color.holo_blue_light));
            attr.recycle();
        } else {
            mColorNormal = getResources().getColor(android.R.color.holo_blue_dark);
            mColorPressed = getResources().getColor(android.R.color.holo_blue_light);
        }

        // initiate constant draw values
        initValues();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set the outline provider for this view. The provider is given the outline which it
            // can
            // then modify as needed. In this case we set the outline to be an oval fitting the
            // height
            // and width.
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, getWidth(), getHeight());
                }
            });

            // Finally, enable clipping to the outline, using the provider we set above
            setClipToOutline(true);
        } else {
            updateBackground();
        }
    }

    /**
     * Initiate the shadow and circle draw variables.
     */
    public void initValues() {
        float density = getResources().getDisplayMetrics().density;
        mDrawableSize = (int) (getHeight() * density);
    }

    /**
     * Updates the background for non Lollipop devices. For now we just draw a circle TODO: add
     * shadowing drawable to the layerlist drawable
     */
    private void updateBackground() {
        LayerDrawable layerDrawable = new LayerDrawable(
            new Drawable[]{
                createFillDrawable(),
            });
        setBackgroundCompat(layerDrawable);
    }

    /**
     * Creates a State List Drawable for the background circle.
     *
     * @return Drawable
     */
    private StateListDrawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{ android.R.attr.state_pressed },
            createCircleDrawable(mColorPressed));
        drawable.addState(new int[]{ }, createCircleDrawable(mColorNormal));
        return drawable;
    }

    /**
     * Create the base circle drawable.
     *
     * @param color Color of the circle drawable. Not a color resource.
     * @return Drawable
     */
    private ShapeDrawable createCircleDrawable(int color) {
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setAntiAlias(true);
        shapeDrawable.setIntrinsicHeight(mDrawableSize);
        shapeDrawable.setIntrinsicWidth(mDrawableSize);
        return shapeDrawable;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * Sets the checked/unchecked state of the FAB.
     *
     * @param checked boolean value for checked state
     */
    public void setChecked(boolean checked) {
        // If trying to set the current state, ignore.
        if (checked == mChecked) {
            return;
        }
        mChecked = checked;

        // Now refresh the drawable state (so the icon changes)
        refreshDrawableState();
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    /**
     * Override performClick() so that we can toggle the checked state when the view is clicked.
     *
     * @return True there was an assigned OnClickListener that was called, false otherwise is
     * returned.
     */
    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // As we have changed size, we should invalidate the outline so that is the the
        // correct size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invalidateOutline();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    /**
     * Sets the background for a given drawable. Works for older versions of android.
     *
     * @param drawable the drawable to be set as the background
     */
    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

}
