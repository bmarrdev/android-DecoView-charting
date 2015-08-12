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
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hookedonplay.decoviewlib.DecoView;

/**
 * Animates some non-core movements for the series of data, such as fades and swirls.
 */
public class DecoDrawEffect {
    /**
     * Value for fully opaque alpha value
     */
    static private final int MAX_ALPHA = 255;
    /**
     * Minimum percentage of dimension to allow explode lines
     */
    static private final float EXPLODE_LINE_MIN = 0.01f;
    /**
     * Maximum percentage of dimension to allow explode lines
     */
    static private final float EXPLODE_LINE_MAX = 0.1f;
    /**
     * Minimum radius of circle in explode mode
     */
    static private final float EXPLODE_CIRCLE_MIN = 0.01f;
    /**
     * Maximum radius of circle in explode mode
     */
    static private final float EXPLODE_CIRCLE_MAX = 0.1f;
    /**
     * Number of lines created during explode animation
     */
    static private final int EXPLODE_LINE_COUNT = 9;
    static private final float MIN_LINE_WIDTH = 10f;
    static private final float MAX_LINE_WIDTH = 100f;
    /**
     * Effect type to draw
     * {@link EffectType}
     */
    private final EffectType mEffectType;
    /**
     * Paint to use for drawing arc item in effect
     */
    private Paint mPaint;
    /**
     * Paint to use for drawing explode effect
     */
    private Paint mPaintExplode;
    /**
     * Paint to use for drawing text in effect
     */
    private Paint mPaintText;
    /**
     * String to display during EFFECT_EXPLODE and EFFECT_SPIRAL_EXPLODE
     */
    private String mText;
    /**
     * Bounds used to allow contraction (or expansion) of spiral animations
     */
    private RectF mSpinBounds = new RectF();

    private int mCircuits = 6;

    /**
     * Construct the delegate for painting the special effects for the arc
     *
     * @param effectType Type of animation
     * @param paint      Paint to use to perform the effect
     * @param text       Optional text to display during some effects
     *                   <p/>
     *                   No Access Modifier for the constructor is specified. This is deliberate so we use the
     *                   default package access. This class should not be constructed outside of the package scope.
     *                   Clients of this library need only to pass an EffectType to the {@link DecoView}
     */
    DecoDrawEffect(@NonNull EffectType effectType, @NonNull Paint paint, @Nullable String text) {
        mEffectType = effectType;
        setPaint(paint);
        setText(text, paint.getColor());
    }

    @SuppressWarnings("unused")
    DecoDrawEffect(@NonNull EffectType effectType, @NonNull Paint paint) {
        mEffectType = effectType;
        setPaint(paint);
    }

    /**
     * Determine the visibility of the arc on completion of this animation effect.
     *
     * @return should remain visible
     */
    public boolean postExecuteVisibility() {
        return (mEffectType == EffectType.EFFECT_SPIRAL_OUT) ||
                (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL);
    }

    private void setPaint(@NonNull Paint paint) {
        mPaint = new Paint(paint);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(determineLineWidth(paint, 1f));

        /**
         * Create the explode line based on the same paint attributes to keep consistent
         * The line width is made smaller as they will be X lines created during this explode
         * effect
         */
        mPaintExplode = new Paint(paint);
        mPaintExplode.setStrokeCap(Paint.Cap.ROUND);
        mPaintExplode.setStyle(Paint.Style.FILL);
        mPaintExplode.setStrokeWidth(determineLineWidth(paint, 0.66f));
    }

    private float determineLineWidth(@NonNull Paint paint, float factor) {
        float width = paint.getStrokeWidth();
        width = Math.min(width, MAX_LINE_WIDTH);
        width = Math.max(width, MIN_LINE_WIDTH);
        return width * factor;
    }

    /**
     * Set the text and text color for the explode text animation
     *
     * @param text  String to display during animation
     * @param color Color of the text
     */
    public void setText(@Nullable String text, int color) {
        mText = text;
        mPaintText = new Paint();
        mPaintText.setColor(color);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setAntiAlias(true);
    }

    @SuppressWarnings("unused")
    public void setRotationCount(int circuits) {
        mCircuits = circuits;
    }

