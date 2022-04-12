package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;

public interface LookDeviceContract {

    interface View extends BaseBleContract.View{

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyBleDeviceState(int connectState);

        void notifyGetTemporaryPassword(String result, String id, String password);

        void notifyGetBattery(String result, int battery);
    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        SmartLookManager getSmartLookManager();

        void updateLockBattery(String user, String deviceId, int battery);
    }
}
