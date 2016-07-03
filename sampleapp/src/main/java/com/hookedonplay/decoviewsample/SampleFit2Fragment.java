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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class SampleFit2Fragment extends SampleFragment {
    private int mSeries1Index;
    private String mProgress;

    public SampleFit2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_fit2, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final View view = getView();
        final DecoView decoView = getDecoView();
        if (view == null || decoView == null) {
            return;
        }

        view.setBackgroundColor(Color.argb(255, 196, 196, 128));

        decoView.executeReset();
        decoView.deleteAll();

        final float mSeriesMax = 100f;

        SeriesItem seriesBack1Item = new SeriesItem.Builder(COLOR_BACK)
                .setRange(0, mSeriesMax, mSeriesMax)
                .setLineWidth(getDimension(46))
                .build();

        decoView.addSeries(seriesBack1Item);

        SeriesItem series1Item = new SeriesItem.Builder(COLOR_NEUTRAL)
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(46))
                .setCapRounded(true)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, COLOR_EDGE, 0.3f))
                .setShowPointWhenEmpty(true)
                .build();

        mSeries1Index = decoView.addSeries(series1Item);

        TextView textListener = (TextView) view.findViewById(R.id.textProgress);
        addFitListener(series1Item, textListener);
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();
        final View view = getView();
        if (view == null || arcView == null || arcView.isEmpty()) {
            return;
        }

        final ImageView imgView = (ImageView) view.findViewById(R.id.imageViewAvatar);

        addAnimation(arcView, mSeries1Index, 79.7f, 2000, imgView, R.drawable.ic_fit_bike, "Cycle %.0f Km", COLOR_GREEN, false);
        addAnimation(arcView, mSeries1Index, 16.4f, 9000, imgView, R.drawable.ic_fit_run, "Run %.1f Km", COLOR_YELLOW, false);
        addAnimation(arcView, mSeries1Index, 58f, 16000, imgView, R.drawable.ic_fit_gym, "Gym %.0f min", COLOR_PINK, false);
        addAnimation(arcView, mSeries1Index, 3.38f, 23000, imgView, R.drawable.ic_fit_swim, "Swim %.2f Km", COLOR_BLUE, true);
    }

    private void addAnimation(final DecoView arcView,
                              int series, float moveTo, int delay,
                              final ImageView imageView, final int imageId,
                              final String format, final int color, final boolean restart) {

        DecoEvent.ExecuteEventListener listener = new DecoEvent.ExecuteEventListener() {
            @Override
            public void onEventStart(DecoEvent event) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), imageId));
                showAvatar(true, imageView);
                mProgress = format;
            }

            @Override
            public void onEventEnd(DecoEvent event) {
                showAvatar(false, imageView);
                if (restart) {
                    setupEvents();
                }
            }
        };

        arcView.addEvent(new DecoEvent.Builder(moveTo)
                .setIndex(series)
                .setDelay(delay)
                .setDuration(5000)
                .setListener(listener)
                .setColor(color)
                .build());
    }

    private void showAvatar(boolean show, View view) {
        AlphaAnimation animation = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
        animation.setDuration(2000);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    private void addFitListener(@NonNull final SeriesItem seriesItem, @NonNull final TextView view) {
        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {

            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                if (mProgress != null) {
                    if (mProgress.contains("%%")) {
                        view.setText(String.format(mProgress, (1.0f - (currentPosition / seriesItem.getMaxValue())) * 100f));
                    } else {
                        view.setText(String.format(mProgress, currentPosition));
                    }
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });
    }
}