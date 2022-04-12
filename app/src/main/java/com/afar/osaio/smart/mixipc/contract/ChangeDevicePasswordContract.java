package com.afar.osaio.smart.mixipc.contract;

import androidx.annotation.NonNull;

public interface ChangeDevicePasswordContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onSetDeviceHotSpot(int state, int resultCode);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void checkDeviceHotSpotPw(String deviceId, String ssid, String pw, String oldPw);
    }
}
