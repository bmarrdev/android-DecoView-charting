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
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

public class Sample2Fragment extends SampleFragment {
    private int mSeries1Index;
    private int mSeries2Index;

    public Sample2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_generic, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final float seriesMax = 50f;
        final DecoView arcView = getDecoView();
        if (arcView == null) {
            return;
        }
        arcView.deleteAll();
        arcView.configureAngles(280, 0);

        arcView.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, seriesMax, seriesMax)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(32f))
                .build());

        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
                .setRange(0, seriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(32f))
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_OUTER, Color.parseColor("#22000000"), 0.4f))
                .setSeriesLabel(new SeriesLabel.Builder("Percent %.0f%%").build())
                .build();

        mSeries1Index = arcView.addSeries(seriesItem1);

        SeriesItem seriesItem2 = new SeriesItem.Builder(Color.argb(255, 64, 0, 196))
                .setRange(0, seriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(24f))
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_OUTER, Color.parseColor("#22000000"), 0.4f))
                .setSeriesLabel(new SeriesLabel.Builder("Value %.0f").build())
                .build();

        mSeries2Index = arcView.addSeries(seriesItem2);

        final TextView textPercent = (TextView) getView().findViewById(R.id.textPercentage);
        textPercent.setVisibility(View.INVISIBLE);
        textPercent.setText("");
        addProgressListener(seriesItem1, textPercent, "%.0f%%");
    }

    @Override
    protected void setupEvents() {
        final DecoView arcView = getDecoView();

        if (arcView == null || arcView.isEmpty()) {
            throw new IllegalStateException("Unable to add events to empty DecoView");
        }
        arcView.executeReset();

        final TextView textPercent = (TextView) getView().findViewById(R.id.textPercentage);
        final View[] linkedViews = {textPercent};

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
                .setDelay(1000)
                .setDuration(2000)
                .setLinkedViews(linkedViews)
                .build());

        arcView.addEvent(new DecoEvent.Builder(25).setIndex(mSeries1Index).setDelay(3300).build());
        arcView.addEvent(new DecoEvent.Builder(50).setIndex(mSeries1Index).setDelay(8000).build());
        arcView.addEvent(new DecoEvent.Builder(0).setIndex(mSeries1Index).setDelay(13000).setDuration(6000).build());

        arcView.addEvent(new DecoEvent.Builder(5).setIndex(mSeries2Index).setDelay(3600).build());
        arcView.addEvent(new DecoEvent.Builder(30).setIndex(mSeries2Index).setDelay(9000).build());
        arcView.addEvent(new DecoEvent.Builder(0).setIndex(mSeries2Index).setDelay(13000).build());

        arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
                .setDelay(19500)
                .setDuration(2000)
                .setLinkedViews(linkedViews)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent event) {

                    }

                    @Override
                    public void onEventEnd(DecoEvent event) {
                        setDemoFinished(true);
                    }
                })
                .build());
    }
}