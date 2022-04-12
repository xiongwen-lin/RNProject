package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/7/4
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieDeviceSettingPresenter extends IBasePresenter {

    void detachView();

    void getDeviceSetting(String deviceId, String model);

    void getAllSetting(String account, String deviceId);

    void getDevInfo(String deviceId);

    void getDevice(String deviceId, String account);

    /**
     * LED
     *
     * @param deviceId
     */
    void getLedStatus(String deviceId);

    void setLedStatus(String deviceId, boolean open);

    /**
     * camera factory reset
     * @param deviceId
     */
    void setFactoryReset(String account, String deviceId);

    void setFactoryResetForAp(String deviceId, String model);

    /**
     * Remove camera
     *
     * @param deviceId
     */
    void removeCamera(String deviceId, String uid, String account, boolean isOnline, boolean isMyDevice, boolean isSubDevice, String pDeviceId);

    void removeDevice(String deviceId, String uid, String account, boolean isOnline, boolean isMyDevice, boolean isSubDevice, String pDeviceId);

    void setSyncTime(String uuid, final int mode, float timeZone, int timeOffset);

    void getDeviceStorageState(String deviceId, int bindType);

    void getCamInfo(String account, String deviceId);

    void setDeviceAiMode(String deviceId, boolean open);

    void updateDeviceNotice(String deviceId, int isNotice);

    void getPirState(String deviceId);

    void setSiren(String deviceId, boolean on);

    void getFileSettingMode(String deviceId);

    void removeApDevice(String user, String uid, String deviceId, String model);
}
