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
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * ChartSeries is the implementation of one series of data.
 * A {@link DecoView} can have one or more DataSeries
 */
abstract public class ChartSeries {
    /**
     * Minimum sweep angle. We need this to be greater than 0 as we want something drawn
     * even the data value is 0
     */
    static final private float MIN_SWEEP_ANGLE = 0.1f;
    static final private float MIN_SWEEP_ANGLE_FLAT = 0.1f;
    static final private float MIN_SWEEP_ANGLE_NONE = 0f;
    static final private float MIN_SWEEP_ANGLE_PIE = MIN_SWEEP_ANGLE_NONE;

    @SuppressWarnings("unused")
    protected final String TAG = getClass().getSimpleName();
    /**
     * ArcItem attributes to be drawn
     */
    protected final SeriesItem mSeriesItem;
    /**
     * Current Mode of drawing
     */
    protected DecoEvent.EventType mDrawMode;
    /**
     * Current Effect being executed (if any)
     */
    protected DecoDrawEffect mEffect;
    /**
     * Positions for current animation
     */
    protected float mPositionStart = 0;
    protected float mPositionEnd = 0;
    protected float mPositionCurrentEnd = 0;
    /**
     * 0..1.0f The percentage of the animation complete
     */
    protected float mPercentComplete = 1.0f;
    /**
     * Drawing bounds for arc
     */
    protected RectF mBounds;
    /**
     * Drawing bounds for arc after inset applied
     */
    protected RectF mBoundsInset;
    /**
     * Angle of drawing point origin.
     */
    protected int mAngleStart = 180;
    /**
     * Number of degrees to sweep. 360 for complete circle
     */
    protected int mAngleSweep = 360;
    /**
     * Paint used for drawing arc
     */
    protected Paint mPaint;
    /**
     * Arc visible or hidden
     */
    private boolean mVisible;
    /**
     * ValueAnimator to calculate arc drawing position during animation
     */
    private ValueAnimator mValueAnimator;

    private ColorAnimate mColorAnimate;

    /**
     * Current event being processed. Kept for the case where we pause and resume the event
     */
    private DecoEvent mEventCurrent;

    /**
     * Has the current move animation been paused
     */
    private boolean mIsPaused = false;

    /**
     * Construct an ArcSeries based on the ArcItem attributes and the angle and shape
     * of the arc
     *
     * @param seriesItem  Arc attributes to apply
     * @param totalAngle  Angle of the total arc area in degrees
     * @param rotateAngle number of degrees to rotate the start position
     *                    <p/>
     *                    No Access Modifier for the constructor is specified. This is deliberate so we use the
     *                    default package access. This class should not be constructed outside of the package scope.
     *                    Clients of this library need only to construct an {@link ChartSeries} and pass that to the
     *                    DynamicArcView to have a new ArcSeries created
     */
    ChartSeries(@NonNull SeriesItem seriesItem, int totalAngle, int rotateAngle) {
        mSeriesItem = seriesItem;
        mVisible = seriesItem.getInitialVisibility();
        setupView(totalAngle, rotateAngle);
        reset();
    }

    /**
     * Configure the view for the given angles. Set the total angle of the arc and also
     * pass an offset angle to change the initial drawing location
     *
     * @param totalAngle  The total angle of the Arc...360 is a circle
     * @param rotateAngle The angle to rotate the drawing start position
     */
    public void setupView(int totalAngle, int rotateAngle) {
        if (totalAngle < 0 || totalAngle > 360) {
            throw new IllegalArgumentException("Total angle of view must be in the range 0..360");
        }
        if (rotateAngle < 0 || rotateAngle > 360) {
            throw new IllegalArgumentException("Rotate angle of view must be in the range 0..360");
        }

        mAngleStart = rotateAngle;
        mAngleSweep = totalAngle;

        /**
         * If we are spinning anti-clockwise we need the start to be where the finish would
         * normally be
         */
        if (!mSeriesItem.getSpinClockwise()) {
            mAngleStart = (mAngleStart + mAngleSweep) % 360;
        }
        mBounds = null;
    }

