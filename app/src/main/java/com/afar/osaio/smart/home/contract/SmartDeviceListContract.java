package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.widget.bean.TabItemBean;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.sdk.device.bean.APPairStatus;

import java.util.List;
import java.util.Map;

public interface SmartDeviceListContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onRefreshDeviceCategory(String account, String uid, long homeId, List<TabItemBean> tabItemBeans, boolean isRefreshDevice);

        void onReceiveDeviceChange();

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void refreshDevice(String account, String uid, long homeId);

        List<SmartCameraDevice> getCameraDevices();

        List<SmartTyDevice> getTyDevices();

        List<SmartRouterDevice> getRouterDevices();

        void tryRefreshDeviceCategory(String account, String uid);

        void refreshDeviceCategory(String account, String uid, long homeId);

        void registerDeviceChangeReceiver();

        void unRegisterDeviceChangeReceiver();

        void checkBleApDeviceConnecting();

        void checkApDirectWhenNetworkChange();

        void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid);

        void stopAPDirectConnection(String model);

        void updateDeviceOpenStatus(String deviceId, boolean on);

        void getDeviceOpenStatus(String deviceId);

        void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean on);

        void removeIpcDevice(String account, String deviceId);

        void controlDevice(String deviceId, boolean open);

        void controlLamp(String devId, String dpId, boolean open);

        void controlStrip(String devId, Map<String, Object> dpsMap);

        void updateDeviceCategory(String account, String uid, String tabDeviceCategory, int deviceSize);

        void loadDeviceBean(String deviceId);
    }
}
