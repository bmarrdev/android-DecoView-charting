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

import android.graphics.Path;
import android.support.annotation.NonNull;

/**
 * EdgeDetail allows the addition of a shadow effect on the edge of the series. This can be
 * added to the inner or outer edge and the usual approach would be to have a semi-transparent shade
 * as the effect. This requires on old versions of Android 4.0-4.3 that Hardware acceleration is
 * turned off for the view.
 *
 * NOTE: It is possible to apply any number of edge detail to each series. If you so desired you
 * could apply a rainbow effect by applying many different colors at different widths
 */
public class EdgeDetail {

    /**
     * Color of the edge to draw. Recommended a shadow like 0x33000000
     */
    private int mColor;
    /**
     * The ratio of the series width to fill with the edge detail. If you apply 0.5f as the ratio
     * then the effect will apply to half of the line width of the data. 1.0 will fill the complete
     * line with the new color, although this would be the least efficient method to change the
     * line color
     */
    private float mRatio;
    /**
     * Inner or Outer Edge to apply shadow
     */
    private EdgeType mEdgeType;
    /**
     * Internal clip path used to apply the effect to only {@link #mRatio} of the line
     */
    private Path mClipPath;

    @SuppressWarnings("unused")
    public EdgeDetail(@NonNull EdgeType edgeType, int color, float percentRatio) {
        if (percentRatio > 1.0 || percentRatio < 0) {
            throw new IllegalArgumentException("Invalid ratio set for EdgeDetail");
        }
        mEdgeType = edgeType;
        mColor = color;
        mRatio = percentRatio;
    }

    EdgeDetail(@NonNull final EdgeDetail edgeDetail) {
        mEdgeType = edgeDetail.mEdgeType;
        mColor = edgeDetail.mColor;
        mRatio = edgeDetail.mRatio;
        mClipPath = null;
    }

    /**
     * Get the color of edge effect
     *
     * @return color
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Ratio of the line to cover with the effect (none 0.0 -> 1.0 full)
     *
     * @return percentage of the line covered
     */
    public float getRatio() {
        return mRatio;
    }

    /**
     * Type of edge effect {@link com.hookedonplay.decoviewlib.charts.EdgeDetail.EdgeType}
     *
     * @return EdgeType to be applied
     */
    public EdgeType getEdgeType() {
        return mEdgeType;
    }

    /**
     * Clip path used when drawing the edge effect. Even though each individual chart type draws its
     * own edge effect, we store the clip path in this object so it does not need to be reallocated
     * on each draw
     *
     * @return Path used for clipping edge effect
     */
    Path getClipPath() {
        return mClipPath;
    }

    /**
     * Set the clip path for the edge effect
     *
     * @param clipPath Path for clipping
     */
    void setClipPath(Path clipPath) {
        mClipPath = clipPath;
    }

    /**
     * Edge to apply the effect to.
     */
    @SuppressWarnings("unused")
    public enum EdgeType {
        EDGE_INNER,
        EDGE_OUTER
    }
}
