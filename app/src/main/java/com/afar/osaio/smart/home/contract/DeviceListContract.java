package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.sdk.device.bean.APPairStatus;

import java.util.List;
import java.util.Map;

public interface DeviceListContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onLoadDeviceSuccess(List<ListDeviceItem> devices);

        void onLoadDeviceSuccessEnd();

        void onLoadDeviceFailed(String result);

        void notifyUpdateDeviceOpenStatusResult(String result, String deviceId, int status);

        void notifyGetDeviceOpenStatusResult(String result, String deviceId, int status);

        void notifyUpdateDeviceCacheSort(String result);

        void notifyLoadLockDeviceSuccess(String result, List<BleDeviceEntity> lockDevices);

        void onDeleteDeviceResult(String result, String deviceId);

        void onCheckApDirectConnection(int state, int resultType, String ssid, APPairStatus status, String uuid);

        void onStartAPDirectConnect(int state, int connectionMode, String deviceSsid, boolean isAccessLive);

        void onStopAPDirectConnection(int state);

        void onLoadApDevice(int state, ApDeviceInfo device);

        void onUpdateApDeviceOpenStatus(int state, String deviceSsid, String deviceId, int status);

        void onCheckApDirectWhenNetworkChange(int state, NetworkChangeResult result);

        void onLoadBleApDevices(int result, List<BleApDeviceEntity> lockDevices);

        void checkBleApDeviceConnecting(int state, ApDeviceInfo result);

        void onCheckBeforeConnectBleDevice(int state, boolean result, String bleDeviceId, String model, String ssid);

        void onUpdateBleApDeviceSort(int state);

        void onLoadRouterDevices(int result, List<ListDeviceItem> devices);

        void onDeleteRouterDevice(int state, String routerDevice);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void stopRefreshTask();

        void stopLoadMoreTask();

        void refreshDevices(String account, String uid);

        void loadMoreDevice(String account);

        void updateDeviceOpenStatus(String deviceId, int status);

        void getDeviceOpenStatus(String deviceId);

        void updateDeviceSort(String account, Map<String, Integer> devices, Map<String, String> bindIdAndDeviceIdMap);

        void loadLockDevices(String account);

        void removeDevice(String account, String deviceId);

        void checkApDirectConnection();

        void startAPDirectConnect(String deviceSsid, boolean isAccessLive);

        void stopAPDirectConnection(String model);

        void loadApDevice();

        void updateApDeviceOpenStatus(String deviceSsid, String deviceId, int status);

        void checkApDirectWhenNetworkChange();

        void loadBleApDevices(String account);

        void checkBleApDeviceConnecting();

        void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid);

        void updateBleApDeviceSort(String user, Map<String, Integer> sortMap);

        void loadRouterDevices(String account, String routerWifiMac);

        void deleteRouterDevice(String routerDevice);

    }
}
