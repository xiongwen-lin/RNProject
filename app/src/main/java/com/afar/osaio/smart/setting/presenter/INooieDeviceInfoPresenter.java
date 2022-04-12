package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.afar.osaio.smart.device.bean.DeviceCfg;

/**
 * Created by victor on 2018/7/4
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieDeviceInfoPresenter extends IBasePresenter {

    void destroy();

    void rename(String deviceId, String alias);

    void loadInfos(DeviceCfg deviceCfg);

    void loadFirmwareVersion(String deviceId);

    void startUpdateDevice(String account, final String deviceId, String model, String version, String pkt, String md5, boolean isSubDevice);

    void startUpdateProcessTask();

    void stopUpdateProcessTask();

    int getUpdateProcess();

    void checkDeviceUpgradeSchedule(String deviceId, String account);

    void queryDeviceUpgradeTime(String deviceId, String account, boolean isUpdateTime);

    /**
     * update query state
     */
    void queryDeviceUpdateStatus(String deviceId, String account, boolean isUpdateTime);

    void stopQueryDeviceUpdateState();

    void stopQueryUpgradeForTimeout();

}
