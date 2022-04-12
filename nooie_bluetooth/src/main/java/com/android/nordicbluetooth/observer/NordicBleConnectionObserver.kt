package com.android.nordicbluetooth.observer

import android.bluetooth.BluetoothDevice
import com.android.nordicbluetooth.SmartBleManager
import com.android.nordicbluetooth.callback.ConnectionCallback
import com.android.nordicbluetooth.tools.BleLogger
import no.nordicsemi.android.ble.observer.ConnectionObserver

/***********************************************************
 * @Author : caro
 * @Date   : 2/21/21
 * @Func:
 * 蓝牙连接状态监听回调
 * 主动断开蓝牙时，不会进行重新连接
 * @Description:
 *
 *
 ***********************************************************/
class NordicBleConnectionObserver(private val connectionCallbackList: List<ConnectionCallback>) :
    ConnectionObserver {
    private val TAG = NordicBleConnectionObserver::class.java.simpleName

    override fun onDeviceConnecting(device: BluetoothDevice) {
        BleLogger.i(TAG, "onDeviceConnecting设备正在连接")
        SmartBleManager.core.isConnecting = true
        connectionCallbackList.forEach {
            it.onDeviceConnecting(device)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        BleLogger.i(TAG, "onDeviceConnected设备已连接")
        SmartBleManager.core.isConnecting = false
        SmartBleManager.core.addConnectedBluetoothDevice(device)
        connectionCallbackList.forEach {
            it.onDeviceConnected(device)
        }
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        BleLogger.i(TAG, "onDeviceFailedToConnect设备连接失败")
        SmartBleManager.core.isConnecting = false
        when (reason) {
            ConnectionObserver.REASON_TIMEOUT -> {
                //连接超时
                BleLogger.i(TAG, "设备连接失败原因Reason:连接超时")
                connectionCallbackList.forEach {
                    it.onDeviceFailedToConnectTimeout(device)
                }
            }
            ConnectionObserver.REASON_NOT_SUPPORTED -> {
                //不支持
                BleLogger.i(TAG, "设备连接失败原因Reason:设备不支持")
                connectionCallbackList.forEach {
                    it.onDeviceFailedToConnectNotSupport(device)
                }
            }
            else -> {
                BleLogger.i(TAG, "设备连接失败原因Reason Code : $reason")
                connectionCallbackList.forEach {
                    it.onDeviceFailedToConnectReasonUnknown(device, reason)
                }
            }
        }
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        SmartBleManager.core.isConnecting = false
        BleLogger.i(TAG, "onDeviceReady设备Ready")
        //Notice：只有在设备已经Ready的状态下，才可进行数据交互
        connectionCallbackList.forEach {
            it.onDeviceReady(device)
        }

    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        SmartBleManager.core.isDisConnecting = true
        BleLogger.i(TAG, "onDeviceDisconnecting设备正在断开连接")
        connectionCallbackList.forEach {
            it.onDeviceDisconnecting(device)
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        SmartBleManager.core.isDisConnecting = false
        BleLogger.i(TAG, "onDeviceDisconnected设备断开连接")
        SmartBleManager.core.removeDisConnectedBluetoothDevice(device)
        when (reason) {
            /**
             * This reason will only be reported when {@link ConnectRequest#shouldAutoConnect()} was called
             * and connection to the device was lost. Android will try to connect automatically.
             */
            ConnectionObserver.REASON_LINK_LOSS -> {
                //连接丢失-在开启自动连接的状态下
                BleLogger.i(TAG, "设备断开连接原因 reason -> 连接丢失-在开启自动连接的状态下")
                connectionCallbackList.forEach {
                    it.onDeviceLinkLost(device)
                }
            }
            /**
             * The local device initiated disconnection
             */
            ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> {
                //本地设备启动断开连接--？开启自动连接时，不会回调
                BleLogger.i(TAG, "设备断开连接原因 reason -> 本地设备启动断开连接")
                connectionCallbackList.forEach {
                    it.onDeviceTerminateLocalHost(device)
                }
            }
            /**
             * The remote device initiated graceful disconnection
             */
            ConnectionObserver.REASON_TERMINATE_PEER_USER -> {
                //远程设备启动正常断开连接 ？开启自动连接时，不会回调
                BleLogger.i(TAG, "设备断开连接原因 reason -> 远程设备启动正常断开连接")
                connectionCallbackList.forEach {
                    it.onDeviceTerminateRemoteHost(device)
                }
            }
            ConnectionObserver.REASON_NOT_SUPPORTED -> {
                //不支持
                BleLogger.i(TAG, "设备连接失败原因Reason:设备不支持")
                connectionCallbackList.forEach {
                    it.onDeviceFailedToConnectNotSupport(device)
                }
            }
            else -> {
                //设备断开连接原因未知UnKnow
                BleLogger.i(TAG, "设备断开连接原因 reason UnKnow :$reason -> UnKnow")
                connectionCallbackList.forEach {

                    it.onDeviceDisConnectUnknownReason(device, reason)
                }
            }
        }
    }
}