package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;

import java.util.List;

public interface SmartIpcDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        //void setIpcDevicePresenter(@NonNull Presenter presenter);

        void onLoadDeviceSuccess(List<ListDeviceItem> devices);

        void onLoadDeviceEnd(int code, List<ListDeviceItem> devices);

        void onCheckBleApDeviceConnecting(int state, ApDeviceInfo result);

        void onCheckApDirectWhenNetworkChange(int state, NetworkChangeResult result);

        void onCheckBeforeConnectBleDevice(int state, boolean result, String bleDeviceId, String model, String ssid);

        void onStopAPDirectConnection(int state);

        void onUpdateDeviceOpenStatusResult(String result, String deviceId, boolean on);

        void onGetDeviceOpenStatusResult(String result, String deviceId, boolean on);

        void onUpdateApDeviceOpenStatus(int state, String deviceSsid, String deviceId, boolean on);

        void onDeleteIpcDeviceResult(String result, String deviceId);

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void refreshIpcDevices(String account, String uid);

        void stopRefreshIpcDevicesTask();

        List<SmartCameraDevice> getIpcDevices();

        void checkBleApDeviceConnecting();

        void checkApDirectWhenNetworkChange();

        void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid);

        void stopAPDirectConnection(String model);

        void updateDeviceOpenStatus(String deviceId, boolean on);

        void getDeviceOpenStatus(String deviceId);

        void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean on);

        void removeIpcDevice(String account, String deviceId);

    }
}
