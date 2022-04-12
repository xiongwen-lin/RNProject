package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.smartlook.bean.BleDevice;

public interface AddBluetoothDeviceContract {

    interface View extends BaseBleContract.View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyCheckBleDevice(String result, boolean isExist, BleDevice bleDevice);

    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void checkAddBleDevice(BleDevice bleDevice);
    }
}
