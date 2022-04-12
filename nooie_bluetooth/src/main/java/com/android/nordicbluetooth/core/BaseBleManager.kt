/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.android.nordicbluetooth.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.android.nordicbluetooth.callback.ConnectionCallback
import com.android.nordicbluetooth.callback.EnableNotificationCallback
import com.android.nordicbluetooth.callback.ScanBleDeviceCallback
import com.android.nordicbluetooth.observer.NordicBleConnectionObserver
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.WriteProgressCallback
import no.nordicsemi.android.ble.data.DefaultMtuSplitter
import no.nordicsemi.android.support.v18.scanner.ScanResult

/***********************************************************
 * @Author : caro
 * @Date   : 2/21/21
 * @Func:
 * 基础配置BleManager
 *
 * @Description:
 *
 *
 ***********************************************************/
abstract class BaseBleManager(context: Context) : BleManager(context) {
    private val TAG = "BaseBleManager"
    val handler = Handler(Looper.getMainLooper())
    val scannerTimeoutHandler = Handler(Looper.getMainLooper())

    /**
     * 设备特征码
     */
    protected var UUID_SERVICE: String? = null
    protected var UUID_WRITE_CHAR: String? = null
    protected var UUID_READ_CHAR: String? = null
    protected var UUID_NOTIFY_CHAR: String? = null
    //Some device have UUID_OTHER
    /*protected var UUID_OTHER = "00004444-0000-1000-8000-00805f9b34fb"*/

    /**
     * 接收消息的特征，默认一般为mNotifyCharacteristic
     */
    protected var receiverCharacteristicUUID: String? = null

    /**
     * 服务
     */
    protected var mUUIDService: BluetoothGattService? = null

    /**
     * 特征
     */
    protected var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    protected var mWriteCharacteristic: BluetoothGattCharacteristic? = null
    protected var mReadCharacteristic: BluetoothGattCharacteristic? = null

    /**
     * 是否正在扫描
     */
    var isScanning = false

    /**
     * 是否正在连接
     */
    var isConnecting = false

    /**
     * 是否正在断开连接
     */
    var isDisConnecting = false

    /**
     * A flag indicating whether Long Write can be used. It's set to false if the UART RX
     * characteristic has only PROPERTY_WRITE_NO_RESPONSE property and no PROPERTY_WRITE.
     * If you set it to false here, it will never use Long Write.
     *
     * TODO change this flag if you don't want to use Long Write even with Write Request.
     */
    protected var useLongWrite = true

    /**
     * mtu默认值:260
     */
    protected var mtuValue = 260

    /**
     * 扫描结果筛选条件
     */
    protected var filterScanResultFullName: String? = null
    protected var filterScanResultCaseName01Text: String? = null
    protected var filterScanResultCaseName02Text: String? = null

    /**
     * 扫描状态下的蓝牙列表
     * Notice:当停止扫描时，会clean掉list；当重新扫描时，也会clean掉list
     * 用途：扫描场景下回调当前已经扫描的设备列表
     */
    protected val scanResultFilterList: MutableList<ScanResult> = mutableListOf()

    fun getScanFinishedList(): List<ScanResult> {
        return scanResultFilterList
    }

    /**
     * 连接回调Callback
     */
    val connectionCallbackList: MutableList<ConnectionCallback> = mutableListOf()

    /**
     * 收到消息回调Callback
     */
    val dataReceiveCallbackList: MutableList<DataReceivedCallback> = mutableListOf()

    /**
     * 蓝牙Notification打开 回调
     */
    val enableNotificationCallbackList: MutableList<EnableNotificationCallback> = mutableListOf()

    /**
     * 扫描设备回调Callback
     */
    val scanBleDeviceCallbackList: MutableList<ScanBleDeviceCallback> = mutableListOf()

    /**
     * 设备已经连接成功队列
     * 列表的最后一个设备即是当前连接的设备
     */
    private val connectedList: MutableList<BluetoothDevice> = mutableListOf()


    init {
        //观察设备连接状态
        setConnectionObserver(NordicBleConnectionObserver(connectionCallbackList))
    }

    fun configServiceUUID(UUID_SERVICE: String): BaseBleManager {
        this.UUID_SERVICE = UUID_SERVICE
        return this
    }

    fun configWriteUUID(UUID_WRITE: String): BaseBleManager {
        this.UUID_WRITE_CHAR = UUID_WRITE
        return this
    }

    fun configReadUUID(UUID_READ: String): BaseBleManager {
        this.UUID_READ_CHAR = UUID_READ
        return this
    }

    fun configNotifyUUID(UUID_NOTIFY_CHAR: String): BaseBleManager {
        this.UUID_NOTIFY_CHAR = UUID_NOTIFY_CHAR
        return this
    }

    fun configReceiveMessageCharacteristicUUID(receiverCharacteristicUUID: String): BaseBleManager {
        this.receiverCharacteristicUUID = receiverCharacteristicUUID
        return this
    }

    fun configAutoSplitLongData(autoSplitLongData: Boolean): BaseBleManager {
        this.useLongWrite = autoSplitLongData
        return this
    }

    fun configMtuValue(mtuValue: Int): BaseBleManager {
        this.mtuValue = mtuValue
        return this
    }

    fun getSettingsMtuValue(): Int {
        return mtuValue
    }

    /**
     * 发送数据 格式为Byte
     * @param byte
     */
    fun send(byte: ByteArray) {
        writeData(byte)
    }

    /**
     * 发送数据 格式为String
     * @param text the text to be sent
     */
    fun send(text: String) {
        writeData(text.toByteArray())
    }

