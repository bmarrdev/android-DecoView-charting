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

public class EdgeDetail {

    public enum EdgeType {
        EDGE_INNER,
        EDGE_OUTER
    }
    private int mColor;
    private float mRatio;
    private EdgeType mEdgeType;

    private Path mClipPath;

    public EdgeDetail(EdgeType edgeType, int color, float percentRatio) {
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

    public int getColor() {
        return mColor;
    }

    public float getRatio() {
        return mRatio;
    }

    public EdgeType getEdgeType() {
        return mEdgeType;
    }

    Path getClipPath() {
        return mClipPath;
    }

    void setClipPath(Path clipPath) {
        mClipPath = clipPath;
    }

}
