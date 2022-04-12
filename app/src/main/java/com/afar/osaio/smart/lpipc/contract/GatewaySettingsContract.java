package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

import java.util.List;

public interface GatewaySettingsContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetGatewayDevicesResult(String result, List<GatewayDevice> gatewayDevices);

        void onDeleteSubDeviceResult(String result, String deviceId, String pDeviceId);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getGatewayDevices(String user, String uid);

        void removeSubDevice(String account, String deviceId, String pDeviceId);
    }
}