    /**
     * Draw effect at current percentage
     *
     * @param canvas          Canvas to draw animation onto
     * @param bounds          bounds to use for drawing animation
     * @param percentComplete percentage that the animation is complete
     * @param startAngle      The initial angle the the arc starts from
     * @param sweepAngle      The total amount of angle for the View (360 for circle)
     */
    public void draw(@NonNull Canvas canvas, @NonNull RectF bounds, float percentComplete, float startAngle, float sweepAngle) {
        switch (mEffectType) {
            case EFFECT_SPIRAL_EXPLODE:
                final float step = 0.6f;
                if (percentComplete <= step) {
                    drawMoveToCenter(canvas, bounds, percentComplete * (1f / step), startAngle, sweepAngle);
                } else {
                    final float remain = (1.0f - step);
                    drawExplode(canvas, bounds, (percentComplete - step) / remain);
                    drawText(canvas, bounds, (percentComplete - step) / remain);
                }

                break;
            case EFFECT_EXPLODE:
                drawExplode(canvas, bounds, percentComplete);
                drawText(canvas, bounds, percentComplete);
                break;
            case EFFECT_SPIRAL_IN:
            case EFFECT_SPIRAL_OUT:
            case EFFECT_SPIRAL_OUT_FILL:
                drawMoveToCenter(canvas, bounds, percentComplete, startAngle, sweepAngle);
                break;
        }
    }

    /**
     * Animate the series in a spiral motion moving to of from the center of the bounds.
     * <p/>
     * If the EffectType is EffectType.EFFECT_SPIRAL_OUT_FILL the animation will continue after
     * reaching the start position and continue to fill out the complete track which is defined
     * as the startAngle + the sweep angle. This feature would generally be used to animate the
     * background track on its initial display.
     *
     * @param canvas          Canvas to draw animation onto
     * @param bounds          bounds to use for drawing animation
     * @param percentComplete percentage that the animation is complete
     * @param startAngle      The initial angle the the arc starts from
     * @param sweepAngle      The total amount of angle for the View. If this is a complete circle
     *                        this will be 360, or if it is an arc it will be < 360
     */
    public void drawMoveToCenter(@NonNull Canvas canvas, RectF bounds,
                                 float percentComplete, float startAngle, float sweepAngle) {

        // Animation moves outward from center to outside
        final boolean moveOutward = (mEffectType == EffectType.EFFECT_SPIRAL_OUT ||
                mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL);

        // Animation spins in a clockwise direction
        final boolean spinClockwise = mEffectType != EffectType.EFFECT_SPIRAL_IN &&
                mEffectType != EffectType.EFFECT_SPIRAL_EXPLODE;

        final float buffer = 10f;
        final float halfWidth = (bounds.width() / 2) - buffer;
        final float halfHeight = (bounds.height() / 2) - buffer;
        final float baseRotateAngle = mCircuits * 360f;

        float rotateAmount = (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL) ? baseRotateAngle + 360f : baseRotateAngle;
        float rotateOffset = (rotateAmount * percentComplete);
        float newAngle = (startAngle + (spinClockwise ? rotateOffset : -rotateOffset)) % 360;
        float sweep = getSweepAngle(percentComplete);

        mSpinBounds.set(bounds);

        float percent = percentComplete;

        if (moveOutward) {
            // Make the animation move outward by inverting the percentage complete
            percent = 1.0f - percentComplete;
        }

        if (mEffectType == EffectType.EFFECT_SPIRAL_OUT_FILL) {
            if ((rotateAmount * percentComplete) > (rotateAmount - 360f)) {
                mPaint.setStyle(Paint.Style.STROKE);
                sweep = (rotateAmount * percentComplete) % 360;
                if (sweep <= 0) {
                    sweep = 360;
                }

                // Cap the fill effect at the sweepAngle for non circular arcs
                if (sweep > sweepAngle) {
                    sweep = sweepAngle;
                }
                newAngle = startAngle;
            } else {
                float min = 1.0f - (baseRotateAngle / rotateAmount);
                if (percent > min) {
                    float adjustedPercentage = (percent - min) / (1.0f - min);
                    mSpinBounds.inset(halfWidth * adjustedPercentage,
                            halfHeight * adjustedPercentage);
                }
            }
        } else {
            // Restrict the bounds to move drawing closer to center of area
            mSpinBounds.inset(halfWidth * percent, halfHeight * percent);
        }

        canvas.drawArc(mSpinBounds,
                newAngle,
                sweep,
                false,
                mPaint);
    }

    private float getSweepAngle(float percentComplete) {
        final float sweepMax = 30f;
        final float sweepMin = 0.1f;

        if (percentComplete < 0.5) {
            return sweepMin + (sweepMax - sweepMin) * (percentComplete * 2);
        }
        return sweepMax - (sweepMax - sweepMin) * ((percentComplete - 0.5f) * 2);
    }

