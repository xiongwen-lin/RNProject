package com.afar.osaio.smart.smartlook.cache;

import android.text.TextUtils;

import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.List;

public class LockDeviceCache extends BaseCache<BleDeviceEntity> {

    private LockDeviceCache() {
    }

    private static class LockDeviceCacheHolder {
        public static final LockDeviceCache INSTANCE = new LockDeviceCache();
    }

    public static LockDeviceCache getInstance() {
        return LockDeviceCacheHolder.INSTANCE;
    }

    public void updateCache(BleDeviceEntity bleDeviceEntity) {
        if (bleDeviceEntity == null || TextUtils.isEmpty(bleDeviceEntity.getDeviceId())) {
            return;
        }
        if (isExisted(bleDeviceEntity.getDeviceId()) && getCacheById(bleDeviceEntity.getDeviceId()) != null) {
            BleDeviceEntity deviceEntity = getCacheById(bleDeviceEntity.getDeviceId());
            deviceEntity.setUser(bleDeviceEntity.getUser());
            deviceEntity.setUid(bleDeviceEntity.getUid());
            deviceEntity.setPhone(bleDeviceEntity.getPhone());
            deviceEntity.setPassword(bleDeviceEntity.getPassword());
            deviceEntity.setUserType(bleDeviceEntity.getUserType());
            deviceEntity.setDeviceId(bleDeviceEntity.getDeviceId());
            deviceEntity.setName(bleDeviceEntity.getName());
            deviceEntity.setDeviceType(bleDeviceEntity.getDeviceType());
            deviceEntity.setRssi(bleDeviceEntity.getRssi());
            deviceEntity.setDeviceType(bleDeviceEntity.getDeviceType());
            deviceEntity.setInitState(bleDeviceEntity.getInitState());
            deviceEntity.setUpdateTime(bleDeviceEntity.getUpdateTime());
            deviceEntity.setSec(bleDeviceEntity.getSec());
            addCache(deviceEntity.getDeviceId(), deviceEntity);
        } else {
            addCache(bleDeviceEntity.getDeviceId(), bleDeviceEntity);
        }
    }

    public void updateDevicesInLocalCache(List<BleDeviceEntity> bleDeviceEntities) {
        for (BleDeviceEntity deviceEntity : CollectionUtil.safeFor(bleDeviceEntities)) {
            if (deviceEntity != null) {
                updateCache(deviceEntity);
            }
        }
    }

}
