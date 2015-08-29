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
package com.hookedonplay.decoviewlib.events;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;

/**
 * Event manager for processing {@link DecoEvent} at the scheduled time (or immediately if no
 * delay is set). This class is also responsible for processing the hide/show fade effects of linked
 * views.
 *
 * Each {@link com.hookedonplay.decoviewlib.DecoView} contains one DecoEventManager, which can
 * handle any number of {@link DecoEvent}
 */
public class DecoEventManager {

    /**
     * Handler to manage firing events at given delays
     */
    private final Handler mHandler = new Handler();

    private ArcEventManagerListener mListener;

    public DecoEventManager(@NonNull ArcEventManagerListener listener) {//DynamicArcView arcView) {
        mListener = listener;
    }

    /**
     * Add a {@link DecoEvent} to the schedule to be processed at the required time
     *
     * @param event DecoEvent to add
     */
    public void add(@NonNull final DecoEvent event) {
        /**
         * Determine if we need to show and linked views attached to the event. This is useful
         * when scheduling an event at a later time and have the linked view automatically
         * faded in when the event is started. The user could do this themselves by setting
         * a listener on the event start.
         */
        final boolean show = (event.getEventType() == DecoEvent.EventType.EVENT_SHOW) ||
                (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT) ||
                (event.getEffectType() == DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT_FILL);

        final boolean ignore = (event.getEventType() == DecoEvent.EventType.EVENT_MOVE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (event.getLinkedViews() != null) {
                        for (View view : event.getLinkedViews()) {

                            // Issue with ICS where View is not displayed after the setVisibility() call if it has no text
                            // This results in subsequent calls to setText also not being visible
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                if (view instanceof TextView) {
                                    TextView textView = (TextView) view;
                                    if (textView.getText().length() <= 0) {
                                        textView.setText(" ");
                                    }
                                }
                            }
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if (!ignore && event.getLinkedViews() != null) {
                    for (final View view : event.getLinkedViews()) {
                        AlphaAnimation anim = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
                        anim.setDuration(event.getFadeDuration());
                        anim.setFillAfter(true);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        view.startAnimation(anim);
                    }
                }
                if (mListener != null) {
                    mListener.onExecuteEventStart(event);
                }

            }
        }, event.getDelay());
    }

    /**
     * Remove any existing delayed messages from the handler
     */
    public void resetEvents() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Callback interface for notification of event to be processed
     */
    public interface ArcEventManagerListener {
        void onExecuteEventStart(@NonNull DecoEvent event);
    }
}