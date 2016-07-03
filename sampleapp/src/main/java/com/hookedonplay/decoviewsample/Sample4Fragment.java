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
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

import java.util.Random;

public class Sample4Fragment extends SampleFragment {
    final float mSeriesMax = 50f;
    final private int[] mColor = {
            Color.parseColor("#FF0000"),
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#EEEEEE"),
            Color.parseColor("#FF6666"),
            Color.parseColor("#DDDDDD"),
            Color.parseColor("#2222FF")
    };
    private int[] mSeriesIndex = new int[7];
    private int mBackIndex;
    private boolean mFullCircle = true;
    private boolean mFlip = true;

    public Sample4Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_generic, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final DecoView decoView = getDecoView();
        final View view = getView();
        if (decoView == null || view == null) {
            return;
        }
        decoView.setVertGravity(DecoView.VertGravity.GRAVITY_VERTICAL_FILL);
        decoView.setHorizGravity(DecoView.HorizGravity.GRAVITY_HORIZONTAL_FILL);

        mFullCircle = !mFullCircle;
        mFlip = mFullCircle ? !mFlip : mFlip;

        view.setBackgroundColor(Color.argb(255, 16, 16, 16));

        decoView.executeReset();
        decoView.deleteAll();

        decoView.configureAngles(mFullCircle ? 360 : 260, mFlip ? 180 : 0);

        float widthLine = getDimension(8f);
        SeriesItem seriesBackItem = new SeriesItem.Builder(Color.argb(255, 32, 32, 32))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setLineWidth(widthLine * mSeriesIndex.length)
                .setInitialVisibility(false)
                .setDrawAsPoint(false)
                .build();

        mBackIndex = decoView.addSeries(seriesBackItem);

        for (int i = 0; i < mSeriesIndex.length; i++) {
            SeriesItem seriesItem = new SeriesItem.Builder(mColor[i])
                    .setRange(0, mSeriesMax, 0)
                    .setLineWidth(widthLine * (mSeriesIndex.length - i))
                    .setInitialVisibility(false)
                    .setDrawAsPoint(i != 0)
                    .build();

            mSeriesIndex[i] = decoView.addSeries(seriesItem);
        }

        final TextView textPercent = (TextView) view.findViewById(R.id.textPercentage);
        textPercent.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();

        if (arcView == null || arcView.isEmpty()) {
            return;
        }
        arcView.executeReset();

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
                .setIndex(mBackIndex)
                .setDelay(100)
                .setDuration(3000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
                .setIndex(mBackIndex)
                .setDelay(18000)
                .setDuration(4000)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent event) {

                    }

                    @Override
                    public void onEventEnd(DecoEvent event) {
                        createTracks();
                        setupEvents();
                    }
                })
                .build());

        Random rand = new Random();
        for (int i = 0; i < mSeriesIndex.length; i++) {
            int index = mSeriesIndex[i];

            arcView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                    .setIndex(mSeriesIndex[i])
                    .setDelay(i * 250)
                    .setDuration(2500)
                    .build());

            if (i == 0) {
                arcView.addEvent(new DecoEvent.Builder(mSeriesMax)
                        .setIndex(index)
                        .setDelay(5000)
                        .build());

                arcView.addEvent(new DecoEvent.Builder(mSeriesMax / 2)
                        .setIndex(index)
                        .setDelay(9500)
                        .build());

            } else {
                arcView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax))
                        .setIndex(index)
                        .setDelay(5000 + (mSeriesIndex.length - 1 - i) * 750)
                        .build());

                arcView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax / 2))
                        .setIndex(index)
                        .setDelay(9500 + (mSeriesIndex.length - 1 - i) * 500)
                        .build());
            }

            arcView.addEvent(new DecoEvent.Builder(mSeriesMax)
                    .setIndex(index)
                    .setDelay(14000)
                    .setDuration(3000)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .build());

            arcView.addEvent(new DecoEvent.Builder(0)
                    .setIndex(index)
                    .setDelay(17500 + (mSeriesIndex.length - 1 - i) * 200)
                    .setDuration(1000)
                    .setInterpolator(new AccelerateInterpolator())
                    .build());


            arcView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_IN)
                    .setIndex(mSeriesIndex[i])
                    .setDelay(18500 + (mSeriesIndex.length - 1 - i) * 200)
                    .setDuration(2000)
                    .setEffectRotations(3)
                    .build());

        }
    }
}