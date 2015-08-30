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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;

/**
 * Encapsulates a scheduled operation to perform on the {@link com.hookedonplay.decoviewlib.DecoView}
 * <p/>
 * This event may operate all series of the view or a specific series. For example, an event can:
 * - Move the current location of a data series
 * - Show/Hide the complete View with animation
 * - Apply a special effect such as a spiral reveal
 * <p/>
 * Use the {@link com.hookedonplay.decoviewlib.events.DecoEvent.Builder} class to construct the Event
 * <p/>
 * The caller can add an event ID to listen for notifications of the event starting or finishing or
 * can create a unique listener for each event.
 * {@link com.hookedonplay.decoviewlib.events.DecoEvent.Builder#setListener(ExecuteEventListener)}
 */
@SuppressWarnings("unused")
public class DecoEvent {
    /**
     * Default Identified for the {@link #mEventID} to be used when not set by the user.
     */
    static public final long EVENT_ID_UNSPECIFIED = -1;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = getClass().getSimpleName();
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
    private int mColor;
    private Interpolator mInterpolator;
    private ExecuteEventListener mListener;
    /**
     * Construct the DecoEvent using the attributes set by the Builder
     *
     * @param builder attributes
     */
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
        mColor = builder.mColor;
        mInterpolator = builder.mInterpolator;
        mListener = builder.mListener;

        if (mEventID != EVENT_ID_UNSPECIFIED && mListener == null) {
            Log.w(TAG, "EventID redundant without specifying an event listener");
        }
    }

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

    /**
     * DisplayText is displayed during the Explode effect in the spirit of the effect displayed
     * when reaching a goal in the Google Fit app
     *
     * @return String containing text to display
     */
    public String getDisplayText() {
        return mDisplayText;
    }

    public float getEndPosition() {
        return mEndPosition;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isColorSet() {
        return Color.alpha(mColor) > 0;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    /**
     * Generate notifications for listeners when the event is complete
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

    public enum EventType {
        EVENT_MOVE, /* Move the current position of the chart series */
        EVENT_SHOW, /* Show the chart series using reveal animation */
        EVENT_HIDE, /* Hide the chart series using an animation */
        EVENT_EFFECT, /* Apply effect animation on the series */
        EVENT_COLOR_CHANGE /* Change the color of the series over time */
    }

    /**
     * Interface for client to receive notifications when an Event has started and when an event
     * has finished. The start event is useful for users that are setting a {@link #mDelay}
     */
    public interface ExecuteEventListener {
        void onEventStart(DecoEvent event);

        void onEventEnd(DecoEvent event);
    }

    public static class Builder {
        private EventType mType;
        private long mEventID = EVENT_ID_UNSPECIFIED;
        private long mDelay = 0;
        private DecoDrawEffect.EffectType mEffectType = null;
        private long mFadeDuration = 1000;
        private View[] mLinkedViews = null;
        private long mEffectDuration = -1;
        private int mIndex = -1;
        private int mEffectRotations = 2;
        private String mDisplayText = null;
        private float mEndPosition = 0;
        private int mColor = Color.parseColor("#00000000");
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
         * @param showView  true to animate showing of view
         */
        public Builder(EventType eventType, boolean showView) {
            if (EventType.EVENT_HIDE != eventType && EventType.EVENT_SHOW != eventType) {
                throw new IllegalArgumentException("Invalid arguments for EventType. Use Alternative constructor");
            }
            mType = showView ? EventType.EVENT_SHOW : EventType.EVENT_HIDE;
        }

        public Builder(EventType eventType, int color) {
            if (EventType.EVENT_COLOR_CHANGE != eventType) {
                throw new IllegalArgumentException("Must specify EVENT_COLOR_CHANGE when setting new color");
            }
            mType = eventType;
            mColor = color;
        }

        /**
         * Optional ID set to the event. This is only useful if you are using the same listener on
         * more than one event. Otherwise this may be ignored. The ID is user generated
         *
         * @param eventID numerical identifier for this event
         * @return this
         */
        public Builder setEventID(long eventID) {
            mEventID = eventID;
            return this;
        }

        /**
         * Index of the data series to apply the event to. Some events can be applied to the complete
         * view (ie. Hide/Show), while others ie. {@link com.hookedonplay.decoviewlib.events.DecoEvent.EventType#EVENT_MOVE}
         * can only be applied to one series of data
         *
         * @param indexPosition index of the data series
         * @return this
         */
        public Builder setIndex(int indexPosition) {
            mIndex = indexPosition;
            return this;
        }

        /**
         * Set a delay for the event. This adds the event to be executed at a future time
         *
         * @param delay time to delay execution (ms)
         * @return this
         */
        public Builder setDelay(long delay) {
            mDelay = delay;
            return this;
        }

        /**
         * Duration to execute the event. This call is optional. If executing a move and no
         * duration is set it will be calculated by calculating the default total rotation duration
         * of the DecoView, multiplied by the percent of 1 rotation required to complete the operation
         *
         * @param effectDuration duration in ms
         * @return this
         */
        public Builder setDuration(long effectDuration) {
            mEffectDuration = effectDuration;
            return this;
        }

        /**
         * Duration to fade in or out linked views. {@link #setLinkedViews(View[])}
         *
         * @param fadeDuration duration in ms
         * @return this
         */
        public Builder setFadeDuration(long fadeDuration) {
            mFadeDuration = fadeDuration;
            return this;
        }

        /**
         * The number of rotations (360 degrees) when applying spiral animation
         *
         * @param effectRotations number of rotations
         * @return this
         */
        public Builder setEffectRotations(int effectRotations) {
            mEffectRotations = effectRotations;
            return this;
        }

        /**
         * Set the display text to be used with
         * {@link com.hookedonplay.decoviewlib.charts.DecoDrawEffect.EffectType#drawExplode(Canvas, RectF, float)}
         *
         * @param displayText text to display
         * @return this
         */
        public Builder setDisplayText(String displayText) {
            mDisplayText = displayText;
            return this;
        }

        /**
         * Linked views can be set to link to this event. When the event is started the linked views
         * will be shown or hidden using a fade to compliment the action of the event. The views passed
         * can be any {@link View} objects
         *
         * @param linkedViews Array of Views to apply hide/show
         * @return this
         */
        public Builder setLinkedViews(View[] linkedViews) {
            mLinkedViews = linkedViews;
            return this;
        }

        /**
         * Interpolator algorithm to define the rate of change for the animation of the event
         * {@link Interpolator}
         * If an Interpolator is not set the default Interpolator will be used
         * {@link android.view.animation.AccelerateDecelerateInterpolator}
         * <p/>
         * It is possible to set the default when creating the series using
         * {@link com.hookedonplay.decoviewlib.charts.SeriesItem.Builder#setInterpolator(Interpolator)}
         *
         * @param interpolator Interpolator to use
         * @return this
         * <p/>
         * Example Interpolators
         * {@link android.view.animation.AccelerateInterpolator}
         * {@link android.view.animation.OvershootInterpolator}
         * {@link android.view.animation.AnticipateInterpolator}
         * {@link android.view.animation.LinearInterpolator}
         */
        public Builder setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        /**
         * Set a new color to be faded from the existing
         *
         * @param color new color
         * @return this
         */
        public Builder setColor(int color) {
            mColor = color;
            return this;
        }

        /**
         * Add a listener for user to get notifications of start and finish of the event
         *
         * @param listener event start/finish listener
         * @return this
         */
        public Builder setListener(ExecuteEventListener listener) {
            mListener = listener;
            return this;
        }

        public DecoEvent build() {
            return new DecoEvent(this);
        }
    }
}