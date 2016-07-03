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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class SamplerAdapter extends FragmentStatePagerAdapter {
    private final int mCount = 10;

    public SamplerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DecoFragment();
            case 1:
                return new SampleFitFragment();
            case 2:
                return new SampleFit2Fragment();
            case 3:
                return new SampleInterpolatorsFragment();
            case 4:
                return new Sample3Fragment();
            case 5:
                return new SamplePeopleFragment();
            case 6:
                return new Sample2Fragment();
            case 7:
                return new Sample1Fragment();
            case 8:
                return new Sample4Fragment();
            case 9:
                return new SamplePauseFragment();
            default:
                return new SampleFitFragment();

        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }
}
