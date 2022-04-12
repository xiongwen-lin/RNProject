package com.afar.osaio.smart.device.presenter;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 9:53 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public interface IIpcDeviceManagePresenter {

    void checkBleApDeviceConnecting(IpcDeviceManagePresenter.OnCheckBleApDeviceConnecting callback);

    void checkApDirectWhenNetworkChange(IpcDeviceManagePresenter.OnCheckApDirectWhenNetworkChange callback);

    void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid, IpcDeviceManagePresenter.OnCheckBeforeConnectBleDevice callback);

    void stopAPDirectConnection(String model, IpcDeviceManagePresenter.OnStopAPDirectConnection callback);

    void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean on);

}
