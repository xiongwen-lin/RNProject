package com.afar.osaio.smart.mixipc.contract;

import android.os.Bundle;

import androidx.annotation.NonNull;

public interface ConnectApDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCheckConnectAp(String result, String ssid);

        void onStartAPDirectConnect(String result, Bundle param, String deviceId);

        void onStartBluetoothAPConnect(int result, Bundle param, String deviceId);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void checkConnectAp();


        void startBluetoothAPConnect(Bundle param);
    }
}
