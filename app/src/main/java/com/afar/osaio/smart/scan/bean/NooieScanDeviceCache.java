package com.afar.osaio.smart.scan.bean;

import android.text.TextUtils;

import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NooieScanDeviceCache
 *
 * @author Administrator
 * @date 2019/4/22
 */
public class NooieScanDeviceCache {

    private Map<String, BindDevice> mCacheMap = new ConcurrentHashMap<>();

    private NooieScanDeviceCache() {
    }

    private static class NooieScanDeviceCacheHolder {
        private static final NooieScanDeviceCache INSTANCE = new NooieScanDeviceCache();
    }

    public static NooieScanDeviceCache getInstance() {
        return NooieScanDeviceCacheHolder.INSTANCE;
    }

    public void updateDevInfo(BindDevice entity) {
        if (entity == null) {
            return;
        }
        mCacheMap.put(entity.getUuid(), entity);
    }

    public void updateSearchDevList(List<BindDevice> list) {
        if (list == null) {
            return;
        }

        for (BindDevice entity : list) {
            updateDevInfo(entity);
        }
    }

    public List<BindDevice> getDeviceInfoEntityList() {
        List<BindDevice> list = new ArrayList<>(mCacheMap.values());
        return list;
    }

    public List<BindDevice> getBindByOtherDeviceInfoEntityList() {
        List<BindDevice> list = new ArrayList<>();
        for (BindDevice device : CollectionUtil.safeFor(new ArrayList<>(mCacheMap.values()))) {
            if (device != null && device.getBind_type() == ApiConstant.BIND_TYPE_SHARE) {
                list.add(device);
            }
        }
        return list;
    }

    public BindDevice getDeviceInfoEntity(String deviceId) {
        return mCacheMap.get(deviceId);
    }

    public List<BindDevice> filterAvailableDevice(List<BindDevice> devices) {
        List<BindDevice> availableDevices = new ArrayList<>();
        if (devices != null) {
            for (BindDevice device : devices) {
                if (isDeviceCanAdd(device)) {
                    availableDevices.add(device);
                }
            }
        }

        return availableDevices;
    }

    public List<BindDevice> filterAvailableDevice() {
        List<BindDevice> availableDevices = new ArrayList<>();
        for (Map.Entry<String,BindDevice> device : mCacheMap.entrySet()) {
            if (isDeviceCanAdd(device.getValue())) {
                availableDevices.add(device.getValue());
            }
        }

        return availableDevices;
    }

    private boolean isDeviceCanAdd(BindDevice device) {
        return device != null && !TextUtils.isEmpty(device.getUuid()) && device.getBind_type() == ApiConstant.BIND_TYPE_OWNER && device.getOnline() == ApiConstant.ONLINE_STATUS_ON;
    }

    public void clear() {
        if (mCacheMap.size() > 0) {
            mCacheMap.clear();
        }
    }
}

