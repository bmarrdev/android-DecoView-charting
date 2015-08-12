/*
 * Copyright (C) 2015 Brent Marriott
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hookedonplay.decoviewlib.charts;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * ArcItem holds the attributes required to represent an animated arc
 * <p/>
 * Construction example:
 * <p/>
 * ArcItem arcItem = new ArcItem.Builder(getResources().getColor(R.color.orb1_series2))
 * .setRange(0,100,0)
 * .setLineWidth(10)
 * .setSpinDuration(8000)
 * .setShowPointWhenEmpty(false)
 * .build();
 */
@SuppressWarnings("unused")
public class SeriesItem {
    /**
     * Main color of the arc
     */
    private int mColor;
    /**
     * Secondary Color of the arc used to create gradient for arc. This is only used if the
     * alpha component is > 0
     */
    private int mColorSecondary;
    /**
     * The width of the line used to draw the arc
     */
    private float mLineWidth;
    /**
     * Duration taken to animate a 360 degree animation of an arc
     */
    private long mSpinDuration;
    /**
     * Minimum value the represents the start of the arc. For example the view may represent
     * a distance where it starts at 0km and ends at 100km
     */
    private float mMinValue;
    /**
     * Value to represent the end of the arc
     */
    private float mMaxValue;
    /**
     * Initial value for this arc. When drawing a background track this should be set to the
     * maximum value
     */
    private float mInitialValue;
    /**
     * Initial value for visibility of this arc.
     */
    private boolean mInitialVisibility;
    /**
     * Determines if the arc animate in a clockwise or anticlockwise direction
     */
    private boolean mSpinClockwise;
    /**
     * Set the cap of the arc to be rounded rather than square
     */
    private boolean mRoundCap;
    /**
     * Draw this series as a point rather than an arc
     */
    private boolean mDrawAsPoint;
    /**
     * Style to draw the data
     * {@link ChartStyle}
     */
    private ChartStyle mChartStyle;
    /**
     * Interpolator used to perform the animation as the current
     * value is moved
     */
    private Interpolator mInterpolator;
    /**
     * Draw the arc even when it is empty
     */
    private boolean mShowPointWhenEmpty;
    /**
     * Draw the arc at an amount inset from the outside of the view
     */
    private PointF mInset;
    /**
     * Draw shadow on edge for effect. Any number of edge effects can be applied
     */
    private ArrayList<EdgeDetail> mEdgeDetail;
    /**
     * Label for the data series
     */
    private SeriesLabel mSeriesLabel;
    /**
     * Provides optional callback functionality on progress update of animation
     */
    private ArrayList<SeriesItemListener> mListeners;

