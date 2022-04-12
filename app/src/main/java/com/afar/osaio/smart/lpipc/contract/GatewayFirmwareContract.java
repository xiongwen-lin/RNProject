package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

public interface GatewayFirmwareContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCheckDeviceUpdateStatusResult(String result, int type);

        void onStartUpdateDeviceResult(String result);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void checkDeviceUpdateStatus(String deviceId, String account);

        void startUpdateDevice(String account, String deviceId, String model, String version, String pkt, String md5);
    }
}
