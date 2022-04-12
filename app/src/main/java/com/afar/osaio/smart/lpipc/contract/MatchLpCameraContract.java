package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;

public interface MatchLpCameraContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onQueryDeviceBindStatus(String result, DeviceBindStatusResult bindResult);

        void onGetBindDeviceSuccess(String result, boolean isSuccess);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void queryDeviceBindStatus();

        void stopQueryDeviceBindStatusTask();

        void startCountDown();

        void stopCountDown();

        void getRecentBindDevice();
    }
}
