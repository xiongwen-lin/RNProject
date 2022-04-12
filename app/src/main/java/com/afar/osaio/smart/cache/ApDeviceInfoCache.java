package com.afar.osaio.smart.cache;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;

import java.util.ArrayList;
import java.util.List;

public class ApDeviceInfoCache extends BaseCache<ApDeviceInfo> {

    public static final String AP_DEVICE_KEY_OPEN_BIND_DEVICE = "AP_DEVICE_KEY_OPEN_BIND_DEVICE";
    public static final String AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING = "AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING";
    public static final String AP_DEVICE_KEY_OPEN_DEV_INFO = "AP_DEVICE_KEY_OPEN_DEV_INFO";
    public static final String AP_DEVICE_KEY_MULTI_DATA = "AP_DEVICE_KEY_MULTI_DATA";
    public static final String AP_DEVICE_KEY_OPEN_STATUS = "AP_DEVICE_KEY_OPEN_STATUS";

    private List<ApHelper.OnUpdateApDeviceCacheListener> mOnUpdateApDeviceCacheListeners = new ArrayList<>();

    private ApDeviceInfoCache() {
    }

    private static class ApDeviceInfoCacheHolder {
        public static final ApDeviceInfoCache INSTANCE = new ApDeviceInfoCache();
    }

    public static ApDeviceInfoCache getInstance() {
        return ApDeviceInfoCacheHolder.INSTANCE;
    }

    public void updateApDeviceCache(String deviceSsid, BindDevice device) {
        if (TextUtils.isEmpty(deviceSsid) || device == null) {
            return;
        }
        ApDeviceInfo apDeviceInfo = getCacheById(deviceSsid);
        if (apDeviceInfo == null) {
            apDeviceInfo = new ApDeviceInfo();
            addCache(deviceSsid, apDeviceInfo);
        }
        apDeviceInfo.setDeviceSsid(deviceSsid);
        if (apDeviceInfo.getBindDevice() == null) {
            apDeviceInfo.setBindDevice(device);
            addCache(deviceSsid, apDeviceInfo);
            notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_BIND_DEVICE, apDeviceInfo);
        }
    }

    public void updateApDeviceCache(String deviceSsid, DevAllSettingsV2 devAllSettingsV2) {
        if (TextUtils.isEmpty(deviceSsid) || devAllSettingsV2 == null) {
            return;
        }
        ApDeviceInfo apDeviceInfo = getCacheById(deviceSsid);
        if (apDeviceInfo == null) {
            apDeviceInfo = new ApDeviceInfo();
            addCache(deviceSsid, apDeviceInfo);
        }
        apDeviceInfo.setDeviceSsid(deviceSsid);
        apDeviceInfo.setDevAllSettingsV2(devAllSettingsV2);
        if (devAllSettingsV2.commSettings != null && apDeviceInfo.getBindDevice() != null) {
            apDeviceInfo.getBindDevice().setOpen_status(devAllSettingsV2.commSettings.sleep == ConstantValue.CMD_STATE_ENABLE ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON);
        }
        addCache(deviceSsid, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING, apDeviceInfo);
    }

    public void updateApDeviceCache(String deviceSsid, DevInfo devInfo) {
        if (TextUtils.isEmpty(deviceSsid) || devInfo == null) {
            return;
        }
        ApDeviceInfo apDeviceInfo = getCacheById(deviceSsid);
        if (apDeviceInfo == null) {
            apDeviceInfo = new ApDeviceInfo();
            addCache(deviceSsid, apDeviceInfo);
        }
        apDeviceInfo.setDeviceSsid(deviceSsid);
        apDeviceInfo.setDevInfo(devInfo);
        if (apDeviceInfo.getBindDevice() != null) {
            apDeviceInfo.getBindDevice().setType(devInfo.model);
            apDeviceInfo.getBindDevice().setModel(devInfo.model);
            apDeviceInfo.getBindDevice().setName(devInfo.model);
        }
        addCache(deviceSsid, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_DEV_INFO, apDeviceInfo);
    }

    public void updateApDeviceCache(String deviceSsid, Bundle data) {
        if (TextUtils.isEmpty(deviceSsid) || data == null) {
            return;
        }
        ApDeviceInfo apDeviceInfo = getCacheById(deviceSsid);
        if (apDeviceInfo == null) {
            apDeviceInfo = new ApDeviceInfo();
            addCache(deviceSsid, apDeviceInfo);
        }
        apDeviceInfo.setDeviceSsid(deviceSsid);
        if (data.containsKey(AP_DEVICE_KEY_OPEN_STATUS)) {
            if (apDeviceInfo.getBindDevice() != null) {
                apDeviceInfo.getBindDevice().setOpen_status(data.getInt(AP_DEVICE_KEY_OPEN_STATUS));
            }
            if (apDeviceInfo.getDevAllSettingsV2() != null && apDeviceInfo.getDevAllSettingsV2().commSettings != null) {
                apDeviceInfo.getDevAllSettingsV2().commSettings.sleep = data.getInt(AP_DEVICE_KEY_OPEN_STATUS) == ApiConstant.OPEN_STATUS_ON ? ConstantValue.CMD_STATE_DISABLE : ConstantValue.CMD_STATE_ENABLE;
            }
        }
        addCache(deviceSsid, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_MULTI_DATA, apDeviceInfo);
    }

    public void notifyApDeviceCacheUpdate(String key, ApDeviceInfo apDeviceInfo) {
        if (CollectionUtil.isEmpty(mOnUpdateApDeviceCacheListeners)) {
            return;
        }
        for (ApHelper.OnUpdateApDeviceCacheListener listener : CollectionUtil.safeFor(mOnUpdateApDeviceCacheListeners)) {
            if (listener != null) {
                listener.onUpdateCache(key, apDeviceInfo);
            }
        }
    }

    public void addApDeviceCacheListener(ApHelper.OnUpdateApDeviceCacheListener listener) {
        if (listener == null) {
            return;
        }
        if (mOnUpdateApDeviceCacheListeners == null) {
            mOnUpdateApDeviceCacheListeners = new ArrayList<>();
        }
        if (mOnUpdateApDeviceCacheListeners.contains(listener)) {
            return;
        }
        mOnUpdateApDeviceCacheListeners.add(listener);
    }

    public void removeApDeviceCacheListener(ApHelper.OnUpdateApDeviceCacheListener listener) {
        if (listener == null || CollectionUtil.isEmpty(mOnUpdateApDeviceCacheListeners)) {
            return;
        }
        if (mOnUpdateApDeviceCacheListeners.contains(listener)) {
            mOnUpdateApDeviceCacheListeners.remove(listener);
        }
    }

    public void clearApDeviceCacheListener() {
        if (CollectionUtil.isEmpty(mOnUpdateApDeviceCacheListeners)) {
            return;
        }
        mOnUpdateApDeviceCacheListeners.clear();
    }

}