    /**
     * Animates the drawing of a text animation of size and alpha
     *
     * @param canvas          Canvas to draw text
     * @param percentComplete percent of the animation complete (0..1)
     */
    public void drawText(@NonNull Canvas canvas, RectF bounds, float percentComplete) {
        if (mText != null && mText.length() > 0) {
            mPaintText.setTextSize(100 * percentComplete);
            mPaintText.setAlpha(MAX_ALPHA);

            final float startFadePercent = 0.7f;
            if (percentComplete > startFadePercent) {
                int alphaText = (int) (MAX_ALPHA - (MAX_ALPHA * ((percentComplete - startFadePercent) / (1.0f - startFadePercent))));
                mPaintText.setAlpha(alphaText);
            }

            // Calculate a centered position for the text
            final float xPos = bounds.left + (bounds.width() / 2);
            final float yPos = (bounds.top + (bounds.height() / 2)) - ((mPaintText.descent() + mPaintText.ascent()) / 2);
            canvas.drawText(mText, xPos, yPos, mPaintText);
        }
    }

    /**
     * Creates an animation where X lines are created and move from the center of the bounds to
     * the outside of the bounds. As the near the edge an alpha fade is applied
     *
     * @param canvas          Canvas to draw effect onto
     * @param bounds          Area to perform the effect
     * @param percentComplete percentage of the animation that has been completed (0..1)
     */
    public void drawExplode(@NonNull Canvas canvas, RectF bounds, float percentComplete) {
        boolean drawCircles = Build.VERSION.SDK_INT <= 17;
        final float maxLength = (bounds.width() * EXPLODE_LINE_MAX);
        final float minLength = (bounds.width() * EXPLODE_LINE_MIN);
        final float startPosition = (bounds.width() * EXPLODE_LINE_MAX);
        int alpha = MAX_ALPHA;

        float length;
        if (percentComplete > 0.5f) {
            float completed = ((percentComplete - 0.5f) * 2);
            length = maxLength - (completed * (maxLength - minLength));
            alpha = MAX_ALPHA - (int) (MAX_ALPHA * completed);
        } else {
            length = minLength + ((percentComplete * 2) * (maxLength - minLength));
        }

        final int initialAlpha = mPaint.getAlpha();
        if (alpha < MAX_ALPHA) {
            mPaintExplode.setAlpha((int) (initialAlpha * (alpha / (float) MAX_ALPHA)));
        }

        float radiusEnd = startPosition + (int) (((bounds.width() / 2) - startPosition) * percentComplete);
        float radiusStart = radiusEnd - length;

        float angleInDegrees = 0;
        for (int i = 0; i < EXPLODE_LINE_COUNT; i++) {
            drawExplodeLine(canvas, bounds, radiusStart, radiusEnd, angleInDegrees, percentComplete, drawCircles);
            angleInDegrees += (360f / EXPLODE_LINE_COUNT);
        }

        if (alpha < MAX_ALPHA) {
            mPaint.setAlpha(initialAlpha);
        }
    }

    private void drawExplodeLine(@NonNull Canvas canvas, RectF bounds,
                                 float radiusStart, float radiusEnd, float angleInDegrees,
                                 float percentComplete, boolean compatMode) {
        float startX = (radiusStart * (float) Math.cos(angleInDegrees * Math.PI / 180F)) + bounds.centerX();
        float startY = (radiusStart * (float) Math.sin(angleInDegrees * Math.PI / 180F)) + bounds.centerY();
        float endX = (radiusEnd * (float) Math.cos(angleInDegrees * Math.PI / 180F)) + bounds.centerX();
        float endY = (radiusEnd * (float) Math.sin(angleInDegrees * Math.PI / 180F)) + bounds.centerY();

        if (!compatMode) {
            canvas.drawLine(startX, startY, endX, endY, mPaintExplode);
        } else {
            // Bug on older Android versions where drawLine does not apply round cap when
            // using drawLine when hardware acceleration is enabled. In this case
            // we just draw a circle instead
            float radius = (bounds.width() * EXPLODE_CIRCLE_MIN) + ((bounds.width() * EXPLODE_CIRCLE_MAX - bounds.width() * EXPLODE_CIRCLE_MIN) * percentComplete);
            canvas.drawCircle(endX, endY, radius, mPaintExplode);
        }
    }

    /**
     * Type of effect to display
     */
    public enum EffectType {
        EFFECT_SPIRAL_OUT_FILL, /* Fill track after outward spiral animation */
        EFFECT_SPIRAL_OUT, /* Animation from center to outside in spiral motion */
        EFFECT_SPIRAL_IN, /* Animation from outside to center in spiral motion */
        EFFECT_EXPLODE, /* Explode animation where several lines are produced from center */
        EFFECT_SPIRAL_EXPLODE /* Combines EFFECT_SPIRAL_IN and EFFECT_EXPLODE */
    }
}