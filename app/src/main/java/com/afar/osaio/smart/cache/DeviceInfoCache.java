package com.afar.osaio.smart.cache;

import android.text.TextUtils;

import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.cache.DeviceConnectionCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class DeviceInfoCache {
    private static final String TAG = DeviceInfoCache.class.getSimpleName();

    private ConcurrentHashMap<String, DeviceInfo> mCacheMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> mModeCacheMap = new ConcurrentHashMap<>();

    private DeviceInfoCache() {
    }

    private static class DeviceInfoCacheHolder {
        private static final DeviceInfoCache INSTANCE = new DeviceInfoCache();
    }

    public static DeviceInfoCache getInstance() {
        return DeviceInfoCacheHolder.INSTANCE;
    }

    private void initCacheMap() {
        if (mCacheMap == null) {
            mCacheMap = new ConcurrentHashMap<>();
        }
        if (mModeCacheMap == null) {
            mModeCacheMap = new ConcurrentHashMap<>();
        }
    }

    public Map<String, DeviceInfo> getCacheMap() {
        return mCacheMap;
    }

    public void updateCache(DeviceInfo info) {
        if (info == null) {
            return;
        }

        initCacheMap();

        DeviceInfo cacheInfo = mCacheMap.get(info.getDeviceId());
        if (cacheInfo == null) {
            mCacheMap.put(info.getDeviceId(), info);
        } else {
            cacheInfo.setDeviceId(info.getDeviceId());
            cacheInfo.setNooieDevice(info.getNooieDevice());
            cacheInfo.setVersionCode(info.getVersionCode());
            cacheInfo.setModel(info.getModel());
            cacheInfo.setOpenCamera(info.isOpenCamera());
            cacheInfo.setOpenCloud(info.isOpenCloud());
            cacheInfo.setDevicePlatform(info.getDevicePlatform());
            cacheInfo.setCloudTime(info.getCloudTime());
            mCacheMap.put(info.getDeviceId(), cacheInfo);
        }
        updateDeviceModel(info.getDeviceId(), info.getModel());
    }

    public void updateCache(List<DeviceInfo> infos) {
        if (infos == null) {
            return;
        }

        for (DeviceInfo info : infos) {
            updateCache(info);
        }
    }

    public void removeCacheById(String deviceId) {
        if (mCacheMap != null && mCacheMap.containsKey(deviceId)) {
            mCacheMap.remove(deviceId);
        }
    }

    public void clearCacheMap() {
        if (mCacheMap != null) {
            mCacheMap.clear();
        }
    }

    public void clearCache() {
        DeviceConnectionCache.getInstance().clearCache();
        DeviceListCache.getInstance().clearCache();
        DeviceConfigureCache.getInstance().clearCache();
        if (mCacheMap != null) {
            mCacheMap.clear();
            mCacheMap = null;
        }

        if (mModeCacheMap != null) {
            mModeCacheMap.clear();
            mModeCacheMap = null;
        }
    }

    public DeviceInfo getDeviceInfoByDeviceId(String devId) {
        return mCacheMap != null ? mCacheMap.get(devId) : null;
    }

    public List<DeviceInfo> getDeviceInfosByDeviceIds(List<String> devIds) {
        List<DeviceInfo> list = new ArrayList<>();
        if (mCacheMap != null && devIds != null) {
            for (String devId : devIds) {
                DeviceInfo deviceInfo = mCacheMap.get(devId);
                if (deviceInfo != null) {
                    list.add(deviceInfo);
                }
            }
        }
        return list;
    }

    public List<String> getDeviceInfoIds(List<DeviceInfo> devices) {
        List<String> ids = new ArrayList<>();
        for(DeviceInfo device : devices) {
            if (device != null && !TextUtils.isEmpty(device.getDeviceId())) {
                ids.add(device.getDeviceId());
            }
        }
        return ids;
    }

    public List<String> filterDeviceIdByPlatform(List<String> ids, int platform) {
        if (CollectionUtil.isEmpty(ids) || mCacheMap == null) {
            return null;
        }
        List<String> deviceIds = new ArrayList<>();
        for (String id : CollectionUtil.safeFor(ids)) {
            if (mCacheMap.containsKey(id) && mCacheMap.get(id).getDevicePlatform() == platform) {
                deviceIds.add(id);
            }
        }

        return deviceIds;
    }

    public List<String> getAllDeviceIds() {
        if (mCacheMap == null || mCacheMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> deviceIds = new ArrayList<>();
        for(Map.Entry<String, DeviceInfo> entry : mCacheMap.entrySet()){
            if (entry != null && entry.getValue() != null) {
                deviceIds.add(entry.getValue().getDeviceId());
            }
        }
        return deviceIds;
    }

    public List<DeviceInfo> getAllDeviceInfo() {
        if (mCacheMap == null || mCacheMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<DeviceInfo> list = new ArrayList<>();
        for(Map.Entry<String, DeviceInfo> entry : mCacheMap.entrySet()) {
            if (entry.getValue() != null) {
                list.add(entry.getValue());
            }
        }

        return list;
    }

    public void replaceCaches() {
        if (mCacheMap != null) {
            mCacheMap.clear();
        }
        coverntAndUpdateCache(DeviceListCache.getInstance().getAllCache(), true);
    }

    public List<DeviceInfo> coverntAndUpdateCache(List<ListDeviceItem> devices, boolean isUpdate) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        for(ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceId(device.getDeviceId());
            deviceInfo.setOpenCamera(device.getOpenStatus() == ApiConstant.OPEN_STATUS_ON ? true : false);
            deviceInfo.setOpenCloud(device.isOpenCloud());
            deviceInfo.setDevicePlatform(device.getDevicePlatform());
            deviceInfo.setVersionCode(device.getVersion());
            deviceInfo.setModel(device.getModel());
            //nooie smart todo add looprecordstatus
            deviceInfo.setLoopRecordStatus(true);
            deviceInfo.setCloudTime(device.getCloudTime());
            deviceInfo.setNooieDevice(device.getBindDevice());
            deviceInfos.add(deviceInfo);
        }

        if (isUpdate) {
            updateCache(deviceInfos);
        }

        return deviceInfos;
    }

    public List<DeviceInfo> getAllDeviceInfoByPlatform(int platform) {
        if (mCacheMap == null || mCacheMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<DeviceInfo> list = new ArrayList<>();
        for(Map.Entry<String, DeviceInfo> entry : mCacheMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getDevicePlatform() == platform) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public List<DeviceInfo> filterDeviceInfoByPlatform(List<DeviceInfo> deviceInfos, int platform) {
        List<DeviceInfo> deviceInfoList = new ArrayList<>();
        for(DeviceInfo deviceInfo : CollectionUtil.safeFor(deviceInfos)) {
            if (deviceInfo.getDevicePlatform() == platform) {
                deviceInfoList.add(deviceInfo);
            }
        }

        return deviceInfoList;
    }

    public void updateDeviceModel(String deviceId, String model) {
        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(model)) {
            return;
        }

        initCacheMap();

        mModeCacheMap.put(deviceId, model);
    }

    public String getDeviceModelById(String deviceId) {
        if (TextUtils.isEmpty(deviceId) || mModeCacheMap == null) {
            return "";
        }

        return mModeCacheMap.get(deviceId);
    }

    public void clearDeviceModel() {
        if (mModeCacheMap != null) {
            mModeCacheMap.clear();
        }
    }
}
