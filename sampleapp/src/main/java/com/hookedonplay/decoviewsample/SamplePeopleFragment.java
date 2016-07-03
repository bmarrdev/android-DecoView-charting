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
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

public class SamplePeopleFragment extends SampleFragment {
    final private int COLOR_BLUE = Color.parseColor("#1D76D2");
    final private int COLOR_PINK = Color.parseColor("#FF4081");
    final private int COLOR_YELLOW = Color.parseColor("#FFC107");
    final private int COLOR_EDGE = Color.parseColor("#22000000");
    final private int COLOR_BACK = Color.parseColor("#0166BB66");
    final float mSeriesMax = 100f;
    private int mSeries1Index;
    private int mSeries2Index;
    private int mSeries3Index;
    private int mBack1Index;

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
        final DecoView decoView = getDecoView();
        final View view = getView();
        if (decoView == null || view == null) {
            return;
        }

        view.setBackgroundColor(Color.argb(255, 128, 218, 128));

        decoView.executeReset();
        decoView.deleteAll();

        float circleInset = getDimension(23) - (getDimension(46) * 0.3f);
        SeriesItem seriesBack1Item = new SeriesItem.Builder(COLOR_BACK)
                .setRange(0, mSeriesMax, mSeriesMax)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .setInset(new PointF(circleInset, circleInset))
                .build();

        mBack1Index = decoView.addSeries(seriesBack1Item);

        SeriesItem series1Item = new SeriesItem.Builder(COLOR_BLUE)
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(46))
                .setSeriesLabel(new SeriesLabel.Builder("Men").build())
                .setCapRounded(false)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, COLOR_EDGE, 0.3f))
                .setShowPointWhenEmpty(false)
                .build();

        mSeries1Index = decoView.addSeries(series1Item);

        SeriesItem series2Item = new SeriesItem.Builder(COLOR_PINK)
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(46))
                .setSeriesLabel(new SeriesLabel.Builder("Women").build())
                .setCapRounded(false)
                        //.setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, COLOR_EDGE, 0.3f))
                .setShowPointWhenEmpty(false)
                .build();

        mSeries2Index = decoView.addSeries(series2Item);

        SeriesItem series3Item = new SeriesItem.Builder(COLOR_YELLOW)
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(46))
                .setSeriesLabel(new SeriesLabel.Builder("Children").build())
                .setCapRounded(false)
                        //.setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, COLOR_EDGE, 0.3f))
                .setShowPointWhenEmpty(false)
                .build();

        mSeries3Index = decoView.addSeries(series3Item);

    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();
        final View view = getView();
        if (arcView == null || arcView.isEmpty() || view == null) {
            return;
        }
        arcView.executeReset();

        final ImageView imgView = (ImageView) view.findViewById(R.id.imageViewAvatar);
        imgView.setImageDrawable(null);
        imgView.setVisibility(View.INVISIBLE);

        addAnimation(arcView, mSeries1Index, 19, 3000, imgView, R.drawable.ic_avatar_man, COLOR_BLUE);
        addAnimation(arcView, mSeries2Index, 45, 11000, imgView, R.drawable.ic_avatar_woman, COLOR_PINK);

        arcView.addEvent(new DecoEvent.Builder(64)
                .setIndex(mSeries1Index)
                .setDelay(11000)
                .setDuration(5000)
                .build());

        addAnimation(arcView, mSeries3Index, 36, 19000, imgView, R.drawable.ic_avatar_child, COLOR_YELLOW);

        arcView.addEvent(new DecoEvent.Builder(79)
                .setIndex(mSeries2Index)
                .setDelay(19000)
                .setDuration(5000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(100)
                .setIndex(mSeries1Index)
                .setDelay(19000)
                .setDuration(5000)
                .build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_COLOR_CHANGE, COLOR_BACK)
                .setIndex(mBack1Index)
                .setDelay(27000)
                .setDuration(2000)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent event) {
                        imgView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_avatar_group));
                        showAvatar(true, imgView);
                    }

                    @Override
                    public void onEventEnd(DecoEvent event) {

                    }
                })
                .build());

        addFinishAnimation(arcView, mSeries3Index, 1250, 30000, imgView);
        addFinishAnimation(arcView, mSeries2Index, 1500, 30000, null);
        addFinishAnimation(arcView, mSeries1Index, 1750, 30000, null);
    }

    private void addFinishAnimation(final DecoView arcView, final int series, final int duration, int delay, final View view) {
        arcView.addEvent(new DecoEvent.Builder(0)
                .setIndex(series)
                .setDelay(delay)
                .setDuration(duration)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent event) {
                        arcView.getChartSeries(series).getSeriesItem().setSeriesLabel(null);
                    }

                    @Override
                    public void onEventEnd(DecoEvent event) {
                        if (view != null) {
                            showAvatar(false, view);
                            setDemoFinished(true);
                        }
                    }
                })
                .build());
    }

    private void addAnimation(final DecoView arcView,
                              int series, float moveTo, int delay,
                              final ImageView imageView, final int imageId,
                              final int color) {
        DecoEvent.ExecuteEventListener listener = new DecoEvent.ExecuteEventListener() {
            @Override
            public void onEventStart(DecoEvent event) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), imageId));

                showAvatar(true, imageView);

                arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_COLOR_CHANGE, color)
                        .setIndex(mBack1Index)
                        .setDuration(2000)
                        .build());
            }

            @Override
            public void onEventEnd(DecoEvent event) {
                showAvatar(false, imageView);

                arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_COLOR_CHANGE, COLOR_BACK)
                        .setIndex(mBack1Index)
                        .setDuration(2000)
                        .build());
            }

        };

        arcView.addEvent(new DecoEvent.Builder(moveTo)
                .setIndex(series)
                .setDelay(delay)
                .setDuration(5000)
                .setListener(listener)
                .build());
    }

    private void showAvatar(boolean show, View view) {
        AlphaAnimation animation = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }
}