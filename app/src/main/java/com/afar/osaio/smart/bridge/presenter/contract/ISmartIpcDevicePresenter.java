package com.afar.osaio.smart.bridge.presenter.contract;

import com.afar.osaio.smart.bridge.bean.ConnectBleApDeviceResult;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.apemans.platformbridge.bean.YRBindDeviceResult;
import com.apemans.platformbridge.bean.YRPlatformDevice;
import com.apemans.platformbridge.listener.IBridgeResultListener;

import java.util.List;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 9:53 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public interface ISmartIpcDevicePresenter {

    void queryDeviceList(String account, String uid, YRBindDeviceResult bindDeviceResult, IBridgeResultListener<List<YRPlatformDevice>> listener);

    void stopQueryDeviceListTask();

    YRPlatformDevice queryNetSpotDevice();

    void refreshIpcDevices(String account, String uid, IBridgeResultListener listener);

    void stopRefreshIpcDevicesTask();

    List<SmartCameraDevice> getIpcDevices();

    void checkBleApDeviceConnecting();

    void checkApDirectWhenNetworkChange();

    void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid, IBridgeResultListener<ConnectBleApDeviceResult> listener);

    void stopAPDirectConnection(String model);

    void updateDeviceOpenStatus(String deviceId, boolean on, IBridgeResultListener<Boolean> listener);

    void getDeviceOpenStatus(String deviceId);

    void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean on, IBridgeResultListener<Boolean> listener);

    void removeIpcDevice(String account, String deviceId, IBridgeResultListener<String> listener);

    void sendCmd(String cmd, IBridgeResultListener<String> listener);

    void queryAllIpcDevice(IBridgeResultListener<String> listener);

}
