package com.afar.osaio.smart.smartlook.cache;

import android.text.TextUtils;

import com.nooie.sdk.cache.BaseCache;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.List;

public class BleDeviceScanCache extends BaseCache<BleDevice> {

    private BleDeviceScanCache() {
    }

    public static BleDeviceScanCache getInstance() {
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

    public void filterLocalDevice(List<String> localBleDeviceIds) {
        if (mCacheMap == null || mCacheMap.isEmpty() || CollectionUtil.isEmpty(localBleDeviceIds)) {
            return;
        }

        for (String deviceId : localBleDeviceIds) {
            if (isExisted(deviceId)) {
                mCacheMap.remove(deviceId);
            }
        }
    }

    private static class BleDeviceCacheHolder {
        public static final BleDeviceScanCache INSTANCE = new BleDeviceScanCache();
    }
}
