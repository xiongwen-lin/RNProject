package com.android.nordicbluetooth.bleLiveData

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.android.nordicbluetooth.bleLiveData.state.ConnectionState
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * 去掉中的默认参数
 * LiveData<ConnectionState>(
 *  ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
 * )
 * 并去掉初始化中的value初始值
 * value = ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
 */
internal class ConnectionStateLiveData: LiveData<ConnectionState>(

), ConnectionObserver {

    init {
        //value = ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        value = ConnectionState.Connecting
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        value = ConnectionState.Initializing
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        value = ConnectionState.Ready
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        value = ConnectionState.Disconnecting
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        value = ConnectionState.Disconnected(reason)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        value = ConnectionState.Disconnected(reason)
    }

}