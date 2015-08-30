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
package com.hookedonplay.decoviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hookedonplay.decoviewlib.charts.ChartSeries;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.LineArcSeries;
import com.hookedonplay.decoviewlib.charts.LineSeries;
import com.hookedonplay.decoviewlib.charts.PieSeries;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEventManager;
import com.hookedonplay.decoviewlib.util.GenericFunctions;

import java.util.ArrayList;

/**
 * Android Custom View for displaying animated Arc based charts
 */
@SuppressWarnings("unused")
public class DecoView extends View implements DecoEventManager.ArcEventManagerListener {
    private final String TAG = getClass().getSimpleName();
    /**
     * Gravity settings
     */
    private VertGravity mVertGravity = VertGravity.GRAVITY_VERTICAL_CENTER;
    private HorizGravity mHorizGravity = HorizGravity.GRAVITY_HORIZONTAL_CENTER;
    /**
     * List of arcs to draw for this view. Generally this will be 1 series for the background arc
     * and then 1 or more for the data being presented
     */
    private ArrayList<ChartSeries> mChartSeries;
    /**
     * Width/Height of the view
     */
    private int mCanvasWidth = -1;
    private int mCanvasHeight = -1;
    /**
     * Bounds for drawing the arcs
     */
    private RectF mArcBounds;
    /**
     * The default line width used for the arcs
     */
    private float mDefaultLineWidth = 30;
    /**
     * RotateAngle adjusts the angle of the start point for drawing. It should be noted that the
     * behavior is different based on if the arc is a full circle or a part circle. If it is a
     * full circle the default position starts at 270 degrees while if it is a part circle the
     * default start point is 90 degrees. This is by design to provide the most common positions
     * as the defaults
     */
    private int mRotateAngle = 0;
    /**
     * Total angle of the orb. 360 = full circle, < 360 horseshoe/arc shape
     */
    private int mTotalAngle = 360;
    /**
     * Event manager that controls the timing of events to be executed on the
     * {@link DecoView}
     */
    private DecoEventManager mDecoEventManager;
    private float mMeasureViewableArea[];

    public DecoView(Context context) {
        super(context);
        initView();
    }

