package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;

import java.util.List;

public interface SmartRouterDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        //void setPresenter(@NonNull Presenter presenter);

        void onLoadRouterDevices(int code, List<SmartRouterDevice> device);

        void onDeleteRouterDevice(int code, String deviceId);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void refreshRouterDevice(String account);

        void loadRouterDevices(String account, String routerWifiMac);

        void stopLoadRouterDevices();

        void deleteRouterDevice(String routerDevice);

        List<SmartRouterDevice> queryDeviceFromDb(String routerWifiMac);

        List<SmartRouterDevice> getRouterDevices();

    }
}
