package com.afar.osaio.smart.electrician.smartScene.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class SmartSceneTypeAdapter extends FragmentStatePagerAdapter {
    private List<String> mSmartSceneType;
    private List<Fragment> mSmartSceneTypeFragment;

    public SmartSceneTypeAdapter(FragmentManager fm, List<String> smartSceneType, List<Fragment> smartSceneTypeFragment) {
        super(fm);
        this.mSmartSceneType = smartSceneType;
        this.mSmartSceneTypeFragment = smartSceneTypeFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return mSmartSceneTypeFragment.get(position);
    }

    @Override
    public int getCount() {
        return mSmartSceneTypeFragment.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mSmartSceneType.get(position);
    }
}
