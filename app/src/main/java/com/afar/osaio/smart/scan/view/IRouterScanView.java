package com.afar.osaio.smart.scan.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.device.bean.APPairStatus;

public interface IRouterScanView extends IBaseView {

    void onUpdateTimer(int seconds);

    void onTimerFinish();

    void onScanDeviceSuccess();

    void onScanDeviceFailed(String msg);

    void onScanDeviceByOther(DeviceBindStatusResult result);

    void onLoadRecentBindDeviceSuccess(boolean isScanSuccess);

    void onLoadRecentBindDeviceFailed(String msg);

    void onQueryAPPairStatus(String result, APPairStatus status);

}
