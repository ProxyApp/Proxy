/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 William Mora
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.proxy.widget.snackbar;

import android.app.Activity;
import android.support.annotation.NonNull;

/**
 * Only one snack at a time. Dismiss Active SnackBars immediately when a new SnackBar instance is
 * made.
 */
public class SnackBarManager {

    private static SnackBar currentSnackBar;

    /**
     * Constructor.
     */
    private SnackBarManager() {
    }

    /**
     * Displays a {@link SnackBar} in the current {@link Activity}, dismissing the
     * current SnackBar being displayed, if any.
     *
     * @param snackBar instance of {@link SnackBar} to display
     * @return the snack bar entered
     */
    public static SnackBar show(@NonNull SnackBar snackBar) {
        show(snackBar, (Activity) snackBar.getContext());
        return snackBar;
    }

    /**
     * Displays a {@link SnackBar} in the current {@link Activity}, dismissing the
     * current SnackBar being displayed, if any.
     *
     * @param snackBar instance of {@link SnackBar} to display
     * @param activity target {@link Activity} to display the SnackBar
     */
    private static void show(@NonNull SnackBar snackBar, @NonNull Activity activity) {
        if (currentSnackBar != null) {
            currentSnackBar.removeSnackBar();
        }
        currentSnackBar = snackBar;
        currentSnackBar.showNewSnackBar(activity);
    }

}
