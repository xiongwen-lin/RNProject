package com.android.nordicbluetooth.callback

import android.bluetooth.BluetoothDevice

/***********************************************************
 * @Author : caro
 * @Date   : 2/21/21
 * @Func:
 * 设备连接状态Callback
 *
 * @Description:
 *
 *
 ***********************************************************/
interface ConnectionCallback {
    /**
     * 正在连接
     */
    fun onDeviceConnecting(device: BluetoothDevice)

    /**
     * 连接失败-超时
     */
    fun onDeviceFailedToConnectTimeout(device: BluetoothDevice)

    /**
     * 连接失败-设备不支持
     */
    fun onDeviceFailedToConnectNotSupport(device: BluetoothDevice)

    /**
     * 连接失败-未知原因
     * @param reason 参考-- ConnectionObserver.REASON_*
     */
    fun onDeviceFailedToConnectReasonUnknown(device: BluetoothDevice, reason: Int)

    /**
     * 设备已连接
     */
    fun onDeviceConnected(device: BluetoothDevice)

    /**
     * 设备已经Ready
     */
    fun onDeviceReady(device: BluetoothDevice)

    /**
     * 设备正在断开连接
     */
    fun onDeviceDisconnecting(device: BluetoothDevice)

    /**
     * 设备失去连接 -- 开启自动连接的情况下
     */
    fun onDeviceLinkLost(device: BluetoothDevice)

    /**
     * 设备断开连接 -- 关闭手机蓝牙导致的断开连接
     */
    fun onDeviceTerminateLocalHost(device: BluetoothDevice)

    /**
     * 设备断开连接 -- 对端硬件蓝牙设备断开导致的断开连接
     */
    fun onDeviceTerminateRemoteHost(device: BluetoothDevice)

    /**
     * 设备断开连接 -- Reason 暂未处理
     * @param reason 参考-- ConnectionObserver.REASON_*
     */
    fun onDeviceDisConnectUnknownReason(device: BluetoothDevice,reason: Int)
}