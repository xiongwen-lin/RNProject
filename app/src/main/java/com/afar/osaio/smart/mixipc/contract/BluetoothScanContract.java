package com.afar.osaio.smart.mixipc.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.db.entity.BleApDeviceEntity;

public interface BluetoothScanContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCheckDeleteBleApDevice(int state, BleApDeviceEntity result);

        void onDeleteBleApDevice(int state);

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void startSendCmdList(int cmdListSize, BluetoothScanContract.SendCmdListListener listener);

        void stopSendCmdList();

        void checkDeleteBleApDevice(String bleApDeviceId);

        void deleteBleApDevice(String deviceId);
    }

    interface SendCmdListListener {
        void onSendCmd(int state, int cmdListSize, int cmdIndex);
    }
}
