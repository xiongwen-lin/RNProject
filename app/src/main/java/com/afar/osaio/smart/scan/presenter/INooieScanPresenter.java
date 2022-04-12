package com.afar.osaio.smart.scan.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.nooie.data.entity.external.DeviceScanState;
import com.nooie.sdk.device.bean.APNetCfg;

/**
 * INooieScanPresenter
 *
 * @author Administrator
 * @date 2019/4/16
 */
public interface INooieScanPresenter extends IBasePresenter {

    void startScanDevice();

    void stopScanDevice();

    void loadRecentBindDevice(String user, boolean isScanSuccess);

    void stopQueryRecentBindDeviceTask();

    void startCountDown();

    void stopCountDown();

    void startScanApDevice(APNetCfg apNetCfg);

    void stopScanApDevice();

    void destroy();

    void checkApSwitchNetwork(String apSSID);

    void stopCheckApSwitchNetwork();

    DeviceScanState getDeviceScanState();
}