    private SeriesItem(Builder builder) {
        mColor = builder.mColor;
        mColorSecondary = builder.mColorSecondary;
        mLineWidth = builder.mLineWidth;
        mSpinDuration = builder.mSpinDuration;
        mMinValue = builder.mMinValue;
        mMaxValue = builder.mMaxValue;
        mInitialValue = builder.mInitialValue;
        mInitialVisibility = builder.mInitialVisibility;
        mSpinClockwise = builder.mSpinClockwise;
        mRoundCap = builder.mRoundCap;
        mDrawAsPoint = builder.mDrawAsPoint;
        mChartStyle = builder.mChartStyle;
        mInterpolator = builder.mInterpolator;
        mShowPointWhenEmpty = builder.mShowPointWhenEmpty;
        mInset = builder.mInset;
        mEdgeDetail = builder.mEdgeDetail;
        mSeriesLabel = builder.mSeriesLabel;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getSecondaryColor() {
        return mColorSecondary;
    }

    public void setSecondaryColor(int color) {
        mColorSecondary = color;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    public long getSpinDuration() {
        return mSpinDuration;
    }

    public float getMinValue() {
        return mMinValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public float getInitialValue() {
        return mInitialValue;
    }

    public boolean getInitialVisibility() {
        return mInitialVisibility;
    }

    public boolean getSpinClockwise() {
        return mSpinClockwise;
    }

    public boolean getRoundCap() {
        return mRoundCap;
    }

    public boolean getDrawAsPoint() {
        return mDrawAsPoint;
    }

    public ChartStyle getChartStyle() {
        return mChartStyle;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public boolean showPointWhenEmpty() {
        return mShowPointWhenEmpty;
    }

    public PointF getInset() {
        if (mInset == null) {
            mInset = new PointF(0, 0);
        }
        return mInset;
    }

    public ArrayList<EdgeDetail> getEdgeDetail() {
        return mEdgeDetail;
    }

    public void addEdgeDetail(@Nullable EdgeDetail edgeDetail) {
        if (edgeDetail == null) {
            mEdgeDetail = null;
            return;
        }
        if (mEdgeDetail == null) {
            mEdgeDetail = new ArrayList<>();
        }
        mEdgeDetail.add(new EdgeDetail(edgeDetail));
    }

    public void setSeriesLabel(SeriesLabel label) {
        mSeriesLabel = label;
    }

    public SeriesLabel getSeriesLabel() {
        return mSeriesLabel;
    }

    /**
     * Set a listener to get notification of completion of animation
     *
     * @param listener OrbSeriesItemListener to be used for callbacks
     */
    public void addArcSeriesItemListener(@NonNull SeriesItemListener listener) {

        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    ArrayList<SeriesItemListener> getListeners() {
        return mListeners;
    }

    public enum ChartStyle {
        STYLE_DONUT, /* Default: Hole in middle */
        STYLE_PIE, /* Drawn from center point to outer limit */
        STYLE_LINE_HORIZONTAL, /* Drawn as a horizontal straight line */
        STYLE_LINE_VERTICAL /* Drawn as a horizontal straight line */
    }

    /**
     * Callback interface for notification of animation end
     */
    public interface SeriesItemListener {
        void onSeriesItemAnimationProgress(float percentComplete, float currentPosition);

        void onSeriesItemDisplayProgress(float percentComplete);
    }

    public static class Builder {
        private int mColor = Color.argb(255, 32, 32, 32);
        private int mColorSecondary = Color.argb(0, 0, 0, 0);
        private float mLineWidth = -1;
        private long mSpinDuration = 5000;
        private float mMinValue = 0f;
        private float mMaxValue = 100f;
        private float mInitialValue = 0f;
        private boolean mInitialVisibility = true;
        private boolean mSpinClockwise = true;
        private boolean mRoundCap = true;
        private boolean mDrawAsPoint = false;
        private ChartStyle mChartStyle = ChartStyle.STYLE_DONUT;
        private Interpolator mInterpolator;
        private boolean mShowPointWhenEmpty = true;
        private PointF mInset = null;
        private ArrayList<EdgeDetail> mEdgeDetail = null;
        private SeriesLabel mSeriesLabel = null;

        public Builder(int color) {
            mColor = color;
        }

        public Builder(int color, int colorSecondary) {
            mColor = color;
            mColorSecondary = colorSecondary;
        }

        public Builder setLineWidth(final float lineWidth) {
            mLineWidth = lineWidth;
            return this;
        }

        public Builder setSpinDuration(final long spinDuration) {
            if (spinDuration <= 100) {
                throw new IllegalArgumentException("SpinDuration must be > 100 (value is in ms)");
            }
            mSpinDuration = spinDuration;
            return this;
        }

        public Builder setInitialVisibility(final boolean visibility) {
            mInitialVisibility = visibility;
            return this;
        }

        public Builder setSpinClockwise(final boolean spinClockwise) {
            mSpinClockwise = spinClockwise;
            return this;
        }

        public Builder setCapRounded(final boolean roundCap) {
            mRoundCap = roundCap;
            return this;
        }

        public Builder setDrawAsPoint(final boolean drawAsPoint) {
            mDrawAsPoint = drawAsPoint;
            return this;
        }

        public Builder setChartStyle(@NonNull final ChartStyle chartStyle) {
            mChartStyle = chartStyle;
            return this;
        }

        public Builder setRange(final float minValue, final float maxValue, final float initialValue) {
            if (minValue >= maxValue) {
                throw new IllegalArgumentException("minimum value must be less that maximum value");
            }
            if (minValue > initialValue || maxValue < initialValue) {
                throw new IllegalArgumentException("Initial value must be in the range of min .. max");
            }
            mMinValue = minValue;
            mMaxValue = maxValue;
            mInitialValue = initialValue;

            return this;
        }

        /**
         * Set the interpolator to be used with the animation
         *
         * @param interpolator Optional interpolator to set
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setInterpolator(@Nullable Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        public Builder setShowPointWhenEmpty(boolean showPointWhenEmpty) {
            mShowPointWhenEmpty = showPointWhenEmpty;
            return this;
        }

        public Builder setInset(@Nullable PointF inset) {
            mInset = inset;
            return this;
        }

        public Builder addEdgeDetail(@Nullable EdgeDetail edgeDetail) {
            if (edgeDetail == null) {
                mEdgeDetail = null;
                return this;
            }
            if (mEdgeDetail == null) {
                mEdgeDetail = new ArrayList<>();
            }
            mEdgeDetail.add(new EdgeDetail(edgeDetail));
            return this;
        }

        public Builder setSeriesLabel(@Nullable SeriesLabel seriesLabel) {
            mSeriesLabel = seriesLabel;
            return this;
        }

        /**
         * Creates a {@link SeriesItem} with the arguments supplied to this builder.
         */
        public SeriesItem build() {
            return new SeriesItem(this);
        }

    }
}