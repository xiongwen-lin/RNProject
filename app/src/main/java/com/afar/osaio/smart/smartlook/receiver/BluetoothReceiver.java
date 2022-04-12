package com.afar.osaio.smart.smartlook.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nooie.common.utils.log.NooieLog;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            NooieLog.d("-->> BluetoothReceiver onReceive action=" + action);
        }
//        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//            // Discovery has found a device. Get the BluetoothDevice
//            // object and its info from the Intent.
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            String deviceName = device.getName();
//            String deviceHardwareAddress = device.getAddress(); // MAC address
//        }
    }
}
