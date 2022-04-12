package com.afar.osaio.smart.scan.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.nooie.sdk.device.bean.APNetCfg;

public interface IRouterScanPresenter extends IBasePresenter {

    void startScanDevice();

    void stopScanDevice();

    void loadRecentBindDevice(String user, boolean isScanSuccess);

    void stopQueryRecentBindDeviceTask();

    void startCountDown();

    void stopCountDown();

    void startScanApDevice(APNetCfg apNetCfg);

    void stopScanApDevice();

    void destroy();
}
