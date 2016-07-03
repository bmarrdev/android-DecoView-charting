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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

import java.util.Random;

public class Sample3Fragment extends SampleFragment {
    final float mSeriesMax = 50f;
    final private int[] mColor = {
            Color.parseColor("#7B1FA2"),
            Color.parseColor("#FBC02D"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#FFCDD2"),
            Color.parseColor("#009688"),
            Color.parseColor("#F44366"),
            Color.parseColor("#FFF9C4")
    };
    private int[] mSeriesIndex = new int[7];
    private int mBackIndex;
    private boolean mFullCircle = true;
    private boolean mFlip = true;

    public Sample3Fragment() {
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
        mFullCircle = !mFullCircle;
        mFlip = mFullCircle ? !mFlip : mFlip;

        getView().setBackgroundColor(Color.argb(255, 32, 32, 32));

        decoView.deleteAll();
        decoView.configureAngles(mFullCircle ? 360 : 180, mFlip ? 180 : 0);


        float widthLine = getDimension(14f);

        SeriesItem seriesBackItem = new SeriesItem.Builder(Color.parseColor("#11FFFFFF"))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setLineWidth(widthLine * mSeriesIndex.length)
                .setInitialVisibility(false)
                .setCapRounded(false)
                .build();

        mBackIndex = decoView.addSeries(seriesBackItem);

        float inset = -((widthLine * (mSeriesIndex.length - 1)) / 2);
        for (int i = 0; i < mSeriesIndex.length; i++) {
            SeriesItem seriesItem = new SeriesItem.Builder(mColor[i])
                    .setRange(0, mSeriesMax, mSeriesMax)
                    .setLineWidth(widthLine)
                    .setInset(new PointF(inset, inset))
                    .setInitialVisibility(false)
                    .build();

            mSeriesIndex[i] = decoView.addSeries(seriesItem);

            inset += widthLine;
        }

        final TextView textPercent = (TextView) view.findViewById(R.id.textPercentage);
        textPercent.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setupEvents() {
        final DecoView decoView = getDecoView();

        if (decoView == null || decoView.isEmpty()) {
            throw new IllegalStateException("Unable to add events to empty DecoView");
        }
        decoView.executeReset();

        decoView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
                .setIndex(mBackIndex)
                .setDelay(100)
                .setDuration(3000)
                .build());

        decoView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
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

            decoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT_FILL)
                    .setIndex(mSeriesIndex[i])
                    .setDelay(500 + i * 250)
                    .setDuration(2500)
                    .build());

            decoView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax))
                    .setIndex(index)
                    .setDelay(5000 + i * 750)
                    .build());

            decoView.addEvent(new DecoEvent.Builder(rand.nextInt((int) mSeriesMax / 2))
                    .setIndex(index)
                    .setDelay(10000 + i * 500)
                    .setColor(Color.parseColor("#FF555555"))
                    .setDuration(2000)
                    .build());

            decoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                    .setIndex(index)
                    .setDelay(15000)
                    .setDuration(2000)
                    .setInterpolator(new BounceInterpolator())
                    .setColor(mColor[i])
                    .build());

            decoView.addEvent(new DecoEvent.Builder(0)
                    .setIndex(index)
                    .setDelay(17500 + i * 200)
                    .setDuration(1000)
                    .setInterpolator(new AccelerateInterpolator()).build());

            decoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_IN)
                    .setIndex(mSeriesIndex[i])
                    .setDelay(18500 + i * 200)
                    .setDuration(2000)
                    .setEffectRotations(3)
                    .build());
        }
    }
}