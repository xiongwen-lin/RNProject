package com.afar.osaio.smart.cache;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.bean.BleApDeviceInfo;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;

import java.util.ArrayList;
import java.util.List;

public class BleApDeviceInfoCache extends BaseCache<BleApDeviceInfo> {

    public static final String AP_DEVICE_KEY_OPEN_BIND_DEVICE = "AP_DEVICE_KEY_OPEN_BIND_DEVICE";
    public static final String AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING = "AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING";
    public static final String AP_DEVICE_KEY_OPEN_DEV_INFO = "AP_DEVICE_KEY_OPEN_DEV_INFO";
    public static final String AP_DEVICE_KEY_MULTI_DATA = "AP_DEVICE_KEY_MULTI_DATA";
    public static final String AP_DEVICE_KEY_OPEN_STATUS = "AP_DEVICE_KEY_OPEN_STATUS";
    public static final String AP_DEVICE_KEY_NAME = "AP_DEVICE_KEY_NAME";

    private List<BleApDeviceInfoCacheListener> mOnUpdateApDeviceCacheListeners = new ArrayList<>();

    private BleApDeviceInfoCache() {
    }

    private static class ApDeviceInfoCacheHolder {
        public static final BleApDeviceInfoCache INSTANCE = new BleApDeviceInfoCache();
    }

    public static BleApDeviceInfoCache getInstance() {
        return ApDeviceInfoCacheHolder.INSTANCE;
    }

    public void updateApDevicesCache(List<BleApDeviceEntity> bleApDeviceEntities) {
        if (CollectionUtil.isEmpty(bleApDeviceEntities)) {
            return;
        }
        for (BleApDeviceEntity bleApDeviceEntity : CollectionUtil.safeFor(bleApDeviceEntities)) {
            if (bleApDeviceEntity != null) {
                updateApDeviceCache(bleApDeviceEntity.getDeviceId(), bleApDeviceEntity);
            }
        }
    }

    public void updateApDeviceCache(String deviceId, BleApDeviceEntity bleApDeviceEntity) {
        if (TextUtils.isEmpty(deviceId) || bleApDeviceEntity == null) {
            return;
        }
        BleApDeviceInfo apDeviceInfo = getCacheById(deviceId);
        if (apDeviceInfo == null) {
            apDeviceInfo = new BleApDeviceInfo();
        }
        apDeviceInfo.setDeviceId(deviceId);
        apDeviceInfo.setBleApDeviceEntity(bleApDeviceEntity);
        apDeviceInfo.setBindDevice(convertBindDeviceForBleApDeviceEntity(apDeviceInfo.getBindDevice(), bleApDeviceEntity));
        addCache(deviceId, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_BIND_DEVICE, apDeviceInfo);
    }

