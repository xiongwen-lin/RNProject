package com.afar.osaio.smart.scan.contract;

import androidx.annotation.NonNull;

public interface ConnectToWiFiContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCheckConnectAp(String result, String ssid);

        void onStartAPDirectConnect(String result, String deviceSsid);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void checkConnectAp();

        void startAPDirectConnect(String deviceSsid);
    }
}
