package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.smartlook.bean.BleDevice;

public interface AddAdminContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyAddAdminResult(String result, boolean isAdmin);

        void notifyBleDeviceState(int connectState);
    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void addAdminUser(String user, String uid, String phone, String password, boolean isAdmin, BleDevice bleDevice);
    }
}
