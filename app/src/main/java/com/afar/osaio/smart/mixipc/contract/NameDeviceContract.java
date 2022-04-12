package com.afar.osaio.smart.mixipc.contract;

import androidx.annotation.NonNull;

public interface NameDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyUpdateDeviceNameState(String result);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void renameDevice(String deviceId, String name);

        void updateDeviceName(String user, String deviceId, String name);
    }
}
