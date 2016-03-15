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
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.hookedonplay.decoviewlib.events.DecoEvent.EventType;

public class Sample1Fragment extends SampleFragment {
    private int mSeries1Index;

    public Sample1Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample1, container, false);
    }

    @Override
    protected void createTracks() {
        setDemoFinished(false);
        final DecoView decoView = getDecoView();
        final View view = getView();
        if (decoView == null || view == null) {
            return;
        }
        decoView.deleteAll();
        decoView.configureAngles(280, 0);

        final float seriesMax = 50f;
        decoView.addSeries(new SeriesItem.Builder(Color.argb(255, 64, 255, 64), Color.argb(255, 255, 0, 0))
                .setRange(0, seriesMax, seriesMax)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(50f))
                .build());

        decoView.addSeries(new SeriesItem.Builder(Color.argb(255, 0, 0, 0))
                .setRange(0, seriesMax, seriesMax)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(24f))
                .build());

        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 255, 64), Color.argb(255, 255, 0, 0))
                .setRange(0, seriesMax, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(50f))
                .setCapRounded(true)
                .setShowPointWhenEmpty(true)
                .build();

        mSeries1Index = decoView.addSeries(seriesItem1);

        final TextView textPercent = (TextView) view.findViewById(R.id.textPercentage);
        textPercent.setText("");
        addProgressListener(seriesItem1, textPercent, "%.0f%%");
    }

    @Override
    protected void setupEvents() {
        final DecoView decoView = getDecoView();
        if (decoView == null || decoView.isEmpty()) {
            throw new IllegalStateException("Unable to add events to empty DecoView");
        }

        decoView.executeReset();
        decoView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
                .setDelay(1000)
                .setDuration(2000)
                .build());

        decoView.addEvent(new DecoEvent.Builder(25).setIndex(mSeries1Index).setDelay(3300).build());
        decoView.addEvent(new DecoEvent.Builder(50).setIndex(mSeries1Index).setDelay(9000).build());
        decoView.addEvent(new DecoEvent.Builder(0).setIndex(mSeries1Index).setDelay(13000).setDuration(6000).build());
        decoView.addEvent(new DecoEvent.Builder(EventType.EVENT_HIDE, false)
                .setDelay(19000)
                .setDuration(2000)
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