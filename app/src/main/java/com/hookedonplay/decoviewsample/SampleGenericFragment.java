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
package com.hookedonplay.decoviewsample;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

public class SampleGenericFragment extends SampleFragment {
    private static final float[] M_TRACK_BACK_WIDTH = {30f, 60f, 30f, 40f, 20f};
    private static final float[] M_TRACK_WIDTH = {30f, 60f, 30f, 40f, 20f};
    private static final float[] M_DETAIL_EDGE = {0.3f, 0.2f, 0.4f, 0.21f, 0.25f};
    private static final boolean[] M_CLOCKWISE = {true, true, true, false, true};
    private static final boolean[] M_ROUNDED = {true, true, true, true, true};
    private static final boolean[] M_PIE = {false, false, false, false, true};
    private static final int[] M_TOTAL_ANGLE = {360, 360, 320, 260, 360};
    private static final int[] M_ROTATE_ANGLE = {0, 180, 180, 0, 270};
    private int mBackIndex;
    private int mSeries1Index;
    private int mSeries2Index;
    private int mStyleIndex;

    public SampleGenericFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_generic, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final DecoView arcView = getDecoView();
        final View view = getView();
        if (arcView == null || view == null) {
            return;
        }
        arcView.deleteAll();
        arcView.configureAngles(M_TOTAL_ANGLE[mStyleIndex], M_ROTATE_ANGLE[mStyleIndex]);

        final float seriesMax = 50f;
        SeriesItem arcBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, seriesMax, seriesMax)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(M_TRACK_BACK_WIDTH[mStyleIndex]))
                .setChartStyle(M_PIE[mStyleIndex] ? SeriesItem.ChartStyle.STYLE_PIE : SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackIndex = arcView.addSeries(arcBackTrack);

        float inset = 0;
        if (M_TRACK_BACK_WIDTH[mStyleIndex] != M_TRACK_WIDTH[mStyleIndex]) {
            inset = getDimension((M_TRACK_BACK_WIDTH[mStyleIndex] - M_TRACK_WIDTH[mStyleIndex]) / 2);
        }
        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 255, 165, 0))
                .setRange(0, seriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(M_TRACK_WIDTH[mStyleIndex]))
                .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(M_CLOCKWISE[mStyleIndex])
                .setCapRounded(M_ROUNDED[mStyleIndex])
                .setChartStyle(M_PIE[mStyleIndex] ? SeriesItem.ChartStyle.STYLE_PIE : SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        if (M_DETAIL_EDGE[mStyleIndex] > 0) {
            seriesItem1.addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_OUTER, Color.parseColor("#33000000"), M_DETAIL_EDGE[mStyleIndex]));
        }

        mSeries1Index = arcView.addSeries(seriesItem1);

        SeriesItem seriesItem2 = new SeriesItem.Builder(Color.argb(255, 255, 51, 51))
                .setRange(0, seriesMax, 0)
                .setInitialVisibility(false)
                .setCapRounded(true)
                .setLineWidth(getDimension(M_TRACK_WIDTH[mStyleIndex]))
                .setInset(new PointF(inset, inset))
                .setCapRounded(M_ROUNDED[mStyleIndex])
                .build();

        if (M_DETAIL_EDGE[mStyleIndex] > 0) {
            seriesItem2.addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#20000000"), M_DETAIL_EDGE[mStyleIndex]));
        }

        mSeries2Index = arcView.addSeries(seriesItem2);

        final TextView textPercent = (TextView) view.findViewById(R.id.textPercentage);
        textPercent.setVisibility(View.INVISIBLE);
        textPercent.setText("");
        addProgressListener(seriesItem1, textPercent, "%.0f%%");
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();
        if (arcView == null || arcView.isEmpty()) {
            return;
        }

        mUpdateListeners = true;
        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
                .setDelay(1000)
                .setDuration(2000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(10).setIndex(mSeries2Index).setDelay(3900).build());
        arcView.addEvent(new DecoEvent.Builder(22).setIndex(mSeries2Index).setDelay(7000).build());

        arcView.addEvent(new DecoEvent.Builder(25).setIndex(mSeries1Index).setDelay(3300).build());
        arcView.addEvent(new DecoEvent.Builder(50).setIndex(mSeries1Index).setDuration(1500).setDelay(9000).build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
                .setDelay(11000)
                .setDuration(2000)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent event) {

                    }

                    @Override
                    public void onEventEnd(DecoEvent event) {
                        mStyleIndex++;
                        if (mStyleIndex >= M_TRACK_BACK_WIDTH.length) {
                            mStyleIndex = 0;
                            setDemoFinished(true);
                            return;
                        }

                        createTracks();
                        setupEvents();
                    }
                })
                .build());

    }
}