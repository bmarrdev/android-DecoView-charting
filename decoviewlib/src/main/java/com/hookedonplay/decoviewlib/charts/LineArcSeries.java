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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hookedonplay.decoviewlib.DecoView;

import java.util.ArrayList;

/**
 * Concrete class to use for Arc based line chart type drawing
 */
public class LineArcSeries extends ArcSeries {

    public static final int CONCAVE_CLIP_ANGLE = 320;
    public static final float EXTRA_SWEEP_CLEANUP_FACTOR = 0.1f;

    private Path mConcaveClipPath;

    public LineArcSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        super(seriesItem, totalAngle, rotateAngle);
    }

    /**
     * Draw this arc in the current position calculated by the ValueAnimator.
     *
     * @param canvas Canvas used to draw
     * @param bounds Bounds to be used to draw the arc
     */
    @Override
    public boolean draw(Canvas canvas, RectF bounds) {
        if (super.draw(canvas, bounds)) {
            return true;
        }

        canvas.save();
        if (getSeriesItem().getEndCap() == EndCapType.CAP_CONCAVE) {
            processConcaveDraw(canvas, mArcAngleStart, true);
        }
        if (getSeriesItem().getEndCap() == EndCapType.CAP_CONCAVE) {
            if (mArcAngleSweep > CONCAVE_CLIP_ANGLE) {
                drawArc(canvas, mArcAngleStart, CONCAVE_CLIP_ANGLE);
                drawArcEdgeDetail(canvas, mArcAngleStart, CONCAVE_CLIP_ANGLE);
            } else {
                drawArc(canvas, mArcAngleStart, mArcAngleSweep);
                drawArcEdgeDetail(canvas, mArcAngleStart, mArcAngleSweep);
            }
        } else {
            drawArc(canvas, mArcAngleStart, mArcAngleSweep);
            drawArcEdgeDetail(canvas, mArcAngleStart, mArcAngleSweep);
        }
        canvas.restore();

        if (getSeriesItem().getEndCap() == EndCapType.CAP_CONCAVE) {
            if (mArcAngleSweep > 320) {
                canvas.save();
                processConcaveDraw(canvas, mArcAngleStart + CONCAVE_CLIP_ANGLE, false);
                drawArc(canvas,
                        mArcAngleStart + CONCAVE_CLIP_ANGLE,
                        mArcAngleSweep - CONCAVE_CLIP_ANGLE + EXTRA_SWEEP_CLEANUP_FACTOR);
                drawArcEdgeDetail(canvas,
                        mArcAngleStart + CONCAVE_CLIP_ANGLE,
                        mArcAngleSweep - CONCAVE_CLIP_ANGLE + EXTRA_SWEEP_CLEANUP_FACTOR);
                canvas.restore();
            }
        }

        return true;
    }

    private void processConcaveDraw(Canvas canvas, float clipAngle, boolean cleanUpAntialiasing) {
        float lineWidth = (getSeriesItem().getLineWidth() * 0.5f);
        float radius = mBoundsInset.width() / 2;
        float angle = (float)(Math.PI * clipAngle / 180.0f);
        float middleX = (float)(mBoundsInset.centerX() + radius * Math.cos(angle));
        float middleY = (float)(mBoundsInset.centerY() + radius * Math.sin(angle));
        if (mConcaveClipPath == null) {
            mConcaveClipPath = new Path();
        }
        mConcaveClipPath.reset();
        mConcaveClipPath.addCircle(middleX, middleY, lineWidth, Path.Direction.CW);
        if (cleanUpAntialiasing) {
            // Add another rect to clean up the end-cap antialiasing.
            mConcaveClipPath.addRect(middleX - (lineWidth + 2.0f), middleY - lineWidth, middleX, middleY + lineWidth, Path.Direction.CW);
        }
        canvas.clipPath(mConcaveClipPath, Region.Op.DIFFERENCE);
    }

    protected void drawArc(@NonNull Canvas canvas, float arcAngleStart, float arcAngleSweep) {
        canvas.drawArc(mBoundsInset,
                arcAngleStart,
                arcAngleSweep,
                false,
                mPaint);
    }

    /**
     * Draw the {@link EdgeDetail} for this View. Note that on API 11 - 17 clipPath is only available
     * if HardwareAcceleration is disable. A function {@link DecoView#enableCompatibilityMode()}
     * is provided which will disable on affected platforms however this needs to be explicitly
     * called by the user, otherwise EdgeDetails will not be drawn
     *
     * @param canvas Canvas to draw to
     * @param arcAngleStart The angle to start drawing the arc
     * @param arcAngleSweep The sweep angle
     */
    private void drawArcEdgeDetail(@NonNull Canvas canvas, float arcAngleStart, float arcAngleSweep) {
        ArrayList<EdgeDetail> edgeDetailList = getSeriesItem().getEdgeDetail();
        if (edgeDetailList == null) {
            return;
        }

        for (EdgeDetail edgeDetail : edgeDetailList) {
            final boolean drawInner = edgeDetail.getEdgeType() == EdgeDetail.EdgeType.EDGE_INNER;
            if (edgeDetail.getClipPath() == null) {
                float inset = (edgeDetail.getRatio() - 0.5f) * mPaint.getStrokeWidth();
                if (drawInner) {
                    inset = -inset;
                }

                Path clipPath = new Path();
                RectF clipRect = new RectF(mBoundsInset);
                clipRect.inset(inset, inset);
                clipPath.addOval(clipRect, Path.Direction.CW);
                edgeDetail.setClipPath(clipPath);
            }
            drawClippedArc(canvas, edgeDetail.getClipPath(), edgeDetail.getColor(),
                    drawInner ? Region.Op.INTERSECT : Region.Op.DIFFERENCE, arcAngleStart, arcAngleSweep);
        }
    }

    /**
     * Draw the {@link EdgeDetail} for this View. Note that on API 11 - 17 clipPath is only available
     * if HardwareAcceleration is disable. A function {@link DecoView#enableCompatibilityMode()}
     * is provided which will disable on affected platforms however this needs to be explicitly
     * called by the user, otherwise EdgeDetails will not be drawn
     */
    protected void drawClippedArc(@NonNull Canvas canvas, @NonNull Path path, int color, @NonNull Region.Op combine, float arcAngleStart, float arcAngleSweep) {
        canvas.save();

        try {
            canvas.clipPath(path, combine);
        } catch (UnsupportedOperationException e) {
            Log.w(TAG, "clipPath unavailable on API 11 - 17 without disabling hardware acceleration. (EdgeDetail functionality requires clipPath). Call DecoView.enableCompatibilityMode() to enable");
            canvas.restore();
            return;
        }

        int colorOld = mPaint.getColor();
        mPaint.setColor(color);
        drawArc(canvas, arcAngleStart, arcAngleSweep);
        mPaint.setColor(colorOld);
        canvas.restore();
    }
}
