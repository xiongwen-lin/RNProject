package com.afar.osaio.smart.setting.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.DeviceTestResult;
import com.afar.osaio.smart.device.bean.NooieDevice;

public interface DeviceTestToolContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onLoadDeviceInfo(String result, NooieDevice deviceInfo);

        void onQueryDeviceUpgradeStatus(String result, int type);

        void onStartAutoUpgradeTest(String result, int state);

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void startAutoUpgradeTest(String deviceId);

        void stopAutoUpgradeTest();

        void startQueryDeviceUpgradeStatus(String deviceId);

        void stopQueryDeviceUpgradeStatus();

        void loadDeviceInfo(String deviceId);

        DeviceTestResult getDeviceTestResult();
    }
}
