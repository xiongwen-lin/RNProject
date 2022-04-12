package com.afar.osaio.smart.electrician.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class DeviceTypeAdapter extends FragmentStatePagerAdapter {
    private List<String> mDeviceType;
    private List<Fragment> mDeviceTypeFragment;

    public DeviceTypeAdapter(FragmentManager fm, List<String> deviceType, List<Fragment> deviceTypeFragment) {
        super(fm);
        this.mDeviceType = deviceType;
        this.mDeviceTypeFragment = deviceTypeFragment;
    }

    public DeviceTypeAdapter(FragmentManager fm, List<Fragment> deviceTypeFragment) {
        super(fm);
        this.mDeviceTypeFragment = deviceTypeFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return mDeviceTypeFragment.get(position); //返回碎片集合的第几项
    }

    @Override
    public int getCount() {
        return mDeviceTypeFragment.size();   //返回碎片集合大小
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mDeviceType.get(position);  //返回标题的第几项
    }
}
