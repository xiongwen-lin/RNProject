package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

public interface DeviceScanCodeContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyBindGatewayResult(String result, String deviceId, int code);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void bindGatewayDevice(String uuid);

        void bindDevice(String uuid);
    }
}
