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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

import java.util.Random;

public class SamplePeopleFragment extends SampleFragment {
    private int mSeries1Index;
    private int mSeries2Index;
    private int mSeries3Index;
    private int mBack1Index;
    private int mBack2Index;

    final float mSeriesMax = 100f;

    public SamplePeopleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_people, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final DecoView arcView = getDecoView();
        if (arcView == null) {
            return;
        }

        arcView.executeReset();
        arcView.deleteAll();

        SeriesItem seriesBack1Item = new SeriesItem.Builder(Color.argb(255, 32, 196, 32))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .build();

        mBack1Index = arcView.addSeries(seriesBack1Item);

        SeriesItem seriesBack2Item = new SeriesItem.Builder(Color.argb(255, 196, 196, 196))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setLineWidth(getDimension(36))
                .build();

        mBack2Index = arcView.addSeries(seriesBack2Item);


            SeriesItem series1Item = new SeriesItem.Builder(Color.parseColor("#FF0000"))
                    .setRange(0, mSeriesMax, 0)
                    .setInitialVisibility(false)
                    .setLineWidth(getDimension(36))
                    .build();

        mSeries1Index = arcView.addSeries(series1Item);

        SeriesItem series2Item = new SeriesItem.Builder(Color.parseColor("#00FF00"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(36))
                .build();

        mSeries2Index = arcView.addSeries(series2Item);

        SeriesItem series3Item = new SeriesItem.Builder(Color.parseColor("#0000FF"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(36))
                .build();

        mSeries3Index = arcView.addSeries(series3Item);

        //final TextView textPercent = (TextView) getView().findViewById(R.id.textPercentage);
        //textPercent.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();

        if (arcView == null || arcView.isEmpty()) {
            return;
        }
        arcView.executeReset();

        final ImageView imgView = (ImageView)getView().findViewById(R.id.imageViewAvatar);

        DecoEvent.ExecuteEventListener listener = new DecoEvent.ExecuteEventListener() {
            @Override
            public void onEventStart(DecoEvent event) {
                imgView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEventEnd(DecoEvent event) {
                imgView.setVisibility(View.INVISIBLE);
            }
        };
        arcView.addEvent(new DecoEvent.Builder(20)
                .setIndex(mSeries1Index)
                .setDelay(1000)
                .setDuration(3000)
                .setListener(listener)
                .build());

        arcView.addEvent(new DecoEvent.Builder(70)
                .setIndex(mSeries1Index)
                .setDelay(6000)
                .setDuration(3000)
                .setColor(Color.parseColor("#FF999999"))
                .build());

        arcView.addEvent(new DecoEvent.Builder(100)
                .setIndex(mSeries1Index)
                .setDelay(11000)
                .setDuration(3000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_COLOR_CHANGE, Color.parseColor("#FF0000"))
                .setIndex(mSeries1Index)
                .setDelay(16000)
                .setDuration(3000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(50)
                .setIndex(mSeries2Index)
                .setDelay(6000)
                .setDuration(3000)
                .setListener(listener)
                .build());

        arcView.addEvent(new DecoEvent.Builder(80)
                .setIndex(mSeries2Index)
                .setDelay(11000)
                .setDuration(3000)
                .setColor(Color.parseColor("#FF666666"))
                .build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_COLOR_CHANGE, Color.parseColor("#00FF00"))
                .setIndex(mSeries2Index)
                .setDelay(16000)
                .setDuration(3000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(30)
                .setIndex(mSeries3Index)
                .setDelay(11000)
                .setDuration(3000)
                .setListener(listener)
                .build());

//
//        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
//                .setIndex(mBackIndex)
//                .setDelay(18000)
//                .setDuration(4000)
//                .setListener(new DecoEvent.ExecuteEventListener() {
//                    @Override
//                    public void onEventStart(DecoEvent event) {
//
//                    }
//
//                    @Override
//                    public void onEventEnd(DecoEvent event) {
//                        createTracks();
//                        setupEvents();
//                    }
//                })
//                .build());
//
//        Random rand = new Random();
//        for (int i = 0; i < mSeriesIndex.length; i++) {
//            int index = mSeriesIndex[i];
//
//            arcView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
//                    .setIndex(mSeriesIndex[i])
//                    .setDelay(i * 250)
//                    .setDuration(2500)
//                    .build());
//
//            arcView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax))
//                    .setIndex(index)
//                    .setDelay(5000 + (mSeriesIndex.length - 1 - i) * 750)
//                            .build());
//
//            arcView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax / 2))
//                    .setIndex(index)
//                    .setDelay(9500 + (mSeriesIndex.length - 1 - i) * 500)
//                    .build());
//
//            arcView.addEvent(new DecoEvent.Builder(mSeriesMax)
//                    .setIndex(index)
//                    .setDelay(14000)
//                    .setDuration(3000)
//                    .setInterpolator(new AnticipateOvershootInterpolator())
//                    .build());
//
//            arcView.addEvent(new DecoEvent.Builder(0)
//                    .setIndex(index)
//                    .setDelay(17500 + (mSeriesIndex.length - 1 - i) * 200)
//                    .setDuration(1000)
//                    .setInterpolator(new AccelerateInterpolator()).build());
//
//            arcView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_IN)
//                    .setIndex(mSeriesIndex[i])
//                    .setDelay(18500 + (mSeriesIndex.length - 1 - i) * 200)
//                    .setDuration(2000)
//                    .setEffectRotations(3)
//                    .build());
//        }
    }
}