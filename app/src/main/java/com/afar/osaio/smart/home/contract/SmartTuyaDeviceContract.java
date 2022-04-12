package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.home.bean.SmartAppliancesDevice;
import com.afar.osaio.smart.home.bean.SmartElectricianDevice;
import com.afar.osaio.smart.home.bean.SmartLightDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;
import java.util.Map;

public interface SmartTuyaDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        //void setTuyaDevicePresenter(@NonNull Presenter presenter);

        void onLoadTuyaDevices(int code, HomeBean homeBean);

        void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean);

        void notifyControlDeviceState();
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void loadHomeDetail(long homeId);

        List<SmartTyDevice> getSmartTyDevices();

        List<SmartElectricianDevice> getElectricianDevices();

        List<SmartLightDevice> getLightDevices();

        List<SmartAppliancesDevice> getAppliancesDevices();

        void controlDevice(String deviceId, boolean open);

        void controlLamp(String devId, String dpId, boolean open);

        void controlStrip(String devId, Map<String, Object> dpsMap);

        void loadDeviceBean(String deviceId);

    }
}
