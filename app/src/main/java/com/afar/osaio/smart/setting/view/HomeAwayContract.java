package com.afar.osaio.smart.setting.view;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.entity.DeviceStatusResult;

public interface HomeAwayContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyUpdateDeviceOpenStatusResult(String result, String deviceId, int status);

        void onUpdateDeviceOpenStatus(String result, String deviceId, String deviceSsid, boolean sleep);

        void notifyGetDeviceOpenStatusSuccess(String deviceId, DeviceStatusResult result);

        void onGetApDeviceOpenStatus(String result, int openStatus);

        void notifyGetDeviceOpenStatusFailed(String deviceId, String msg);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void updateDeviceOpenStatus(String deviceId, int status);

        void updateApDeviceOpenStatus(String deviceId, String deviceSsid, boolean sleep);

        void getDeviceOpenStatus(String deviceId);

        void getAPDeviceOpenStatus(String deviceId);
    }

}