    public SeriesItem getSeriesItem() {
        return mSeriesItem;
    }

    /**
     * Create the animation of filling the chart by using a valueAnimator to adjust values
     */
    public void startAnimateMove(@NonNull final DecoEvent event) {
        mIsPaused = false;
        mDrawMode = event.getEventType();
        mVisible = true;

        cancelAnimation();
        mEventCurrent = event;

        final boolean changeColors = event.isColorSet();
        if (changeColors) {
            mColorAnimate = new ColorAnimate(mSeriesItem.getColor(), event.getColor());
            mSeriesItem.setColor(event.getColor());
        }
        float position = event.getEndPosition();


        event.notifyStartListener();

        mPositionStart = mPositionCurrentEnd;
        mPositionEnd = position;

        long animationDuration = event.getEffectDuration();

        if ((animationDuration == 0) || (Math.abs(mPositionEnd - mPositionStart) < 0.01)) {
            cancelAnimation();
            mPositionCurrentEnd = mPositionEnd;
            mEventCurrent = null;
            mPercentComplete = 1.0f;
            for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                seriesItemListener.onSeriesItemAnimationProgress(1.0f, mPositionEnd);
            }
            event.notifyEndListener();
            return;
        }

        if (animationDuration < 0) {
            /**
             * If an animation duration is not set we calculate it using a formula of the proportion
             * of a revolution multiplied by the default time for a full revolution. This ensures
             * that the speed of the move is consistent for all ranges
             */
            animationDuration = (Math.abs((int) (mSeriesItem.getSpinDuration() *
                    ((mPositionStart - mPositionEnd) / mSeriesItem.getMaxValue()))));

        }

        mValueAnimator = ValueAnimator.ofFloat(mPositionStart, position);
        mValueAnimator.setDuration(animationDuration);

        /**
         * Note: When setting the Interpolator the default is
         * {@link android.view.animation.AccelerateDecelerateInterpolator}
         *
         * However, if you call setInterpolator(null) then the default is not used, rather:
         * {@link LinearInterpolator}
         */
        if (event.getInterpolator() != null) {
            mValueAnimator.setInterpolator(event.getInterpolator());
        } else {
            if (mSeriesItem.getInterpolator() != null) {
                mValueAnimator.setInterpolator(mSeriesItem.getInterpolator());
            }
        }

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float current = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                mPercentComplete = (current - mPositionStart) / (mPositionEnd - mPositionStart);
                mPositionCurrentEnd = current;

