package com.afar.osaio.smart.mixipc.profile.cache;

import android.text.TextUtils;

import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.cache.BaseCache;

import java.util.List;

public class BleDeviceCache extends BaseCache<BleDevice> {

    private BleDeviceCache() {
    }

    private static class BleDeviceCacheHolder {
        public static final BleDeviceCache INSTANCE = new BleDeviceCache();
    }

    public static BleDeviceCache getInstance() {
        return BleDeviceCacheHolder.INSTANCE;
    }

    public void updateCache(BleDevice device) {
        if (device == null || device.getDevice() == null || TextUtils.isEmpty(device.getDevice().getAddress())) {
            return;
        }
        if (isExisted(device.getDevice().getAddress()) && getCacheById(device.getDevice().getAddress()) != null) {
            BleDevice bleDevice = getCacheById(device.getDevice().getAddress());
            bleDevice.setDevice(device.getDevice());
            bleDevice.setRssi(device.getRssi());
            bleDevice.setDeviceType(device.getDeviceType());
            bleDevice.setInitState(device.getInitState());
            bleDevice.setUpdateTime(device.getUpdateTime());
            bleDevice.setSec(device.getSec());
            addCache(device.getDevice().getAddress(), bleDevice);
        } else {
            addCache(device.getDevice().getAddress(), device);
        }
    }

    public void updateDevicesInLocalCache(List<BleDevice> devices) {
        for (BleDevice device : CollectionUtil.safeFor(devices)) {
            if (device != null && device.getDevice() != null && isExisted(device.getDevice().getAddress())) {
                updateCache(device);
            }
        }
    }

}
