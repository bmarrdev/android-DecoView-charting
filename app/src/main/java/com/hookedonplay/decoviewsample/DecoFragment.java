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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class DecoFragment extends SampleFragment {

    final private int[] mPalette = {Color.parseColor("#f57c00"), Color.parseColor("#212121"), Color.parseColor("#4caf50"), Color.parseColor("#727272"), Color.parseColor("#b6b6b6")};
    private int mSeries1Index;

    public DecoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deco, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final DecoView arcView = getDecoView();
        if (arcView == null) {
            return;
        }
        arcView.deleteAll();
        arcView.configureAngles(270, 90);

        final int count = mPalette.length;
        final float width = 28;

        for (int i = 0; i < count; i++) {
            float inset = i * getDimension(width - 5);
            SeriesItem seriesItem1 = new SeriesItem.Builder(mPalette[i])
                    .setRange(0, 100, 0)
                    .setLineWidth(getDimension(width))
                    .setInset(new PointF(inset, inset))
                    .setShowPointWhenEmpty(false)
                    .build();

            mSeries1Index = arcView.addSeries(seriesItem1);
        }
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();
        final View view = getView();
        if (arcView == null || view == null) {
            return;
        }

        try {
            View finished = view.findViewById(R.id.imageSwipeRight);
            finished.setVisibility(View.INVISIBLE);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Unable to find swipe hint image");
        }

        int count = mPalette.length;
        for (int i = 0; i < count; i++) {
            final boolean last = i == count - 1;

            DecoEvent revealEvent = new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                    .setIndex(mSeries1Index - i)
                    .setDelay(200 * i)
                    .setDuration(1500)
                    .build();

            arcView.addEvent(revealEvent);

            arcView.addEvent(new DecoEvent.Builder(100).setIndex(mSeries1Index - i).setDelay(1500 + (200 * i))
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(4000).build());

            arcView.addEvent(new DecoEvent.Builder(0).setIndex(mSeries1Index - i).setDelay(5750 + (200 * i))
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(1500).build());


            arcView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_IN)
                    .setIndex(mSeries1Index - i)
                    .setEffectRotations(3)
                    .setDelay(7250 + (200 * i))
                    .setDuration(1500)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new DecoEvent.ExecuteEventListener() {
                        @Override
                        public void onEventStart(DecoEvent event) {

                        }

                        @Override
                        public void onEventEnd(DecoEvent event) {
                            if (last) {
                                try {
                                    View finished = getView().findViewById(R.id.imageSwipeRight);
                                    finished.setVisibility(View.VISIBLE);
                                } catch (NullPointerException npe) {
                                    Log.e(TAG, "Unable to access finished view");
                                }
                            }
                        }
                    })
                    .build());
        }
    }
}
