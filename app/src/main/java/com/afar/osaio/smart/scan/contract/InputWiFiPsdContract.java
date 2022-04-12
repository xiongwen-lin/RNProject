package com.afar.osaio.smart.scan.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.mixipc.contract.BluetoothScanContract;

public interface InputWiFiPsdContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetSSID(String result, int useType, String ssid);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getSSID(int useType);

        void startSendCmdList(int cmdListSize, BluetoothScanContract.SendCmdListListener listener);

        void stopSendCmdList();
    }

    interface SendCmdListListener {
        void onSendCmd(int state, int cmdListSize, int cmdIndex);
    }
}
