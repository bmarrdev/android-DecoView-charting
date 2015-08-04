package com.hookedonplay.decoviewsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class SamplerAdapter extends FragmentStatePagerAdapter {
    public SamplerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        Fragment fragment;
        switch(position) {
//            case 6:
//                return new Sample20Fragment();// SampleFitFragment();
//            case 2:
//                return new SampleInterpolatorsFragment();
//
//            case 1:
//                return new SampleTest4Fragment();
//            //return new SampleFitFragment();
//
//            case 4:
//                return new SampleReverseFragment();
//            case 5:
//                return new Sample20Fragment();
//            case 7:
//                fragment = new SampleGenericFragment();
//                return fragment;
//            case 3:
////                    bundle.putInt(SampleGenericFragment.STYLE_KEY, SampleGenericFragment.STYLE_C);
////                    fragment = new SampleGenericFragment();
////                    fragment.setArguments(bundle);
////                    return fragment;
//                return new SampleTest3Fragment();
            case 2:
                return new SampleInterpolatorsFragment();
            case 1:
//                    bundle.putInt(SampleGenericFragment.STYLE_KEY, SampleGenericFragment.STYLE_D);
//                    fragment = new SampleGenericFragment();
//                    fragment.setArguments(bundle);
//                    return fragment;
                return new DecoFragment();
            // return new SampleHealthFragment();
        }
        return new SampleFitFragment();
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }
}
