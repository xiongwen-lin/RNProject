package com.afar.osaio.smart.db.dao;

import android.os.Bundle;

import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.db.base.core.DbManager;
import com.nooie.sdk.db.entity.DaoSession;
import com.nooie.sdk.db.entity.DeviceGuideEntity;
import com.nooie.sdk.db.entity.DeviceGuideEntityDao;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.List;

/**
 * DeviceGuideService
 *
 * @author Administrator
 * @date 2019/7/19
 */
public class DeviceGuideService {

    public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
    public static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    public static final String KEY_USED = "KEY_USED";
    public static final String KEY_TALK_USED = "KEY_TALK_USED";

    private static class SingleTon {
        private static final DeviceGuideService INSTANCE = new DeviceGuideService();
    }

    public static DeviceGuideService getInstance() {
        return SingleTon.INSTANCE;
    }

    private DeviceGuideEntityDao mDeviceGuideDao;

    private DeviceGuideService() {
        DaoSession daoSession = DbManager.getInstance().getDaoSession();
        mDeviceGuideDao = daoSession.getDeviceGuideEntityDao();
    }

    public void addDeviceGuide(String uuid, String account, int used) {
        try {
            DeviceGuideEntity deviceGuideEntity = new DeviceGuideEntity();
            deviceGuideEntity.setUuid(uuid);
            deviceGuideEntity.setAccount(account);
            deviceGuideEntity.setUsed(used);
            mDeviceGuideDao.insertOrReplace(deviceGuideEntity);
        } catch (Exception e) {
        }
    }

    public void updateDeviceGuide(String deviceId, String account, Bundle data) {
        try {
            DeviceGuideEntity deviceGuideEntity = getDeviceGuide(deviceId, account);
            boolean isAdd = deviceGuideEntity == null;
            if (isAdd) {
                deviceGuideEntity = new DeviceGuideEntity();
                deviceGuideEntity.setUuid(deviceId);
                deviceGuideEntity.setAccount(account);
                deviceGuideEntity.setUsed(ConstantValue.DEVICE_GUIDE_NOT_USED);
                deviceGuideEntity.setTalkUsed(ConstantValue.DEVICE_GUIDE_NOT_USED);
            }
            if (data == null) {
                return;
            }
            if (data.containsKey(KEY_USED)) {
                deviceGuideEntity.setUsed(data.getInt(KEY_USED, ConstantValue.DEVICE_GUIDE_NOT_USED));
            }
            if (data.containsKey(KEY_TALK_USED)) {
                deviceGuideEntity.setTalkUsed(data.getInt(KEY_TALK_USED, ConstantValue.DEVICE_GUIDE_NOT_USED));
            }
            if (isAdd) {
                mDeviceGuideDao.insertOrReplace(deviceGuideEntity);
            } else {
                mDeviceGuideDao.update(deviceGuideEntity);
            }
            mDeviceGuideDao.insertOrReplace(deviceGuideEntity);
        } catch (Exception e) {
        }
    }

    public DeviceGuideEntity getDeviceGuide(String deviceId, String account) {
        try {
            return mDeviceGuideDao.queryBuilder()
                    .where(DeviceGuideEntityDao.Properties.Uuid.eq(deviceId), DeviceGuideEntityDao.Properties.Account.eq(account))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public void log() {
        try {
            List<DeviceGuideEntity> allDeviceGuide = mDeviceGuideDao.queryBuilder().build().forCurrentThread().list();
            for (DeviceGuideEntity deviceGuideEntity : CollectionUtil.safeFor(allDeviceGuide)) {
                NooieLog.d("-->> DanaleDevReportService danale dev report deviceId=" + deviceGuideEntity.getUuid() + " account=" + deviceGuideEntity.getAccount() + " use=" + deviceGuideEntity.getUsed());
            }
        } catch (Exception e) {
        }
    }
}