    /**
     * 写入数据
     */
    private fun writeData(byteData: ByteArray) {
        if (!isConnected) {
            return
        }
        if (mWriteCharacteristic == null) {
            return
        }

//        val len = byteData.size
//        val mSendMsg = ByteArray(200)
//        if (len > 200) {
//            val temp_len = if (len > 200) 200 else len
//            val sndBuf = ByteArray(temp_len)
//            System.arraycopy(mSendMsg, 0, sndBuf, 0, temp_len)
//        }
//

        val request: WriteRequest = writeCharacteristic(mWriteCharacteristic, byteData)
            .with { device, data ->
                Log.i(TAG, data.getStringValue(0) + " to sent")
            }
            .done {
                Log.i(TAG, " data to sent done")
            }
            .fail { device, status ->
                Log.i(TAG, " data to sent failed (status:$status) ")
            }

        if (useLongWrite) {
            // This will automatically split the long data into MTU-3-byte long packets.
            // 这将自动将长数据拆分为MTU-3字节长数据包。
            request.split(DefaultMtuSplitter(), object : WriteProgressCallback {
                override fun onPacketSent(device: BluetoothDevice, data: ByteArray?, index: Int) {
                    data?.let {
                        val charset = Charsets.UTF_8
                        //val byteArray = "Hello".toByteArray(charset)
                        println(it.contentToString()) // [72, 101, 108, 108, 111]
                        val dataString = it.toString(charset)
                        println(dataString) // Hello

                        Log.i(TAG,
                            "onPacketSent -- > data size:${it.size} | index:$index content = $dataString")
                    }

                }

            })
        }
        request.enqueue()

    }


    /**
     * 检测是否连接
     */
    fun isBleConnect(): Boolean {
        return isConnected
    }

    /**
     * 断开连接
     */
    fun disconnectBle() {
        disconnect().enqueue()
        //确保连接状态断开
        isConnecting = false
    }

    /**
     * 请在connecting状态下获取
     */
    private var connectingBluetoothDevice: BluetoothDevice? = null

    fun getConnectingDevice(): BluetoothDevice {
        if (!isConnecting) {
            throw RuntimeException("请在connecting状态下获取正在连接的设备")
        }
        return connectingBluetoothDevice!!
    }

    /**
     * 连接蓝牙
     * 进行配置连接
     *
     * @param bluetoothDevice
     * @param needRetry
     * @param retryCount
     * @param retryDelayTime
     * @param autoConnect
     * @param connectTimeOut
     */
    fun connectBluetooth(
        bluetoothDevice: BluetoothDevice,
        needRetry: Boolean = true,
        retryCount: Int = 3,
        retryDelayTime: Int = 2000,
        autoConnect: Boolean = true,
        connectTimeOut: Long = 10000,
    ) {
        val requestConnect = connect(bluetoothDevice)

        if (needRetry) {
            requestConnect.retry(retryCount, retryDelayTime)
        }
        requestConnect.useAutoConnect(autoConnect)

        requestConnect
            .timeout(connectTimeOut)
            .enqueue()
        this.connectingBluetoothDevice = bluetoothDevice
    }

    /**
     * 观察扫描结果
     */
    fun observerScanCallback(
        context: Context,
        scanBleDeviceCallback: ScanBleDeviceCallback,
    ) {
        if (scanBleDeviceCallbackList.contains(scanBleDeviceCallback)) {
            throw RuntimeException("scanBleDeviceCallback have added scan callback,please do not added repeat ")
        }

        val lifecycleOwner = context as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    val r = scanBleDeviceCallbackList.remove(scanBleDeviceCallback)
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        scanBleDeviceCallbackList.add(scanBleDeviceCallback)
    }

    /**
     * 观察连接结果
     */
    fun observerConnectionCallback(
        context: Context,
        connectionCallback: ConnectionCallback,
    ) {
        if (connectionCallbackList.contains(connectionCallback)) {
            throw RuntimeException("connectionCallback have added scan callback,please do not added repeat ")
        }

        val lifecycleOwner = context as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    val r = connectionCallbackList.remove(connectionCallback)
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        connectionCallbackList.add(connectionCallback)
    }

    /**
     * 观察蓝牙数据返回
     */
    fun observerBleMessageCallback(
        context: Context,
        dataReceivedCallback: DataReceivedCallback,
    ) {
        if (dataReceiveCallbackList.contains(dataReceivedCallback)) {
            throw RuntimeException("dataReceivedCallback have added scan callback,please do not added repeat ")
        }

        val lifecycleOwner = context as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    val r = dataReceiveCallbackList.remove(dataReceivedCallback)
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        dataReceiveCallbackList.add(dataReceivedCallback)
    }

    /**
     * 观察蓝牙Notification是否打开成功
     */
    fun observerBleNotificationOpenCallback(
        context: Context,
        enableNotificationCallback: EnableNotificationCallback,
    ) {
        if (enableNotificationCallbackList.contains(enableNotificationCallback)) {
            throw RuntimeException("enableNotificationCallback have added scan callback,please do not added repeat ")
        }

        val lifecycleOwner = context as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    val r = enableNotificationCallbackList.remove(enableNotificationCallback)
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        enableNotificationCallbackList.add(enableNotificationCallback)
    }


    /**
     * 添加已经连接成功的设备
     */
    fun addConnectedBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        if (connectedList.contains(bluetoothDevice)) {
            return
        }
        connectedList.add(bluetoothDevice)
    }

    /**
     * 移除已经断开的设备
     */
    fun removeDisConnectedBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        connectedList.remove(bluetoothDevice)
    }

    /**
     * 判断当前蓝牙设备是否已经连接
     */
    fun isConnectedBluetoothDevice(bluetoothDevice: BluetoothDevice): Boolean {
        return connectedList.contains(bluetoothDevice)
    }

}