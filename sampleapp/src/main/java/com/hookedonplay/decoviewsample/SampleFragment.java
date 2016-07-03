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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;

abstract public class SampleFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName();

    final protected int COLOR_BLUE = Color.parseColor("#AA1D76D2");
    final protected int COLOR_PINK = Color.parseColor("#AAFF4081");
    final protected int COLOR_YELLOW = Color.parseColor("#AAFFC107");
    final protected int COLOR_GREEN = Color.parseColor("#AA07CC07");
    final protected int COLOR_EDGE = Color.parseColor("#22000000");
    final protected int COLOR_BACK = Color.parseColor("#22888888");
    final protected int COLOR_NEUTRAL = Color.parseColor("#FF999999");
    protected boolean mUpdateListeners = true;
    private boolean mInitialized;

    /**
     * Add a listener to update the progress on a TextView
     *
     * @param seriesItem ArcItem to listen for update changes
     * @param view       View to update
     * @param format     String.format to display the progress
     *                   <p/>
     *                   If the string format includes a percentage character we assume that we should set
     *                   a percentage into the string, otherwise the current position is added into the string
     *                   For example if the arc has a min of 0 and a max of 50 and the current position is 20
     *                   Format -> "%.0f%% Complete" -> "40% Complete"
     *                   Format -> "%.1f Km" -> "20.0 Km"
     *                   Format -> "%.0f/40 Levels Complete" -> "20/40 Levels Complete"
     */
    protected void addProgressListener(@NonNull final SeriesItem seriesItem, @NonNull final TextView view, @NonNull final String format) {
        if (format.length() <= 0) {
            throw new IllegalArgumentException("String formatter can not be empty");
        }

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                if (mUpdateListeners) {
                    if (format.contains("%%")) {
                        // We found a percentage so we insert a percentage
                        float percentFilled = (currentPosition - seriesItem.getMinValue()) / (seriesItem.getMaxValue() - seriesItem.getMinValue());
                        view.setText(String.format(format, percentFilled * 100f));
                    } else {
                        view.setText(String.format(format, currentPosition));
                    }
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });
    }

    protected void addProgressRemainingListener(@NonNull final SeriesItem seriesItem, @NonNull final TextView view, @NonNull final String format, final float maxValue) {
        if (format.length() <= 0) {
            throw new IllegalArgumentException("String formatter can not be empty");
        }

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {

            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                if (mUpdateListeners) {
                    if (format.contains("%%")) {
                        // We found a percentage so we insert a percentage
                        view.setText(String.format(format, (1.0f - (currentPosition / seriesItem.getMaxValue())) * 100f));
                    } else {
                        view.setText(String.format(format, maxValue - currentPosition));
                    }
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });
    }

    private boolean createAnimation() {
        if (mInitialized) {
            createTracks();
            if (super.getUserVisibleHint()) {
                setupEvents();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                setDemoFinished(false);
                createAnimation();
            }
        } else {
            stopFragment();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            return;
        }

        mInitialized = true;
        final View replay = getView().findViewById(R.id.imageReplay);
        final View swipe = getView().findViewById(R.id.imageSwipe);
        if (replay != null) {
            replay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation animation = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.rotate_hide);

                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            swipe.setVisibility(View.INVISIBLE);
                            replay.setEnabled(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setDemoFinished(false);
                            createAnimation();
                            replay.setEnabled(true);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    replay.startAnimation(animation);
                }
            });
        }
        createAnimation();
    }

    /**
     * Override to create events for demo. For example move, reveal or effect
     */
    abstract protected void setupEvents();

    /**
     * Override to create all the tracks (arcs) required for current page
     */
    abstract protected void createTracks();

    /**
     * Override to stop all once it is no longer displayed
     */
    protected void stopFragment() {
        final DecoView arcView = getDecoView();

        if (arcView == null || arcView.isEmpty()) {
            return;
        }
        arcView.executeReset();
        arcView.deleteAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        createAnimation();
    }

    protected void setDemoFinished(boolean finished) {
        if (getView() != null) {
            final View continueLayout = getView().findViewById(R.id.layoutContinue);
            final View swipe = getView().findViewById(R.id.imageSwipe);
            if (finished) {
                if (continueLayout != null) {
                    continueLayout.setVisibility(View.VISIBLE);
                }
                if (swipe != null) {
                    swipe.setVisibility(View.VISIBLE);
                }
            } else {
                if (continueLayout != null) {
                    continueLayout.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * Convert base dip into pixels based on the display metrics of the current device
     *
     * @param base dip value
     * @return pixels from base dip
     */
    protected float getDimension(float base) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, base, getResources().getDisplayMetrics());
    }

    protected DecoView getDecoView() {
        if (getView() == null) {
            return null;
        }

        try {
            return (DecoView) getView().findViewById(R.id.dynamicArcView);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Unable to resolve view " + npe.getMessage());
        }
        return null;
    }
}