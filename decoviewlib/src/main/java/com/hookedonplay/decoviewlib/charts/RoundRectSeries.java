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

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;

public class RoundRectSeries extends ChartSeries {

    RoundRectSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
        Log.e(TAG, "RoundRectSeries is experimental. Functionality is implemented.");
    }

    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }

//        processRevealEffect();
//
//        final float endPos = calcCurrentPosition(mPositionStart, mPositionEnd, mSeriesItem.getMinValue(), mSeriesItem.getMaxValue(), mPercentComplete);
//        float sweep = verifyMinSweepAngle(endPos * mAngleSweep);
//        float startAngle = mAngleStart;
//
//        sweep = adjustSweepDirection(sweep);
//
//        if (mSeriesItem.getDrawAsPoint()) {
//            startAngle = adjustDrawPointAngle(sweep);
//            sweep = adjustSweepDirection(getMinSweepAngle());
//
//        }
//
//        float insetX = mSeriesItem.getInset() != null ? mSeriesItem.getInset().x : 0;
//        float insetY = mSeriesItem.getInset() != null ? mSeriesItem.getInset().y : 0;
//        float lineWidth = (getSeriesItem().getLineWidth() / 2);
//
//        Log.e("TAG", " Inset  x: " + insetX + " y: " + insetY + " line " + lineWidth);
//        Path path = new Path();
//        path.moveTo(insetX + lineWidth, lineWidth + insetY);
//        path.lineTo(canvas.getWidth() - insetX - lineWidth, lineWidth + insetY);
//        path.lineTo(canvas.getWidth() - insetX - lineWidth, canvas.getHeight() - lineWidth - insetY);
//
//
//        canvas.drawPath(path, mPaint);
        return true;
    }

    /**
     * Build a gradient if required. This will be executed every time the bounds changed. Subclasses
     * must implement this method to create a gradient that will work with the given shape
     */
    @Override
    protected void applyGradientToPaint() {

    }
}
