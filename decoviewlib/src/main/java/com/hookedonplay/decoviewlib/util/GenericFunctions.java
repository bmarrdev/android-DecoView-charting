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
package com.hookedonplay.decoviewlib.util;

import android.content.Context;
import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public class GenericFunctions {

    static private boolean mInitialized = false;
    static private float mScaledDensity = 3.0f;

    static public void initialize(@NonNull Context context) {
        mInitialized = true;
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    }

    static public float pixelsToSp(final float px) {
        verifyInitialized();
        return px / mScaledDensity;
    }

    static public float spToPixels(final float sp) {
        verifyInitialized();
        return sp * mScaledDensity;
    }

    static public void verifyInitialized()
            throws IllegalStateException {
        if (!mInitialized) {
            throw new IllegalStateException("Missing call to GenericFunctions::initialize()");
        }
    }
}
