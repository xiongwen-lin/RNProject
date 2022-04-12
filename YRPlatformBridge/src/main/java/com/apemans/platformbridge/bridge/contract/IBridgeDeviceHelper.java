package com.apemans.platformbridge.bridge.contract;

import android.app.Activity;

import androidx.lifecycle.LiveData;

import com.apemans.platformbridge.bean.YRBindDeviceResult;
import com.apemans.platformbridge.bean.YRPlatformDevice;
import com.apemans.platformbridge.listener.IBridgeResultListener;

import java.util.List;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/16 11:01 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public interface IBridgeDeviceHelper {

    void queryDeviceList(String uid, String account, YRBindDeviceResult bindDeviceResult, IBridgeResultListener<List<YRPlatformDevice>> listener);

    YRPlatformDevice queryNetSpotDevice();

    void refreshNetSpotConnection();

    void stopAPDirectConnection(String model, IBridgeResultListener listener);

    LiveData<String> getNetSpotConnectionState();

    boolean checkIsNetSpot();

    String encryptUid(String uid);

    void openAddDevicePage();

    void openLiveAsSingle(String deviceId);

    void openPlaybackAsSingle(String deviceId, long seekTime, boolean isCloud);

    void openSensitivityPage(String deviceId);

    void deviceItemClick(String deviceId, String deviceInfoType, String model, String deviceSsid, String bleDeviceId, Activity activity, IBridgeResultListener<String> listener);

    void deviceItemSwitch(String deviceId, String deviceInfoType, String deviceSsid, boolean state, IBridgeResultListener<Boolean> listener);

    void sendCmd(String cmd, IBridgeResultListener<String> listener);

    void queryAllIpcDevice(IBridgeResultListener<String> listener);

}
