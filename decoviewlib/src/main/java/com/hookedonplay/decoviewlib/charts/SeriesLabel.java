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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hookedonplay.decoviewlib.util.GenericFunctions;

/**
 * Label for one series of data. One DecoView chart can have many series and each series can have
 * one label. As the series is animated the label will move to be centered over the visible area
 * of the series. For example if another series is on top then potentially only a small portion of
 * the complete area of the series will be visible.
 * <p/>
 * The initial implementation of a series label does not support anti-clockwise series.
 * <p/>
 * To update the SeriesLabel text as the series is animation there are two options:
 * (a) you can use the listener for the series to update the label text
 * (b) you can add a formatted string as the label and it will be updated automatically. If your
 * label text includes %% then the percentage will be substituted. eg: "Goal %.0f%%" or
 * if you want the actual current value of the series "%.0f min to goal"
 */
@SuppressWarnings("unused")
public class SeriesLabel {
    static private Typeface mDefaultTypeFace;
    private final float mBufferX = 15f;
    private final float mBufferY = 15f;
    private String mLabel;
    private Paint mPaintBack;
    private Paint mPaintText;
    private float mFontSize;
    private Typeface mTypeface;
    private Rect mTextBounds;
    private RectF mTextDraw;
    private float mTextCenter;
    private boolean mVisible;
    private int mColorText;
    private int mColorBack;

    private SeriesLabel(Builder builder) {
        mLabel = builder.mLabel;
        mVisible = builder.mVisible;
        mColorText = builder.mColorText;
        mColorBack = builder.mColorBack;
        mTypeface = builder.mTypeface;
        mFontSize = builder.mFontSize;
        recalcLayout();
    }

    static public SeriesLabel createLabel(String label) {
        return new Builder(label).build();
    }

    static public void setDefaultFont(Context context, String fontName) {
        mDefaultTypeFace = Typeface.createFromAsset(context.getAssets(), fontName);
    }

    private void recalcLayout() {
        if (mPaintBack == null) {
            mPaintBack = new Paint();
            mPaintBack.setColor(mColorBack);
        }

        if (mPaintText == null) {
            mPaintText = new Paint();
            mPaintText.setColor(mColorText);
            mPaintText.setTextSize(GenericFunctions.spToPixels(mFontSize));
            mPaintText.setTextAlign(Paint.Align.CENTER);

            if (mTypeface != null) {
                mPaintText.setTypeface(mDefaultTypeFace);
            } else if (mDefaultTypeFace != null) {
                mPaintText.setTypeface(mDefaultTypeFace);
            }
        }

        if (mTextBounds == null) {
            mTextBounds = new Rect();
            mPaintText.getTextBounds(mLabel, 0, mLabel.length(), mTextBounds);
            mTextDraw = new RectF();
            mTextCenter = ((mPaintText.descent() + mPaintText.ascent()) / 2);
        }
    }

    private String getDisplayString(float percentComplete, float positionValue) {
        // Check if we have dynamically generated text
        if (mLabel.contains("%%")) {
            // We found a percentage symbol so we insert a percentage
            return String.format(mLabel, percentComplete * 100f);
        } else if (mLabel.contains("%")) {
            return String.format(mLabel, positionValue);
        }
        // Static label
        return mLabel;
    }

    public void setLabel(@NonNull String label) {
        mLabel = label;
        mTextBounds = null;
        recalcLayout();
    }

    public RectF draw(@NonNull Canvas canvas, @NonNull RectF rect,
                      float percentAngle, float percentComplete, float positionValue) {
        if (!mVisible) {
            return null;
        }

        float radius = rect.width() / 2;
        float radians = ((360f * percentAngle) - 90) * (float) (Math.PI / 180f);

        float xVal = (float) Math.cos(radians) * radius + rect.centerX();
        float yVal = (float) Math.sin(radians) * radius + rect.centerY();

        final float halfWidth = (mTextBounds.width() / 2) + mBufferX;
        final float halfHeight = (mTextBounds.height() / 2) + mBufferY;
        if (0 > xVal - halfWidth) {
            xVal = halfWidth;
        }
        if (canvas.getWidth() < xVal + halfWidth) {
            xVal = canvas.getWidth() - halfWidth;
        }
        if (0 > yVal - halfHeight) {
            yVal = halfHeight;
        }
        if (canvas.getHeight() < yVal + halfHeight) {
            yVal = canvas.getHeight() - halfHeight;
        }

        mTextDraw.set(xVal - halfWidth,
                yVal - halfHeight,
                xVal + halfWidth,
                yVal + halfHeight);


        canvas.drawRoundRect(
                mTextDraw,
                10f, 10f, mPaintBack);

        yVal -= mTextCenter;
        canvas.drawText(getDisplayString(percentComplete, positionValue), xVal, yVal, mPaintText);

        return mTextDraw;
    }

    public static class Builder {
        private String mLabel = null;
        private Typeface mTypeface = null;
        private float mFontSize = 16;
        private int mColorText = Color.parseColor("#FFFFFFFF");
        private int mColorBack = Color.parseColor("#AA000000");
        private boolean mVisible = true;

        public Builder(@NonNull String labelText) {
            mLabel = labelText;
        }

        public Builder setTypeface(@Nullable Typeface typeface) {
            mTypeface = typeface;
            return this;
        }


        public Builder setFontSize(float fontSize) {
            mFontSize = fontSize;
            return this;
        }

        public Builder setVisible(boolean visible) {
            mVisible = visible;
            return this;
        }

        public Builder setColorText(int colorText) {
            mColorText = colorText;
            return this;
        }

        public Builder setColorBack(int colorBack) {
            mColorBack = colorBack;
            return this;
        }

        public SeriesLabel build() {
            return new SeriesLabel(this);
        }
    }
}
