package com.afar.osaio.smart.electrician.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class GroupTypeAdapter extends FragmentStatePagerAdapter {
    private List<String> mGroupType;
    private List<Fragment> mGroupTypeFragment;

    public GroupTypeAdapter(FragmentManager fm, List<String> groupType, List<Fragment> groupTypeFragment) {
        super(fm);
        this.mGroupType = groupType;
        this.mGroupTypeFragment = groupTypeFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return mGroupTypeFragment.get(position);
    }

    @Override
    public int getCount() {
        return mGroupTypeFragment.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mGroupType.get(position);
    }
}
