/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package butter.droid.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import butter.droid.R;

/**
 * A layout that draws something in the insets passed to {@link #fitSystemWindows(android.graphics.Rect)}, i.e. the area above UI
 * chrome (status and navigation bars, overlay action bars).
 * <p/>
 * Essentially its used to allow a navigation drawer to appear behind the status bar.
 */
public class ScrimInsetsFrameLayout extends FrameLayout {

    private Drawable insetForeground;

    private Rect insets;
    private final Rect tempRect = new Rect();
    private OnInsetsCallback onInsetsCallback;

    public ScrimInsetsFrameLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ScrimInsetsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ScrimInsetsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrimInsetsFrameLayout, defStyle, 0);
        if (a == null) {
            return;
        }
        insetForeground = a.getDrawable(R.styleable.ScrimInsetsFrameLayout_insetForegroundColor);
        a.recycle();

        setWillNotDraw(true);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        this.insets = new Rect(insets);
        setWillNotDraw(insetForeground == null);
        ViewCompat.postInvalidateOnAnimation(this);
        if (onInsetsCallback != null) {
            onInsetsCallback.onInsetsChanged(insets);
        }
        return true; // consume insets
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (insets != null && insetForeground != null) {
            final int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            // Top
            tempRect.set(0, 0, width, insets.top);
            insetForeground.setBounds(tempRect);
            insetForeground.draw(canvas);

            // Bottom
            tempRect.set(0, height - insets.bottom, width, height);
            insetForeground.setBounds(tempRect);
            insetForeground.draw(canvas);

            // Left
            tempRect.set(0, insets.top, insets.left, height - insets.bottom);
            insetForeground.setBounds(tempRect);
            insetForeground.draw(canvas);

            // Right
            tempRect.set(width - insets.right, insets.top, width, height - insets.bottom);
            insetForeground.setBounds(tempRect);
            insetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (insetForeground != null) {
            insetForeground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (insetForeground != null) {
            insetForeground.setCallback(null);
        }
    }

    /**
     * Allows the calling container to specify a callback for custom processing when insets change (i.e. when
     * {@link #fitSystemWindows(android.graphics.Rect)} is called. This is useful for setting padding on UI elements based on UI chrome
     * insets (e.g. a Google Map or a ListView). When using with ListView or GridView, remember to set clipToPadding to false.
     */
    public void setOnInsetsCallback(OnInsetsCallback onInsetsCallback) {
        this.onInsetsCallback = onInsetsCallback;
    }

    public static interface OnInsetsCallback {
        public void onInsetsChanged(Rect insets);
    }
}
