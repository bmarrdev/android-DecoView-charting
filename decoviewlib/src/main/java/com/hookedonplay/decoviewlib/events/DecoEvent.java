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

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;

@SuppressWarnings("unused")
public class DecoEvent {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = getClass().getSimpleName();

    public enum EventType {
        EVENT_MOVE, /* Move the current position of the chart series*/
        // --Commented out by Inspection (5/08/2015 7:49 AM):EVENT_MOVE_START,
        EVENT_SHOW, /* Show the chart series using reveal animation */
        EVENT_HIDE, /* hide the series */
        EVENT_EFFECT /* Apply effect animation on the series*/
    }

    static public final long EVENT_ID_UNSPECIFIED = 0;

    private EventType mType;
    private long mEventID;
    private long mDelay;
    private DecoDrawEffect.EffectType mEffectType;
    private long mFadeDuration;
    private View[] mLinkedViews;
    private long mEffectDuration;
    private int mIndexPosition;
    private int mEffectRotations;
    private String mDisplayText;
    private float mEndPosition;

    private Interpolator mInterpolator;
    private ExecuteEventListener mListener;

    public EventType getEventType() {
        return mType;
    }

    /**
     * ID to identify Event when implementing {@link ExecuteEventListener} interface
     * <p/>
     * ID is specified by the client when building Event. If an ID is not specified it will be set to
     * {@link #EVENT_ID_UNSPECIFIED}.
     *
     * @return event identifier
     */
    public long getEventID() {
        return mEventID;
    }

    public long getDelay() {
        return mDelay;
    }

    public DecoDrawEffect.EffectType getEffectType() {
        return mEffectType;
    }

    public long getFadeDuration() {
        return mFadeDuration;
    }

    public View[] getLinkedViews() {
        return mLinkedViews;
    }

    public long getEffectDuration() {
        return mEffectDuration;
    }

    public int getIndexPosition() {
        return mIndexPosition;
    }

    public int getEffectRotations() {
        return mEffectRotations;
    }

    public String getDisplayText() {
        return mDisplayText;
    }

    public float getEndPosition() {
        return mEndPosition;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public static class Builder {
        private EventType mType;
        private long mEventID = EVENT_ID_UNSPECIFIED;
        private long mDelay = 0;
        private DecoDrawEffect.EffectType mEffectType = null;
        private long mFadeDuration = 1000;
        private View[] mLinkedViews = null;
        private long mEffectDuration = 2000;
        private int mIndex = -1;
        private int mEffectRotations = 2;
        private String mDisplayText = null;
        private float mEndPosition = 0;
        private Interpolator mInterpolator = null;
        private ExecuteEventListener mListener = null;

        /**
         * Construct an {@link DecoEvent} of EventType.EVENT_MOVE to specify a new end position for
         * an data series.
         *
         * @param endPosition new End position
         */
        public Builder(float endPosition) {
            mType = EventType.EVENT_MOVE;
            mEndPosition = endPosition;
        }

        /**
         * Construct an {@link DecoEvent} of EventType.EVENT_EFFECT
         *
         * @param effectType Type of effect
         */
        public Builder(@NonNull DecoDrawEffect.EffectType effectType) {
            mType = EventType.EVENT_EFFECT;
            mEffectType = effectType;
        }

        /**
         * Construct an {@link DecoEvent} of EventType.EVENT_SHOW or EventType.EVENT_HIDE. Note the
         * eventType is ignored and only passed to make it explicit as to what to use this
         * constructor for
         *
         * @param eventType Type of event (HIDE/SHOW)
         * @param showView true to animate showing of view
         */
        public Builder(EventType eventType, boolean showView) {
            if (EventType.EVENT_HIDE != eventType && EventType.EVENT_SHOW != eventType) {
                throw new IllegalArgumentException("Invalid arguments for EventType. Use Alternative constructor");
            }
            mType = showView ? EventType.EVENT_SHOW : EventType.EVENT_HIDE;
        }

        public Builder setEventID(long eventID) {
            mEventID = eventID;
            return this;
        }

        public Builder setIndex(int indexPosition) {
            mIndex = indexPosition;
            return this;
        }

        public Builder setDelay(long delay) {
            mDelay = delay;
            return this;
        }

        public Builder setDuration(long effectDuration) {
            mEffectDuration = effectDuration;
            return this;
        }

        public Builder setFadeDuration(long fadeDuration) {
            mFadeDuration = fadeDuration;
            return this;
        }

        public Builder setEffectRotations(int effectRotations) {
            mEffectRotations = effectRotations;
            return this;
        }

        public Builder setDisplayText(String displayText) {
            mDisplayText = displayText;
            return this;
        }

        public Builder setLinkedViews(View[] linkedViews) {
            mLinkedViews = linkedViews;
            return this;
        }

        public Builder setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        public Builder setListener(ExecuteEventListener listener) {
            mListener = listener;
            return this;
        }

        public DecoEvent build() {
            return new DecoEvent(this);
        }
    }

    private DecoEvent(Builder builder) {
        mType = builder.mType;
        mEventID = builder.mEventID;
        mDelay = builder.mDelay;
        mEffectType = builder.mEffectType;
        mFadeDuration = builder.mFadeDuration;
        mLinkedViews = builder.mLinkedViews;
        mEffectDuration = builder.mEffectDuration;
        mIndexPosition = builder.mIndex;
        mEffectRotations = builder.mEffectRotations;
        mDisplayText = builder.mDisplayText;
        mEndPosition = builder.mEndPosition;
        mInterpolator = builder.mInterpolator;
        mListener = builder.mListener;

        if (mEventID != EVENT_ID_UNSPECIFIED && mListener == null) {
            Log.w(TAG, "EventID redundant without specifying an event listener");
        }
    }

    /**
     * Interface for client to receive notifications when an Event has started and when an event
     * has finished. The start event is useful for users that are setting a {@link #mDelay}
     */
    public interface ExecuteEventListener {
        void onEventStart(DecoEvent event);

        void onEventEnd(DecoEvent event);
    }

    /**
     * package-private generate notifications for listeners when the event is complete
     */
    public void notifyEndListener() {
        if (mListener != null) {
            mListener.onEventEnd(this);
        }
    }

    /**
     * Event is starting
     */
    public void notifyStartListener() {
        if (mListener != null) {
            mListener.onEventStart(this);
        }
    }
}