package com.afar.osaio.smart.mixipc.profile.cache;

import android.text.TextUtils;

import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.cache.BaseCache;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

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

    public void updateBleDevice(List<ScanResult> resultList) {
        if (CollectionUtil.isEmpty(resultList)) {
            return;
        }
        for (ScanResult result : resultList) {
            //boolean isValidBle = result != null && result.getDevice() != null && result.getDevice().getName() != null && result.getDevice().getName().contains(ConstantValue.AP_FUTURE_PREFIX);
            boolean isValidBle = result != null && result.getDevice() != null && result.getDevice().getName() != null;
            if (isValidBle) {
                BleDevice bleDevice = new BleDevice();
                bleDevice.setDevice(result.getDevice());
                updateCache(bleDevice);
            }
        }
    }

    private static class BleDeviceCacheHolder {
        public static final BleDeviceScanCache INSTANCE = new BleDeviceScanCache();
    }
}
