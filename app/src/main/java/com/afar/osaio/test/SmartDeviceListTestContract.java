package com.afar.osaio.test;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.widget.bean.TabItemBean;

import java.util.List;
import java.util.Map;

public interface SmartDeviceListTestContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void getHomeId(long homeId);

        void onRefreshDeviceCategory(String account, String uid, long homeId, List<TabItemBean> tabItemBeans);
    }

    interface Presenter {

        /*void createDefaultHome(int times);*/

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void refreshDevice(String account, String uid, long homeId);

        List<SmartCameraDevice> getCameraDevices();

        List<SmartTyDevice> getTyDevices();

        void refreshDeviceCategory(String account, String uid, long homeId);

        void controlDevice(String deviceId, boolean open);

        void controlLamp(String devId, String dpId, boolean open);

        void controlStrip(String devId, Map<String, Object> dpsMap);
    }
}