                /**
                 * Notify the listeners of position update. This will be the OrbView itself and
                 * possibly the user who is using a listener to update the progress in an alternative
                 * manner, ie. displaying text progress %
                 */
                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemAnimationProgress(mPercentComplete, mPositionCurrentEnd);
                }
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (changeColors) {
                    mColorAnimate = null;
                }

                event.notifyEndListener();
            }
        });

        mValueAnimator.start();
    }

    /**
     * If we are currently animating we stop it first before starting a new
     * animation from the current position
     */
    public void cancelAnimation() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mEventCurrent = null;

        if (mColorAnimate != null) {
            mPaint.setColor(mSeriesItem.getColor());
            mColorAnimate = null;
        }
    }

    /**
     * Kick off an animation to hide or show the arc. This results in an animation where
     * both the width of the line used for the arc and the transparency of the arc is
     * altered over the duration provided
     *
     * @param event   Event to process
     * @param showArc True to show the arc, false to hide
     */
    public void startAnimateHideShow(@NonNull final DecoEvent event, final boolean showArc) {
        cancelAnimation();
        event.notifyStartListener();


        mDrawMode = event.getEventType();
        mPercentComplete = showArc ? 1.0f : 0f;
        mVisible = true;

        final float maxValue = 1.0f;
        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);

        mValueAnimator.setDuration(event.getEffectDuration());
        mValueAnimator.setInterpolator(new LinearInterpolator());

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float current = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                mPercentComplete = showArc ? (maxValue - current) : current;

                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (event.getEventType() != DecoEvent.EventType.EVENT_EFFECT) {
                    event.notifyEndListener();
                }
            }
        });

        mValueAnimator.start();
    }

    /**
     * Animate change of color
     *
     * @param event Event to process
     */
    public void startAnimateColorChange(@NonNull final DecoEvent event) {
        cancelAnimation();
        event.notifyStartListener();
        mVisible = true;

        mDrawMode = event.getEventType();
        mPercentComplete = 0f;

        final boolean changeColors = event.isColorSet();
        if (changeColors) {
            mColorAnimate = new ColorAnimate(mSeriesItem.getColor(), event.getColor());
            mSeriesItem.setColor(event.getColor());
        } else {
            Log.w(TAG, "Must set new color to start CHANGE_COLOR event");
            return;
        }

        final float maxValue = 1.0f;
        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);

        mValueAnimator.setDuration(event.getEffectDuration());
        if (event.getInterpolator() != null) {
            mValueAnimator.setInterpolator(event.getInterpolator());
        } else {
            mValueAnimator.setInterpolator(new LinearInterpolator());
        }

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPercentComplete = Float.valueOf(valueAnimator.getAnimatedValue().toString());

                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                event.notifyEndListener();
            }
        });

        mValueAnimator.start();
    }

    /**
     * Execute an Animation effect by starting the Value Animator
     *
     * @param event Event to process effect
     * @throws IllegalStateException No effect set in event
     */
    public void startAnimateEffect(@NonNull final DecoEvent event)
            throws IllegalStateException {
        if (event.getEffectType() == null) {
            throw new IllegalStateException("Unable to execute null effect type");
        }

        // All effects run from 0.0 .. 1.0f in duration
        final float maxValue = 1.0f;

        cancelAnimation();
        event.notifyStartListener();

        mVisible = true;
        mDrawMode = event.getEventType();
        mEffect = new DecoDrawEffect(event.getEffectType(), mPaint, event.getDisplayText());
        mEffect.setRotationCount(event.getEffectRotations());

        mPercentComplete = 0f;

        mValueAnimator = ValueAnimator.ofFloat(0, maxValue);
        mValueAnimator.setDuration(event.getEffectDuration());
        Interpolator interpolator = (event.getInterpolator() != null) ? event.getInterpolator() : new LinearInterpolator();
        mValueAnimator.setInterpolator(interpolator);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPercentComplete = Float.valueOf(valueAnimator.getAnimatedValue().toString());
                for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
                    seriesItemListener.onSeriesItemDisplayProgress(mPercentComplete);
                }
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                event.notifyEndListener();
                mDrawMode = DecoEvent.EventType.EVENT_MOVE;
                mVisible = mEffect.postExecuteVisibility();
                mEffect = null;
            }
        });

        mValueAnimator.start();
    }

    /**
     * Reset the arc back to the initial values and cancel any current animations
     */
    public void reset() {
        mDrawMode = DecoEvent.EventType.EVENT_MOVE;
        mVisible = mSeriesItem.getInitialVisibility();

        cancelAnimation();

        mPositionStart = mSeriesItem.getMinValue();
        mPositionEnd = mSeriesItem.getInitialValue();
        mPositionCurrentEnd = mSeriesItem.getInitialValue();
        mPercentComplete = 1.0f;

        mPaint = new Paint();
        mPaint.setColor(mSeriesItem.getColor());
        mPaint.setStyle((mSeriesItem.getChartStyle() == SeriesItem.ChartStyle.STYLE_DONUT) ? Paint.Style.STROKE : Paint.Style.FILL);
        mPaint.setStrokeWidth(mSeriesItem.getLineWidth());
        mPaint.setStrokeCap(mSeriesItem.getRoundCap() ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        mPaint.setAntiAlias(true);

        // We need to reset the bounds for the case we are drawing a gradient and need to recreate
        // based on the bounds
        mBounds = null;

        for (SeriesItem.SeriesItemListener seriesItemListener : mSeriesItem.getListeners()) {
            seriesItemListener.onSeriesItemAnimationProgress(mPercentComplete, mPositionCurrentEnd);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public RectF drawLabel(Canvas canvas, RectF bounds, float anglePercent) {

        if (!mVisible) {
            return null;
        }

        if (bounds == null || bounds.isEmpty()) {
            throw new IllegalArgumentException("Drawing bounds can not be null or empty");
        }
        if (mSeriesItem.getSeriesLabel() != null) {
            return mSeriesItem.getSeriesLabel().draw(canvas, bounds, anglePercent, getPositionPercent(), mPositionCurrentEnd);
        }
        return null;
    }

    /**
     * Draw this series in the current position calculated by the ValueAnimator.
     *
     * @param canvas Canvas used to draw
     * @param bounds Bounds to be used to draw the arc
     * @return true if drawing has already been handled
     */
    public boolean draw(Canvas canvas, RectF bounds) {
        if (!mVisible) {
            return true;
        }

        if (bounds == null || bounds.isEmpty()) {
            throw new IllegalArgumentException("Drawing bounds can not be null or empty");
        }

        processBoundsChange(bounds);

        if (mDrawMode == DecoEvent.EventType.EVENT_EFFECT) {
            // Delegate the drawing to the ArcEffect as required
            if (mEffect != null) {
                mEffect.draw(canvas, mBoundsInset, mPercentComplete, mAngleStart, mAngleSweep);
            }
            return true;
        }

        processRevealEffect();

        if (mColorAnimate != null) {
            mPaint.setColor(mColorAnimate.getColorCurrent(mPercentComplete));
        } else if (mPaint.getColor() != getSeriesItem().getColor()) {
            mPaint.setColor(getSeriesItem().getColor());
        }

        return false;
    }

    /**
     * Adjust the sweep value if the direction of the arc is not being drawn in a
     * clockwise direction
     *
     * @param sweep degrees to sweep when drawing arc
     * @return new sweep degrees adjusted for direction
     */
    protected float adjustSweepDirection(float sweep) {
        return mSeriesItem.getSpinClockwise() ? sweep : -sweep;
    }

    /**
     * Adjust the starting point for drawing the arc
     *
     * @param sweep degrees
     * @return adjusted angle
     */
    protected float adjustDrawPointAngle(float sweep) {
        return (mAngleStart + (sweep - getMinSweepAngle())) % 360;

    }

    /**
     * Adjust the line width used when the hide animation is taking place. This will reduce the
     * line width from the original width to nothing over time. At the same this the alpha of the
     * line will be reduced to nothing
     */
    protected void processRevealEffect() {
        if ((mDrawMode != DecoEvent.EventType.EVENT_HIDE) &&
                (mDrawMode != DecoEvent.EventType.EVENT_SHOW)) {
            return;
        }

        float lineWidth = mSeriesItem.getLineWidth();
        if (mPercentComplete > 0) {
            lineWidth *= (1.0f - mPercentComplete);
            mPaint.setAlpha((int) (Color.alpha(mSeriesItem.getColor()) * (1.0f - mPercentComplete)));
        } else {
            mPaint.setAlpha(Color.alpha(mSeriesItem.getColor()));
        }

        mPaint.setStrokeWidth(lineWidth);
    }

    /**
     * Adjusts the gradient used for the chart series to set the shader used in the paint. This is only done
     * if the line contains two colors and the bounds of the line has changed since it was last set.
     *
     * @param bounds The bounds used to draw the chart
     */
    protected void processBoundsChange(final RectF bounds) {
        if (mBounds == null || !mBounds.equals(bounds)) {
            mBounds = new RectF(bounds);
            mBoundsInset = new RectF(bounds);
            if (mSeriesItem.getInset() != null) {
                mBoundsInset.inset(mSeriesItem.getInset().x, mSeriesItem.getInset().y);
            }
            applyGradientToPaint();
        }
    }

    /**
     * Build a gradient if required. This will be executed every time the bounds changed. Subclasses
     * must implement this method to create a gradient that will work with the given shape
     */
    abstract protected void applyGradientToPaint();

    /**
     * Even if we have 0 we want to show a marker so the sweep angle needs to be > 0
     *
     * @param angle sweep angle to draw the arc
     * @return sweep angle after adjustment (if required)
     */
    protected float verifyMinSweepAngle(final float angle) {
        return (Math.abs(angle) < getMinSweepAngle() &&
                getSeriesItem().showPointWhenEmpty())
                ? getMinSweepAngle() : angle;
    }

    /**
     * Calculates the current position of an series based on the progress of the animation
     * being executed
     *
     * @return Current percentage to fill chart (0 .. 1.0f)
     */
    protected float calcCurrentPosition(float start, float end, float min, float max, float percent) {
        start -= min;
        end -= min;
        max -= min;

        if (Math.abs(start - end) < 0.01) {
            return (start / max);
        }

        if ((mDrawMode == DecoEvent.EventType.EVENT_HIDE) ||
                (mDrawMode == DecoEvent.EventType.EVENT_SHOW) ||
                (mDrawMode == DecoEvent.EventType.EVENT_COLOR_CHANGE)) {
            // When revealing we are not animating the movement, but animating the size and
            // transparency, so treat this as if it is 100% complete already
            percent = 1.0f;
        }

        if (Math.abs(end) < 0.01) {
            return (start / max) * (start - (start * percent)) / start;
        }

        return (end / max) * (start + (percent * (end - start))) / end;
    }

    /**
     * Determine the minimum sweep angle that should be allowed for the current settings. If the
     * sweep is 360 a complete circle is drawn, if the sweep is 0 nothing is drawn. In some
     * states even if the data is say 0 out of 100 and mathematically the sweep angle is 0 we
     * want to draw a point anyway which is why this function exists
     *
     * @return minimum number of degrees allowed in the current state
     */
    protected float getMinSweepAngle() {
        if (!mSeriesItem.showPointWhenEmpty()) {
            return MIN_SWEEP_ANGLE_NONE;
        }

        if (mSeriesItem.getChartStyle() == SeriesItem.ChartStyle.STYLE_PIE) {
            return MIN_SWEEP_ANGLE_PIE;
        }

        if (mPaint.getStrokeCap() == Paint.Cap.ROUND) {
            return MIN_SWEEP_ANGLE;
        }

        return MIN_SWEEP_ANGLE_FLAT;
    }

    /**
     * Calculate the percentage filled the series is at the current position. eg if the series
     * runs from (empty) 100 to 200 (full) and the current position is set to 175, then the
     * current position percent is 75%. This should not be confused with the current animation
     * percentage, which is the progress of the current operation, say moving from 100 to 110 may
     * be 50% complete at 105.
     *
     * @return Current percentage that the chart is filled
     */
    public float getPositionPercent() {
        return mPositionCurrentEnd / (mSeriesItem.getMaxValue() - mSeriesItem.getMinValue());
    }

    /**
     * Is the series currently visible
     *
     * @return true if visible
     */
    public boolean isVisible() {
        return mVisible;
    }

    /**
     * Pause any move animation currently in progress
     */
    public boolean pause() {
        if (mValueAnimator != null && mValueAnimator.isRunning() && !mIsPaused) {
            mValueAnimator.cancel();
            mIsPaused = true;
            return true;
        }
        return false;
    }

    /**
     * Resume a previously paused move animation
     *
     * @return true if move resumed
     */
    public boolean resume() {
        if (isPaused()) {
            startAnimateMove(mEventCurrent);
            return true;
        }
        return false;
    }

    /**
     * Is the current animated move paused
     *
     * @return true if paused
     */
    public boolean isPaused() {
        return mIsPaused;
    }

    /**
     * Force move of current position without animation
     *
     * @param position value to move current position to
     */
    public void setPosition(float position) {
        mPositionStart = position;
        mPositionEnd = position;
        mPositionCurrentEnd = position;
        mPercentComplete = 1.0f;
    }
}