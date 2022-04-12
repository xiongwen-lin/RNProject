package com.android.nordicbluetooth.callback

import android.bluetooth.BluetoothDevice

/***********************************************************
 * @Author : caro
 * @Date   : 2/21/21
 * @Func:
 *
 *
 * @Description:
 *
 *
 ***********************************************************/
interface EnableNotificationCallback {
    fun onRequestCompleted(device: BluetoothDevice)
    fun onRequestFailed(device: BluetoothDevice, status: Int)
}