package com.afar.osaio.smart.smartlook.contract;

import android.content.Context;

import com.afar.osaio.smart.smartlook.bean.BleDevice;

import java.util.List;

public interface BaseBleContract {

    interface View {

        void onScanDeviceFinish(String result);

    }

    interface Presenter {

        void startScanDeviceByTask(String user, List<String> filterDeviceIds);

        void stopScanDeviceByTask();

        void connect(Context context, BleDevice bleDevice);

        void disconnect();

        void scanDeviceFinish(String result);
    }
}