    public DecoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DecoView,
                0, 0);

        int rotateAngle = 0;
        try {
            mDefaultLineWidth = a.getDimension(R.styleable.DecoView_dv_lineWidth, 30f);
            rotateAngle = a.getInt(R.styleable.DecoView_dv_rotateAngle, 0);
            mTotalAngle = a.getInt(R.styleable.DecoView_dv_totalAngle, 360);
            mVertGravity = VertGravity.values()[a.getInt(R.styleable.DecoView_dv_arc_gravity_vertical, VertGravity.GRAVITY_VERTICAL_CENTER.ordinal())];
            mHorizGravity = HorizGravity.values()[a.getInt(R.styleable.DecoView_dv_arc_gravity_horizontal, HorizGravity.GRAVITY_HORIZONTAL_CENTER.ordinal())];
        } finally {
            a.recycle();
        }

        configureAngles(mTotalAngle, rotateAngle);

        initView();
    }

    public DecoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * Alter the total degrees of the ArcView and applies a rotation angle to change the start
     * position. If this is 360 then the view is a full circle. 270 degrees is 3/4 of a circle
     *
     * @param totalAngle  Total angle of the view in degrees
     * @param rotateAngle Number of degrees to rotate the start position
     */
    public void configureAngles(int totalAngle, int rotateAngle) {
        if (totalAngle <= 0) {
            throw new IllegalArgumentException("Total angle of the arc must be > 0");
        }
        final int circleStartPosition = 270;
        final int arcStartPosition = 90;
        final int degreesInCircle = 360;

        mTotalAngle = totalAngle;
        mRotateAngle = (circleStartPosition + rotateAngle) % degreesInCircle;

        if (mTotalAngle < degreesInCircle) {
            mRotateAngle = ((arcStartPosition + (degreesInCircle - totalAngle) / 2) + rotateAngle) % degreesInCircle;
        }

        if (mChartSeries != null) {
            for (ChartSeries chartSeries : mChartSeries) {
                chartSeries.setupView(mTotalAngle, mRotateAngle);
            }
        }
    }

    private void initView() {
        GenericFunctions.initialize(getContext());
        enableCompatibilityMode();
        createVisualEditorTrack();
    }

    /**
     * Retrieve event manager for delayed events
     *
     * @return event manager
     */
    private DecoEventManager getEventManager() {
        if (mDecoEventManager == null) {
            mDecoEventManager = new DecoEventManager(this);
        }
        return mDecoEventManager;
    }

    /**
     * Determines if any arcs have been added to the view
     *
     * @return true if one or more arcs have been added to the view
     */
    public boolean isEmpty() {
        return mChartSeries == null || mChartSeries.isEmpty();
    }

    /**
     * Add a new item to the ArcView. An ArcView may have any number of arcs
     *
     * @param seriesItem orb item attributes
     * @return index into orb item list
     */
    public int addSeries(@NonNull SeriesItem seriesItem) {
        if (mChartSeries == null) {
            mChartSeries = new ArrayList<>();
        }

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {

            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                invalidate();
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {
                invalidate();
            }
        });

        if (seriesItem.getLineWidth() < 0) {
            seriesItem.setLineWidth(mDefaultLineWidth);
        }

        ChartSeries chartSeries;
        switch (seriesItem.getChartStyle()) {
            case STYLE_DONUT:
                chartSeries = new LineArcSeries(seriesItem, mTotalAngle, mRotateAngle);
                break;
            case STYLE_PIE:
                chartSeries = new PieSeries(seriesItem, mTotalAngle, mRotateAngle);
                break;
            case STYLE_LINE_HORIZONTAL:
            case STYLE_LINE_VERTICAL:
                Log.w(TAG, "STYLE_LINE_* is currently experimental");
                LineSeries lineSeries = new LineSeries(seriesItem, mTotalAngle, mRotateAngle);
                lineSeries.setHorizGravity(mHorizGravity);
                lineSeries.setVertGravity(mVertGravity);
                chartSeries = lineSeries;
                break;
            default:
                throw new IllegalStateException("Chart Style not implemented");
        }
        mChartSeries.add(mChartSeries.size(), chartSeries);
        mMeasureViewableArea = new float[mChartSeries.size()];

        recalcLayout();
        return mChartSeries.size() - 1;
    }

    /**
     * When displaying this view in the visual design editor we just mock up a background
     * series. This will not be executed when your app is run
     */
    private void createVisualEditorTrack() {
        if (isInEditMode()) {
            addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                    .setRange(0, 100, 100)
                    .setLineWidth(mDefaultLineWidth)
                    .build());
            addSeries(new SeriesItem.Builder(Color.argb(255, 255, 64, 64))
                    .setRange(0, 100, 25)
                    .setLineWidth(mDefaultLineWidth)
                    .build());

        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mCanvasWidth = width;
        mCanvasHeight = height;

        recalcLayout();
    }

    /**
     * Calculate the bounds based on the size of the view and the maximum width of any of the
     * ArcSeries. Must be called when:
     * <p/>
     * (a) OnSizeChanged() is called
     * (b) A new series of data is added
     */
    private void recalcLayout() {
        if (mCanvasWidth <= 0 || mCanvasHeight <= 0) {
            return;
        }

        float offsetLineWidth = getWidestLine() / 2;
        float offsetX = 0;
        float offsetY = 0;

        if (mCanvasWidth != mCanvasHeight) {
            if (mCanvasWidth > mCanvasHeight) {
                offsetX = (mCanvasWidth - mCanvasHeight) / 2;
            } else {
                offsetY = (mCanvasHeight - mCanvasWidth) / 2;
            }
        }

        if (mVertGravity == VertGravity.GRAVITY_VERTICAL_FILL) {
            offsetY = 0;
        }

        if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_FILL) {
            offsetX = 0;
        }
        /**
         * Respect the padding of the view and ensure we have at least that amount of
         * space on each edge
         */
        float paddingLeft = offsetX + getPaddingLeft();
        float paddingTop = offsetY + getPaddingTop();
        float paddingRight = offsetX + getPaddingRight();
        float paddingBottom = offsetY + getPaddingBottom();

        mArcBounds = new RectF(offsetLineWidth + paddingLeft,
                offsetLineWidth + paddingTop,
                mCanvasWidth - offsetLineWidth - paddingRight,
                mCanvasHeight - offsetLineWidth - paddingBottom);

        if (mVertGravity == VertGravity.GRAVITY_VERTICAL_TOP) {
            mArcBounds.offset(0, -offsetY);
        } else if (mVertGravity == VertGravity.GRAVITY_VERTICAL_BOTTOM) {
            mArcBounds.offset(0, offsetY);
        }

        if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_LEFT) {
            mArcBounds.offset(-offsetX, 0);
        } else if (mHorizGravity == HorizGravity.GRAVITY_HORIZONTAL_RIGHT) {
            mArcBounds.offset(offsetX, 0);
        }
    }

    /**
     * find the width of the widest line used for any of the series of data
     *
     * @return widest arc line
     */
    private float getWidestLine() {
        if (mChartSeries == null) {
            return 0;
        }

        float widest = 0;
        for (ChartSeries chartSeries : mChartSeries) {
            widest = Math.max(chartSeries.getSeriesItem().getLineWidth(), widest);
        }
        return widest;
    }

    /**
     * Drawing routine that is called automatically when the view needs to be redrawn
     *
     * @param canvas the canvas on which the view will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mArcBounds == null || mArcBounds.isEmpty()) {
            return;
        }

        if (mChartSeries != null) {
            boolean labelsSupported = true;
            for (int i = 0; i < mChartSeries.size(); i++) {
                ChartSeries chartSeries = mChartSeries.get(i);
                chartSeries.draw(canvas, mArcBounds);
                // labels Unsupported if one or more series run anticlockwise
                labelsSupported &= (!chartSeries.isVisible() || chartSeries.getSeriesItem().getSpinClockwise());
                mMeasureViewableArea[i] = getLabelPosition(i);
            }

            // Draw the labels as a second pass as we want all labels to be on top of all
            // series data
            if (labelsSupported) {
                for (int i = 0; i < mMeasureViewableArea.length; i++) {
                    if (mMeasureViewableArea[i] >= 0f) {
                        ChartSeries chartSeries = mChartSeries.get(i);
                        chartSeries.drawLabel(canvas, mArcBounds, mMeasureViewableArea[i]);
                        //TODO: Keep bounds of all labels and don't allow overlap
                    }
                }
            }
        }
    }

    /**
     * Determine where a label should be displayed given its position and the position of all
     * other data series
     *
     * @param index position of index in {@link #mChartSeries} array
     * @return < 0 if label not visible, else 0f .. 1.0f to indicate position on circle
     */
    private float getLabelPosition(final int index) {
        float max = 0.0f;

        ChartSeries chartSeries = mChartSeries.get(index);
        // We only need to check those series drawn after this series
        for (int i = index + 1; i < mChartSeries.size(); i++) {
            ChartSeries innerSeries = mChartSeries.get(i);
            if (innerSeries.isVisible()) {
                if (max < innerSeries.getPositionPercent()) {
                    max = innerSeries.getPositionPercent();
                }
            }
        }

        if (max < chartSeries.getPositionPercent()) {
            // Adjust for incomplete circles
            float adjusted = ((chartSeries.getPositionPercent() + max) / 2) * ((float) mTotalAngle / 360f);

            // Adjust for rotation of start point
            float adjust = adjusted + (((float) mRotateAngle + 90f) / 360f);

            // Normalize
            while (adjust > 1.0f) {
                adjust -= 1.0f;
            }
            return adjust;
        }
        return -1f;
    }

    /**
     * Execute a move event
     *
     * @param event Event to execute
     */
    private void executeMove(@NonNull DecoEvent event) {
        if ((event.getEventType() != DecoEvent.EventType.EVENT_MOVE) &&
                (event.getEventType() != DecoEvent.EventType.EVENT_COLOR_CHANGE)) {
            return;
        }

        if (mChartSeries != null) {
            if (mChartSeries.size() <= event.getIndexPosition()) {
                throw new IllegalArgumentException("Invalid index: Position out of range (Index: " + event.getIndexPosition() + " Series Count: " + mChartSeries.size() + ")");
            }

            final int index = event.getIndexPosition();
            if (index >= 0 && index < mChartSeries.size()) {
                ChartSeries item = mChartSeries.get(event.getIndexPosition());
                if (event.getEventType() == DecoEvent.EventType.EVENT_COLOR_CHANGE) {
                    item.startAnimateColorChange(event);
                } else {
                    item.startAnimateMove(event);
                }
            } else {
                Log.e(TAG, "Ignoring move request: Invalid array index. Index: " + index + " Size: " + mChartSeries.size());
            }
        }
    }

    /**
     * Add an event to the DynamicArcViews {@link DecoEventManager} for processing. This can be
     * executed immediately or if the event has a {@link DecoEvent#mDelay} set then it will be
     * executed at a future time.
     * <p/>
     * When this event is to be executed the {@link DecoEventManager.ArcEventManagerListener#onExecuteEventStart(DecoEvent)}
     * callback will be executed
     * <p/>
     * To create an event see {@link DecoEvent.Builder}
     *
     * @param event Event to be processed
     */
    public void addEvent(@NonNull DecoEvent event) {
        getEventManager().add(event);
    }

    /**
     * Basic wrapper function to create an event with all defaults for the arc and simply execute
     * a move for the current position of the arc. If you want to customize the move (such as delay,
     * speed, interpolator...) then you need to use create an {@link DecoEvent} and call
     * {@link #addEvent(DecoEvent)}
     *
     * @param index    index of the arc series to apply the move
     * @param position position of the arc
     */
    public void moveTo(int index, float position) {
        addEvent(new DecoEvent.Builder(position).setIndex(index).build());
    }

    /**
     * Basic wrapper function to create an event with all defaults for the arc and simply execute
     * a move for the current position of the arc. If you want to customize the move (such as delay,
     * speed, interpolator...) then you need to use create an {@link DecoEvent} and call
     * {@link #addEvent(DecoEvent)}
     * <p/>
     * This function will not create a {@link DecoEvent} if you pass 0 as the duration
     *
     * @param index    index of the arc series to apply the move
     * @param position position of the arc
     * @param duration duration of the move
     */
    public void moveTo(int index, float position, int duration) {
        if (duration == 0) {
            getChartSeries(index).setPosition(position);
            invalidate();
            return;
        }
        addEvent(new DecoEvent.Builder(position).setIndex(index).setDuration(duration).build());
    }

    /**
     * Reset all arcs back to the start positions and remove all queued events
     */
    public void executeReset() {
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }

        if (mChartSeries != null) {
            for (ChartSeries chartSeries : mChartSeries) {
                chartSeries.reset();
            }
        }
    }

    /**
     * Remove all scheduled events and all data series
     */
    public void deleteAll() {
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }

        mChartSeries = null;
    }

    /**
     * Process event reveal as required
     *
     * @param event DecoEvent to process
     * @return true if handled
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean executeReveal(@NonNull DecoEvent event) {
        if ((event.getEventType() != DecoEvent.EventType.EVENT_SHOW) &&
                (event.getEventType() != DecoEvent.EventType.EVENT_HIDE)) {
            return false;
        }

        if (event.getEventType() == DecoEvent.EventType.EVENT_SHOW) {
            setVisibility(View.VISIBLE);
        }

        if (mChartSeries != null) {
            for (int i = 0; i < mChartSeries.size(); i++) {
                if ((event.getIndexPosition() == i) || (event.getIndexPosition() < 0)) {
                    ChartSeries chartSeries = mChartSeries.get(i);
                    chartSeries.startAnimateHideShow(event, event.getEventType() == DecoEvent.EventType.EVENT_SHOW);
                }
            }
        }
        return true;
    }

    /**
     * Process event effect as required
     *
     * @param event DecoEvent to process
     * @return true is handled
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean executeEffect(@NonNull DecoEvent event) {
        if (event.getEventType() != DecoEvent.EventType.EVENT_EFFECT) {
            return false;
        }

        if (mChartSeries == null) {
            return false;
        }

        if (event.getIndexPosition() < 0) {
            Log.e(TAG, "EffectType " + event.getEventType().toString() + " must specify valid data series index");
            return false;
        }

        /**
         * The EFFECT_SPIRAL_EXPLODE is a special case where different operations are applied to
         * different series automatically. Must specify a valid series to use this effect
         */
        if (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE) {
            // hide all series, except the one to apply the effect
            for (int i = 0; i < mChartSeries.size(); i++) {
                ChartSeries chartSeries = mChartSeries.get(i);
                if (i != event.getIndexPosition()) {
                    chartSeries.startAnimateHideShow(event, false);
                } else {
                    chartSeries.startAnimateEffect(event);
                }
            }
            return true;
        }

        for (int i = 0; i < mChartSeries.size(); i++) {
            if ((event.getIndexPosition() == i) || event.getIndexPosition() < 0) {
                ChartSeries chartSeries = mChartSeries.get(i);
                chartSeries.startAnimateEffect(event);
            }
        }

        return true;
    }

    /**
     * This is called when the view is detached from a window. At this point it no longer has a
     * surface for drawing, so we need to remove all scheduled events from the event manager
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDecoEventManager != null) {
            mDecoEventManager.resetEvents();
        }
    }

    /**
     * Event Manager wants to start an event. It is this classes responsibility to execute the
     * event
     *
     * @param event Event to be executed
     */
    @Override
    public void onExecuteEventStart(@NonNull DecoEvent event) {
        executeMove(event);
        executeReveal(event);
        executeEffect(event);
    }

    /**
     * Set the Vertical gravity of the DecoView
     *
     * @param vertGravity Vertical Gravity
     */
    public void setVertGravity(VertGravity vertGravity) {
        mVertGravity = vertGravity;
    }

    /**
     * Set the Horizontal Gravity of the DecoView
     *
     * @param horizGravity Horizontal Gravity
     */
    public void setHorizGravity(HorizGravity horizGravity) {
        mHorizGravity = horizGravity;
    }

    /**
     * Allows your app to use the EdgeDetail decoration by disabling Hardware acceleration
     * for the view on android API 11 - 17.
     * <p/>
     * Calling this function will do nothing on all other API versions
     * <p/>
     * This will turn off Hardware Acceleration for this view only
     * <p/>
     * If you do not call this function and you use EdgeDetails they will not display on the
     * affected API versions.
     * <p/>
     * For more information about Hardware acceleration and this issue
     * {@see http://developer.android.com/guide/topics/graphics/hardware-accel.html}
     * <p/>
     * The function causing the incompatibility is
     * {@link Canvas#clipPath(Path)}
     * This is used to clip the drawing rectangle to help render the Edge details decorations
     */
    public void enableCompatibilityMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * Retrieve the {@link SeriesItem} based on the index
     *
     * @param index index of the series item
     * @return SeriesItem
     */
    @Deprecated
    public SeriesItem getSeriesItem(int index) {
        if (index >= 0 && index < mChartSeries.size()) {
            return mChartSeries.get(index).getSeriesItem();
        }
        return null;
    }

    /**
     * Retrieve the {@link SeriesItem} based on the index
     *
     * @param index index of the series item
     * @return ChartSeries at given index
     */
    public ChartSeries getChartSeries(int index) {
        if (index >= 0 && index < mChartSeries.size()) {
            return mChartSeries.get(index);
        }
        return null;
    }

    /**
     * Vertical positioning values
     */
    public enum VertGravity {
        GRAVITY_VERTICAL_TOP,
        GRAVITY_VERTICAL_CENTER,
        GRAVITY_VERTICAL_BOTTOM,
        GRAVITY_VERTICAL_FILL
    }

    /**
     * Horizontal positioning values
     */
    public enum HorizGravity {
        GRAVITY_HORIZONTAL_LEFT,
        GRAVITY_HORIZONTAL_CENTER,
        GRAVITY_HORIZONTAL_RIGHT,
        GRAVITY_HORIZONTAL_FILL
    }
}