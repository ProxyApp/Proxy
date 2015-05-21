/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proxy.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.proxy.R;


/**
 * Layout which an {@link android.widget.EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on
 * Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 */
@SuppressWarnings("unused")
public class FloatLabelLayout extends LinearLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float DEFAULT_LABEL_PADDING_LEFT = 3f;
    private static final float DEFAULT_LABEL_PADDING_TOP = 4f;
    private static final float DEFAULT_LABEL_PADDING_RIGHT = 3f;
    private static final float DEFAULT_LABEL_PADDING_BOTTOM = 4f;

    private EditText _editText;
    private TextView _label;

    private CharSequence _hint;
    private Interpolator _interpolator;

    /**
     * Constructor.
     *
     * @param context activity context
     */
    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     *
     * @param context activity context
     * @param attrs   attributes
     */
    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor.
     *
     * @param context  activity context
     * @param attrs    attributes
     * @param defStyle styles
     */
    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(VERTICAL);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        int leftPadding = a.getDimensionPixelSize(
            R.styleable.FloatLabelLayout_floatLabelPaddingLeft,
            dipsToPix(DEFAULT_LABEL_PADDING_LEFT));
        int topPadding = a.getDimensionPixelSize(
            R.styleable.FloatLabelLayout_floatLabelPaddingTop,
            dipsToPix(DEFAULT_LABEL_PADDING_TOP));
        int rightPadding = a.getDimensionPixelSize(
            R.styleable.FloatLabelLayout_floatLabelPaddingRight,
            dipsToPix(DEFAULT_LABEL_PADDING_RIGHT));
        int bottomPadding = a.getDimensionPixelSize(
            R.styleable.FloatLabelLayout_floatLabelPaddingBottom,
            dipsToPix(DEFAULT_LABEL_PADDING_BOTTOM));
        _hint = a.getText(R.styleable.FloatLabelLayout_floatLabelHint);

        _label = new TextView(context);
        _label.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        _label.setVisibility(INVISIBLE);
        _label.setText(_hint);
        ViewCompat.setPivotX(_label, 0f);
        ViewCompat.setPivotY(_label, 0f);

        _label.setTextAppearance(context,
            a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                android.R.style.TextAppearance_Small));
        a.recycle();

        addView(_label, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


        _interpolator = getInterpolator(context);
    }

    /**
     * Get the correct Interpolator based on API level.
     *
     * @param context this context
     * @return the Interpolator
     */
    @SuppressLint("NewApi")
    private Interpolator getInterpolator(Context context) {
        return AnimationUtils.loadInterpolator(context,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? android.R.interpolator.fast_out_slow_in
                : android.R.anim.decelerate_interpolator);
    }

    @Override
    public final void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    /**
     * Handle showing the label.
     *
     * @param animate should animate
     */
    private void updateLabelVisibility(boolean animate) {
        boolean hasText = !TextUtils.isEmpty(_editText.getText());
        boolean isFocused = _editText.isFocused();

        _label.setActivated(isFocused);

        if (hasText || isFocused) {
            // We should be showing the label so do so if it isn't already
            if (_label.getVisibility() != VISIBLE) {
                showLabel(animate);
            }
        } else {
            // We should not be showing the label so hide it
            if (_label.getVisibility() == VISIBLE) {
                hideLabel(animate);
            }
        }
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return _editText;
    }

    /**
     * Set this views {@link EditText}.
     *
     * @param editText this EditText
     */
    private void setEditText(EditText editText) {
        // If we already have an EditText, throw an exception
        if (_editText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        _editText = editText;

        // Update the label visibility with no animation
        updateLabelVisibility(false);

        // Add a TextWatcher so that we know when the text input has changed
        _editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLabelVisibility(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        _editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                updateLabelVisibility(true);
            }
        });

        // If we do not have a valid hint, try and retrieve it from the EditText
        if (TextUtils.isEmpty(_hint)) {
            setHint(_editText.getHint());
        }
    }

    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return _label;
    }

    /**
     * Set the hint to be displayed in the floating label.
     *
     * @param hint this labels hint
     */
    public void setHint(CharSequence hint) {
        _hint = hint;
        _label.setText(hint);
    }

    /**
     * Show the label.
     *
     * @param animate should animate
     */
    private void showLabel(boolean animate) {
        if (animate) {
            _label.setVisibility(View.VISIBLE);
            ViewCompat.setTranslationY(_label, _label.getHeight());

            float scale = _editText.getTextSize() / _label.getTextSize();
            ViewCompat.setScaleX(_label, scale);
            ViewCompat.setScaleY(_label, scale);

            ViewCompat.animate(_label)
                .translationY(0f)
                .scaleY(1f)
                .scaleX(1f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null)
                .setInterpolator(_interpolator).start();
        } else {
            _label.setVisibility(VISIBLE);
        }

        _editText.setHint(null);
    }

    /**
     * Hide the label.
     *
     * @param animate should animate
     */
    private void hideLabel(boolean animate) {
        if (animate) {
            float scale = _editText.getTextSize() / _label.getTextSize();
            ViewCompat.setScaleX(_label, 1f);
            ViewCompat.setScaleY(_label, 1f);
            ViewCompat.setTranslationY(_label, 0f);

            ViewCompat.animate(_label)
                .translationY(_label.getHeight())
                .setDuration(ANIMATION_DURATION)
                .scaleX(scale)
                .scaleY(scale)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        _label.setVisibility(INVISIBLE);
                        _editText.setHint(_hint);
                    }
                })
                .setInterpolator(_interpolator).start();
        } else {
            _label.setVisibility(INVISIBLE);
            _editText.setHint(_hint);
        }
    }

    /**
     * Helper method to convert dips to pixels.
     *
     * @param dps density pixels
     * @return number of pixels
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
            getResources().getDisplayMetrics());
    }
}
