package com.afar.osaio.smart.smartlook.db.dao;

import android.text.TextUtils;

import com.nooie.sdk.db.base.core.DbManager;
import com.nooie.sdk.db.entity.DaoSession;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.db.entity.LockRecordEntity;
import com.nooie.sdk.db.entity.LockRecordEntityDao;

import java.util.List;

public class LockRecordService {

    private LockRecordEntityDao mDeviceDao;

    private LockRecordService() {
        DaoSession daoSession = DbManager.getInstance().getDaoSession();
        mDeviceDao = daoSession.getLockRecordEntityDao();
    }

    private final static class SingleTon {
        private static LockRecordService INSTANCE = new LockRecordService();
    }

    public static LockRecordService getInstance() {
        return SingleTon.INSTANCE;
    }

    public void addRecord(String recordKey, String user, String deviceId, String deviceIndex, String name, int nameType, long time) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId)) {
            return;
        }

        try {
            LockRecordEntity lockRecordEntity = new LockRecordEntity();
            lockRecordEntity.setRecordKey(recordKey);
            lockRecordEntity.setUser(user);
            lockRecordEntity.setDeviceId(deviceId);
            lockRecordEntity.setDeviceIndex(deviceIndex);
            lockRecordEntity.setName(name);
            lockRecordEntity.setNameType(nameType);
            lockRecordEntity.setTime(time);
            mDeviceDao.insertOrReplace(lockRecordEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LockRecordEntity getRecord(String user, String deviceId, String recordKey) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(LockRecordEntityDao.Properties.User.eq(user),LockRecordEntityDao.Properties.DeviceId.eq(deviceId), LockRecordEntityDao.Properties.RecordKey.eq(recordKey))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public List<LockRecordEntity> getRecords(String user, String deviceId) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(LockRecordEntityDao.Properties.User.eq(user),LockRecordEntityDao.Properties.DeviceId.eq(deviceId))
                    .build()
                    .forCurrentThread()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteRecord(String user, String deviceId, String recordKey) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId)) {
            return;
        }

        try {
            LockRecordEntity deviceEntity = getRecord(user, deviceId, recordKey);
            if (deviceEntity != null) {
                mDeviceDao.delete(deviceEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String user, String deviceId) {
        try {
            List<LockRecordEntity> lockRecordEntities = getRecords(user, deviceId);
            for (LockRecordEntity lockRecordEntity : CollectionUtil.safeFor(lockRecordEntities)) {
                NooieLog.d("-->> LockRecordService user=" + lockRecordEntity.getUser() + " deviceId=" + lockRecordEntity.getDeviceId() + "recordKey=" + lockRecordEntity.getRecordKey() + " nameType=" + lockRecordEntity.getNameType() + " name=" + lockRecordEntity.getName() + " time=" + lockRecordEntity.getTime());
            }
        } catch (Exception e) {
        }
    }
}