    public void updateApDeviceCache(String deviceId, BindDevice device) {
        if (TextUtils.isEmpty(deviceId) || device == null) {
            return;
        }
        BleApDeviceInfo apDeviceInfo = getCacheById(deviceId);
        if (apDeviceInfo == null) {
            apDeviceInfo = new BleApDeviceInfo();
            addCache(deviceId, apDeviceInfo);
        }
        apDeviceInfo.setDeviceId(deviceId);
        if (apDeviceInfo.getBindDevice() == null) {
            apDeviceInfo.setBindDevice(device);
            addCache(deviceId, apDeviceInfo);
            notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_BIND_DEVICE, apDeviceInfo);
        }
    }

    public void updateApDeviceCache(String deviceId, DevAllSettingsV2 devAllSettingsV2) {
        if (TextUtils.isEmpty(deviceId) || devAllSettingsV2 == null) {
            return;
        }
        BleApDeviceInfo apDeviceInfo = getCacheById(deviceId);
        if (apDeviceInfo == null) {
            apDeviceInfo = new BleApDeviceInfo();
            addCache(deviceId, apDeviceInfo);
        }
        apDeviceInfo.setDeviceId(deviceId);
        apDeviceInfo.setDevAllSettingsV2(devAllSettingsV2);
        if (devAllSettingsV2.commSettings != null && apDeviceInfo.getBindDevice() != null) {
            apDeviceInfo.getBindDevice().setOpen_status(devAllSettingsV2.commSettings.sleep == ConstantValue.CMD_STATE_ENABLE ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON);
        }
        addCache(deviceId, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_DEVICE_ALL_SETTING, apDeviceInfo);
    }

    public void updateApDeviceCache(String deviceId, DevInfo devInfo) {
        if (TextUtils.isEmpty(deviceId) || devInfo == null) {
            return;
        }
        BleApDeviceInfo apDeviceInfo = getCacheById(deviceId);
        if (apDeviceInfo == null) {
            apDeviceInfo = new BleApDeviceInfo();
            addCache(deviceId, apDeviceInfo);
        }
        apDeviceInfo.setDeviceId(deviceId);
        apDeviceInfo.setDevInfo(devInfo);
        if (apDeviceInfo.getBindDevice() != null) {
            apDeviceInfo.getBindDevice().setType(devInfo.model);
            apDeviceInfo.getBindDevice().setModel(devInfo.model);
            apDeviceInfo.getBindDevice().setName(devInfo.model);
        }
        addCache(deviceId, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_OPEN_DEV_INFO, apDeviceInfo);
    }

    public void updateApDeviceCache(String deviceId, Bundle data) {
        if (TextUtils.isEmpty(deviceId) || data == null) {
            return;
        }
        BleApDeviceInfo apDeviceInfo = getCacheById(deviceId);
        if (apDeviceInfo == null || apDeviceInfo.getBleApDeviceEntity() == null || apDeviceInfo.getBindDevice() == null) {
            return;
        }
        apDeviceInfo.setDeviceId(deviceId);
        if (data.containsKey(AP_DEVICE_KEY_OPEN_STATUS)) {
            if (apDeviceInfo.getBindDevice() != null) {
                apDeviceInfo.getBindDevice().setOpen_status(data.getInt(AP_DEVICE_KEY_OPEN_STATUS));
            }
            if (apDeviceInfo.getDevAllSettingsV2() != null && apDeviceInfo.getDevAllSettingsV2().commSettings != null) {
                apDeviceInfo.getDevAllSettingsV2().commSettings.sleep = data.getInt(AP_DEVICE_KEY_OPEN_STATUS) == ApiConstant.OPEN_STATUS_ON ? ConstantValue.CMD_STATE_DISABLE : ConstantValue.CMD_STATE_ENABLE;
            }
            apDeviceInfo.getBleApDeviceEntity().setOpenStatus(data.getInt(AP_DEVICE_KEY_OPEN_STATUS));
        }
        if (data.containsKey(AP_DEVICE_KEY_NAME)) {
            apDeviceInfo.getBindDevice().setName(data.getString(AP_DEVICE_KEY_NAME));
            apDeviceInfo.getBleApDeviceEntity().setName(data.getString(AP_DEVICE_KEY_NAME));
        }
        addCache(deviceId, apDeviceInfo);
        notifyApDeviceCacheUpdate(AP_DEVICE_KEY_MULTI_DATA, apDeviceInfo);
    }

    public void updateApDeviceName(String deviceId, String name) {
        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(name)) {
            return;
        }
        Bundle data = new Bundle();
        data.putString(AP_DEVICE_KEY_NAME, name);
        updateApDeviceCache(deviceId, data);
    }

    public void notifyApDeviceCacheUpdate(String key, BleApDeviceInfo apDeviceInfo) {
        if (CollectionUtil.isEmpty(mOnUpdateApDeviceCacheListeners)) {
            return;
        }
        for (BleApDeviceInfoCacheListener listener : CollectionUtil.safeFor(mOnUpdateApDeviceCacheListeners)) {
            if (listener != null) {
                listener.onUpdateCache(key, apDeviceInfo);
            }
        }
    }

    public void addApDeviceCacheListener(BleApDeviceInfoCacheListener listener) {
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

    public void removeApDeviceCacheListener(BleApDeviceInfoCacheListener listener) {
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

    public List<BleApDeviceEntity> getAllBleApDevice() {
        List<BleApDeviceEntity> result = new ArrayList<>();
        for (BleApDeviceInfo bleApDeviceInfo : CollectionUtil.safeFor(getAllCache())) {
             if (bleApDeviceInfo != null && NooieDeviceHelper.checkBleApDeviceEntityValid(bleApDeviceInfo.getBleApDeviceEntity())) {
                result.add(bleApDeviceInfo.getBleApDeviceEntity());
            }
        }
        return result;
    }

    public BleApDeviceEntity getBleApDeviceEntityByDeviceId(String deviceId) {
        if (TextUtils.isEmpty(deviceId) || getCacheById(deviceId) == null) {
            return null;
        }
        return getCacheById(deviceId).getBleApDeviceEntity();
    }

    private BindDevice convertBindDeviceForBleApDeviceEntity(BindDevice device, BleApDeviceEntity entity) {
        if (entity == null) {
            return device;
        }
        if (device == null) {
            device = new BindDevice();
        }
        device.setUuid(entity.getDeviceId());
        device.setType(entity.getModel());
        device.setModel(entity.getModel());
        device.setName(TextUtils.isEmpty(entity.getName()) ? entity.getModel() : entity.getName());
        device.setBind_type(ApiConstant.BIND_TYPE_OWNER);
        device.setOnline(ApiConstant.ONLINE_STATUS_ON);
        device.setOpen_status(ApiConstant.OPEN_STATUS_ON);
        //device.setOpen_status(entity.getOpenStatus());
        device.setPuuid(ConstantValue.NORMAL_DEVICE_PUUID);
        device.setVersion(entity.getVersion());
        return device;
    }

    public interface BleApDeviceInfoCacheListener {
        void onUpdateCache(String key, BleApDeviceInfo apDeviceInfo);
    }

}
