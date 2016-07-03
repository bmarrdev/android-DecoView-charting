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
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.ChartSeries;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class SamplePauseFragment extends SampleFragment {
    private int mSeries1Index;
    private int mPieIndex;

    public SamplePauseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_pause, container, false);
    }

    @Override
    protected void createTracks() {
        final DecoView decoView = getDecoView();
        final View view = getView();
        if (decoView == null || view == null) {
            return;
        }
        view.setBackgroundColor(Color.argb(255, 64, 96, 214));

        decoView.executeReset();
        decoView.deleteAll();

        final float seriesMax = 100f;

        float circleInset = getDimension(18);
        SeriesItem seriesBack1Item = new SeriesItem.Builder(Color.parseColor("#11000000"))
                .setRange(0, seriesMax, 0)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .setInset(new PointF(circleInset, circleInset))
                .build();

        mPieIndex = decoView.addSeries(seriesBack1Item);

        SeriesItem series1Item = new SeriesItem.Builder(Color.parseColor("#FFFFC107"))
                .setRange(0, seriesMax, 0)
                .setLineWidth(getDimension(36))
                .setInterpolator(new LinearInterpolator())
                .build();

        series1Item.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                decoView.getChartSeries(mPieIndex).setPosition(percentComplete < 1.0f ? percentComplete * seriesMax : 0f);
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries1Index = decoView.addSeries(series1Item);

        final TextView textPause = (TextView) view.findViewById(R.id.textViewPause);
        final DecoEvent.ExecuteEventListener eventListener = new DecoEvent.ExecuteEventListener() {
            @Override
            public void onEventStart(DecoEvent event) {
                textPause.setText("PAUSE");
            }

            @Override
            public void onEventEnd(DecoEvent event) {
                textPause.setText("");
                decoView.getChartSeries(mPieIndex).reset();
            }
        };

        Button buttonEmpty = (Button) view.findViewById(R.id.buttonEmpty);
        buttonEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decoView.addEvent(new DecoEvent.Builder(0)
                        .setIndex(mSeries1Index)
                        .setListener(eventListener)
                        .build());
            }
        });

        Button buttonFifty = (Button) view.findViewById(R.id.buttonFifty);
        buttonFifty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decoView.addEvent(new DecoEvent.Builder(seriesMax * 0.5f)
                        .setIndex(mSeries1Index)
                        .setListener(eventListener)
                        .build());
            }
        });

        Button buttonFull = (Button) view.findViewById(R.id.buttonFull);
        buttonFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decoView.addEvent(new DecoEvent.Builder(seriesMax)
                        .setIndex(mSeries1Index)
                        .setListener(eventListener)
                        .build());
            }
        });

        textPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChartSeries series = decoView.getChartSeries(mSeries1Index);
                if (series.isPaused()) {
                    series.resume();
                } else {
                    if (series.pause()) {
                        textPause.setText("RESUME");
                        decoView.getChartSeries(mPieIndex).reset();
                    }
                }
            }
        });
    }

    @Override
    protected void setupEvents() {
        // Not scheduled events, all user initiated
    }
}