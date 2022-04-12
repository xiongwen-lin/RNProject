package com.afar.osaio.smart.bluetooth.listener;

import android.bluetooth.BluetoothDevice;

public interface OnBleConnectListener {

    void onResult(int state, BluetoothDevice bluetoothDevice);
}
