package com.afar.osaio.smart.smartlook.db.dao;

import android.text.TextUtils;

import com.nooie.sdk.db.base.core.DbManager;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.db.entity.DaoSession;
import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.nooie.sdk.db.entity.LockAuthorizationEntityDao;

import java.util.List;

public class LockAuthorizationService {

    private LockAuthorizationEntityDao mDeviceDao;

    private LockAuthorizationService() {
        DaoSession daoSession = DbManager.getInstance().getDaoSession();
        mDeviceDao = daoSession.getLockAuthorizationEntityDao();
    }

    private final static class SingleTon {
        private static LockAuthorizationService INSTANCE = new LockAuthorizationService();
    }

    public static LockAuthorizationService getInstance() {
        return SingleTon.INSTANCE;
    }

    public void addAuthorization(String user, String deviceId, String codeIndex, String name, String code) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(code)) {
            return;
        }

        try {
            LockAuthorizationEntity lockAuthorizationEntity = new LockAuthorizationEntity();
            lockAuthorizationEntity.setUser(user);
            lockAuthorizationEntity.setDeviceId(deviceId);
            lockAuthorizationEntity.setCodeIndex(codeIndex);
            lockAuthorizationEntity.setName(name);
            lockAuthorizationEntity.setCode(code);
            mDeviceDao.insertOrReplace(lockAuthorizationEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LockAuthorizationEntity getAuthorization(String user, String deviceId, String code) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(LockAuthorizationEntityDao.Properties.User.eq(user),LockAuthorizationEntityDao.Properties.DeviceId.eq(deviceId), LockAuthorizationEntityDao.Properties.Code.eq(code))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public List<LockAuthorizationEntity> getAuthorizations(String user, String deviceId) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(LockAuthorizationEntityDao.Properties.User.eq(user),LockAuthorizationEntityDao.Properties.DeviceId.eq(deviceId))
                    .build()
                    .forCurrentThread()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAuthorization(String user, String deviceId, String code) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(code)) {
            return;
        }

        try {
            LockAuthorizationEntity authorizationEntity = getAuthorization(user, deviceId, code);
            if (authorizationEntity != null) {
                mDeviceDao.delete(authorizationEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateAuthorization(LockAuthorizationEntity lockAuthorizationEntity) {
        if (lockAuthorizationEntity == null) {
            return;
        }

        try {
            LockAuthorizationEntity authorizationEntity = getAuthorization(lockAuthorizationEntity.getUser(), lockAuthorizationEntity.getDeviceId(), lockAuthorizationEntity.getCode());
            if (authorizationEntity != null) {
                authorizationEntity.setUser(lockAuthorizationEntity.getUser());
                authorizationEntity.setDeviceId(lockAuthorizationEntity.getDeviceId());
                authorizationEntity.setCodeIndex(lockAuthorizationEntity.getCodeIndex());
                authorizationEntity.setName(lockAuthorizationEntity.getName());
                authorizationEntity.setCode(lockAuthorizationEntity.getCode());
                mDeviceDao.update(authorizationEntity);
            } else {
                addAuthorization(lockAuthorizationEntity.getUser(), lockAuthorizationEntity.getDeviceId(), lockAuthorizationEntity.getCodeIndex(), lockAuthorizationEntity.getName(), lockAuthorizationEntity.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String user, String deviceId) {
        try {
            List<LockAuthorizationEntity> lockAuthorizationEntities = getAuthorizations(user, deviceId);
            for (LockAuthorizationEntity lockAuthorizationEntity : CollectionUtil.safeFor(lockAuthorizationEntities)) {
                NooieLog.d("-->> LockRecordService user=" + lockAuthorizationEntity.getUser() + " deviceId=" + lockAuthorizationEntity.getDeviceId() + "index=" + lockAuthorizationEntity.getCodeIndex() + " name=" + lockAuthorizationEntity.getName() + " name=" + lockAuthorizationEntity.getName() + " code=" + lockAuthorizationEntity.getCode());
            }
        } catch (Exception e) {
        }
    }
}
