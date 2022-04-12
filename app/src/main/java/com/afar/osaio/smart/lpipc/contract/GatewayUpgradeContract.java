package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

public interface GatewayUpgradeContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onQueryDeviceUpdateStatus(int type, int process);

        void onStartUpdateDeviceResult(String result);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void queryDeviceUpgradeTime(String deviceId, String account, boolean isUpdateTime);

        void stopUpdateProcessTask();

        void queryDeviceUpdateStatus(String deviceId);

        void stopQueryDeviceUpdateState();

        void startUpdateDevice(String account, String deviceId, String model, String version, String pkt, String md5);
    }
}
