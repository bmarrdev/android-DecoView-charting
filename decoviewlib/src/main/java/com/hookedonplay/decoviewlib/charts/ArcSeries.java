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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;

abstract public class ArcSeries extends ChartSeries {
    /**
     * Angle that the arc starts
     */
    protected float mArcAngleStart;
    /**
     * Sweep angle starts at {@link #mArcAngleStart}
     */
    protected float mArcAngleSweep;

    ArcSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
    }

    /**
     * Draw the arc in the current state
     *
     * @param canvas Canvas to draw onto
     */
    abstract void drawArc(Canvas canvas);

    /**
     * Draw this arc in the current position calculated by the ValueAnimator.
     *
     * @param canvas Canvas used to draw
     * @param bounds Bounds to be used to draw the arc
     * @return true is draw view has been handled
     */
    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }

        final float endPos = calcCurrentPosition(mPositionStart, mPositionEnd, mSeriesItem.getMinValue(), mSeriesItem.getMaxValue(), mPercentComplete);
        mArcAngleSweep = adjustSweepDirection(verifyMinSweepAngle(endPos * mAngleSweep));
        mArcAngleStart = mAngleStart;

        if (mSeriesItem.getDrawAsPoint()) {
            mArcAngleStart = adjustDrawPointAngle(mArcAngleSweep);
            mArcAngleSweep = adjustSweepDirection(getMinSweepAngle());
        }

        return false;
    }

    /**
     * Build a gradient if required. This must be executed every time the bounds changes
     */
    protected void applyGradientToPaint() {
        if (Color.alpha(mSeriesItem.getSecondaryColor()) != 0) {
            SweepGradient gradient;
            if (mAngleSweep < 360) {
                /**
                 * When we have less than a full circle we change the style of gradient so that
                 * the two colors start at the same point. The two provided colors meet rather than
                 * a fade the complete circumference. A matrix is rotated so the meeting of the
                 * two colors occurs in the middle of the gap when the part circle is not drawn
                 */
                final int[] colors = {mSeriesItem.getColor(), mSeriesItem.getSecondaryColor()};
                final float[] positions = {0, 1};
                gradient = new SweepGradient(mBounds.centerX(), mBounds.centerY(), colors, positions);
                Matrix gradientRotationMatrix = new Matrix();
                gradientRotationMatrix.preRotate(mAngleStart - ((360f - mAngleSweep) / 2), mBounds.centerX(), mBounds.centerY());
                gradient.setLocalMatrix(gradientRotationMatrix);
            } else {
                /**
                 * Drawing a gradient around the complete circumference of the circle. This
                 * gradient fades gently between the two colors.
                 */
                final int[] colors = {mSeriesItem.getSecondaryColor(), mSeriesItem.getColor(), mSeriesItem.getSecondaryColor()};
                final float[] positions = {0, 0.5f * (mAngleSweep / 360f), 1};
                gradient = new SweepGradient(mBounds.centerX(), mBounds.centerY(), colors, positions);
            }

            mPaint.setShader(gradient);
        }
    }
}