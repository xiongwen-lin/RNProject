package com.afar.osaio.smart.smartlook.db.dao;

import android.text.TextUtils;

import com.nooie.sdk.db.base.core.DbManager;
import com.nooie.sdk.db.entity.BleDeviceEntityDao;
import com.nooie.sdk.db.entity.DaoSession;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.List;

public class BleDeviceService {

    private BleDeviceEntityDao mDeviceDao;

    private BleDeviceService() {
        DaoSession daoSession = DbManager.getInstance().getDaoSession();
        mDeviceDao = daoSession.getBleDeviceEntityDao();
    }

    private final static class SingleTon {
        private static BleDeviceService INSTANCE = new BleDeviceService();
    }

    public static BleDeviceService getInstance() {
        return SingleTon.INSTANCE;
    }


    public void addDevice(String user, String uid, String phone, String password, int userType, BleDevice bleDevice) {
        if (TextUtils.isEmpty(uid) || bleDevice == null || bleDevice.getDevice() == null) {
            return;
        }

        try {
            BleDeviceEntity bleDeviceEntity = new BleDeviceEntity();
            bleDeviceEntity.setUser(user);
            bleDeviceEntity.setUid(uid);
            bleDeviceEntity.setPhone(phone);
            bleDeviceEntity.setPassword(password);
            bleDeviceEntity.setUserType(userType);
            bleDeviceEntity.setDeviceId(bleDevice.getDevice().getAddress());
            bleDeviceEntity.setName(bleDevice.getDevice().getName());
            bleDeviceEntity.setInitState(bleDevice.getInitState());
            bleDeviceEntity.setRssi(bleDevice.getRssi());
            bleDeviceEntity.setSec(bleDevice.getSec());
            bleDeviceEntity.setUpdateTime(bleDevice.getUpdateTime());

            mDeviceDao.insertOrReplace(bleDeviceEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BleDeviceEntity getDevice(String user, String deviceId) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(BleDeviceEntityDao.Properties.User.eq(user),BleDeviceEntityDao.Properties.DeviceId.eq(deviceId))
                    .build()
                    .unique();
        } catch (Exception e) {
        }
        return null;
    }

    public List<BleDeviceEntity> getDevices(String user) {
        try {
            return mDeviceDao.queryBuilder()
                    .where(BleDeviceEntityDao.Properties.User.eq(user))
                    .build()
                    .forCurrentThread()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteDevice(String user, String deviceId) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId)) {
            return;
        }

        try {
            BleDeviceEntity deviceEntity = getDevice(user, deviceId);
            if (deviceEntity != null) {
                mDeviceDao.delete(deviceEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDeviceBattery(String user, String deviceId, int battery) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(deviceId)) {
            return;
        }

        try {
            BleDeviceEntity deviceEntity = getDevice(user, deviceId);
            if (deviceEntity != null) {
                deviceEntity.setBattery(battery);
                mDeviceDao.update(deviceEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String user) {
        try {
            List<BleDeviceEntity> allDevice = getDevices(user);
            for (BleDeviceEntity deviceEntity : CollectionUtil.safeFor(allDevice)) {
                StringBuilder secSb = new StringBuilder();
                if (deviceEntity.getSec() != null && deviceEntity.getSec().length >= 2) {
                    secSb.append(deviceEntity.getSec()[0] & 0xFF);
                    secSb.append("|");
                    secSb.append(deviceEntity.getSec()[1] & 0xFF);
                }
                NooieLog.d("-->> DeviceInfoService user=" + deviceEntity.getUser() + " userType=" + deviceEntity.getUserType() + " deviceId=" + deviceEntity.getDeviceId() + " deviceType=" + deviceEntity.getDeviceType() + " name=" + deviceEntity.getName() + " initState=" + deviceEntity.getInitState()
                        + " rssi=" + deviceEntity.getRssi() + " sec=" + secSb + " updateTime=" + deviceEntity.getUpdateTime() + " phone=" + deviceEntity.getPhone() + " code=" + deviceEntity.getPassword());
            }
        } catch (Exception e) {
        }
    }
}
