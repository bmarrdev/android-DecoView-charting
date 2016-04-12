/*
 * Copyright (C) 2015 Brent Marriott
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
package com.hookedonplay.decoviewlib.charts;

import android.graphics.Color;

/**
 * Helper to calculate color change over a period of time
 */
public class ColorAnimate {
    static public final int ANIMATE_ALPHA = 0x01;
    static public final int ANIMATE_RED = 0x02;
    static public final int ANIMATE_GREEN = 0x04;
    static public final int ANIMATE_BLUE = 0x08;
    static public final int ANIMATE_ALL = ANIMATE_ALPHA | ANIMATE_RED | ANIMATE_GREEN | ANIMATE_BLUE;

    /**
     * Mask to determine which color values to animate
     */
    private int mMask;
    /**
     * Color at 0% complete
     */
    private final int mColorStart;
    /**
     * Color at 100% complete
     */
    private final int mColorEnd;
    /**
     * Current calculated color
     */
    private int mColorCurrent;

    /**
     * Create a new ColorAnimate helper
     *
     * @param colorStart initial color
     * @param colorEnd   final color
     */
    public ColorAnimate(int colorStart, int colorEnd) {
        mColorStart = colorStart;
        mColorEnd = colorEnd;
        mColorCurrent = mColorStart;
        setMask(ANIMATE_ALL);
    }

    public void setMask(int mask) {
        mMask = mask;
    }

    private int getValue(int mask, int start, int end, float percent) {
        if ((mask & mMask) == 0) {
            return start;
        }

        return start + (int) ((end - start) * percent);
    }

    /**
     * Calculate the color based on amount of the animation completed
     *
     * @param percentComplete percentage of animation completed
     * @return color at current position
     */
    public int getColorCurrent(float percentComplete) {
        mColorCurrent = Color.argb(
                getValue(ANIMATE_ALPHA, Color.alpha(mColorStart), Color.alpha(mColorEnd), percentComplete),
                getValue(ANIMATE_RED, Color.red(mColorStart), Color.red(mColorEnd), percentComplete),
                getValue(ANIMATE_GREEN, Color.green(mColorStart), Color.green(mColorEnd), percentComplete),
                getValue(ANIMATE_BLUE, Color.blue(mColorStart), Color.blue(mColorEnd), percentComplete));

        return mColorCurrent;
    }
}