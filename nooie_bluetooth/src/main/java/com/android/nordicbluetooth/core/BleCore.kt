package com.android.nordicbluetooth.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import com.android.nordicbluetooth.tools.BleLogger
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class BleCore(context: Context) : BaseBleManager(context) {
    private val TAG = BleCore::class.java.simpleName

    override fun getGattCallback(): BleManagerGattCallback {
        return GattCallback()
    }

    override fun log(priority: Int, message: String) {
        //BleLogger.i(TAG, "priority:$priority message:$message")
    }

    override fun shouldClearCacheWhenDisconnected(): Boolean {
        return false
    }

    /**
     * 设置Notification
     */
    private fun setDeviceNotificationCallback(notificationCharacteristic: BluetoothGattCharacteristic) {
        setNotificationCallback(notificationCharacteristic)
            .with { device, data ->
                val text = data.getStringValue(0)
                //BleLogger.i(TAG, "notificationCharacteristic收到数据text：$text received")
                dataReceiveCallbackList.forEach {
                    it.onDataReceived(device, data)
                }
            }

        enableNotifications(notificationCharacteristic)
            .done { device ->
                BleLogger.i(TAG, "enableNotifications 成功--> SuccessCallback ")
                enableNotificationCallbackList.forEach {
                    it.onRequestCompleted(device)
                }
            }
            .fail { device, status ->
                BleLogger.i(TAG, "enableNotifications 失败--> onRequestFailed status: $status")
                enableNotificationCallbackList.forEach {
                    it.onRequestFailed(device, status)
                }
            }
            .enqueue()

        if (useLongWrite) {
            requestMtu(getSettingsMtuValue()).enqueue()
            //requestMtu(247).enqueue()
        }

    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class GattCallback : BleManagerGattCallback() {
        override fun initialize() {
            BleLogger.i(TAG, "isRequiredServiceSupported --> GattCallback init ")
            //设备已连接准备，打开Notify
            if (receiverCharacteristicUUID.isNullOrEmpty()) {
                throw RuntimeException("receiverCharacteristicUUID uuid must configure,Please setup receive message uuid!")
            }
            //可扩展 -- 某些设备接收消息用的特征UUID不一定是Notify UUID
            if (receiverCharacteristicUUID.equals(UUID_NOTIFY_CHAR)) {
                mNotifyCharacteristic?.let { setDeviceNotificationCallback(it) }
            }
            if (receiverCharacteristicUUID.equals(UUID_READ_CHAR)) {
                mReadCharacteristic?.let { setDeviceNotificationCallback(it) }
            }

        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            BleLogger.i(TAG, "isRequiredServiceSupported --> gatt:$gatt")
            if (UUID_SERVICE.isNullOrEmpty()) {
                throw RuntimeException("service uuid must configure,Please setup service uuid!")
            }
            val service = gatt.getService(UUID.fromString(UUID_SERVICE))
            if (service == null) {
                BleLogger.i(TAG, "isRequiredServiceSupported --> service uuid is null 获取不到service disconnectBle")
                //找不到服务特征时，将断开当前的连接
                disconnectBle()
                return false
            }
            mUUIDService = service

            //可从service.characteristics中匹配蓝牙设备的UUID
            /*
            val characteristicArray: List<BluetoothGattCharacteristic> = service.characteristics
            characteristicArray.forEach {
                val uuid: UUID = it.uuid
                val uuidValue = uuid.toString()
                BleLogger.i(TAG, "isRequiredServiceSupported :uuidValue == $uuidValue")
                if (uuidValue == UUID_NOTIFY_CHAR) {
                    mNotifyCharacteristic = service.getCharacteristic(UUID_NOTIFY_CHAR)
                }

                if (uuidValue == UUID_WRITE_CHAR) {
                    mWriteCharacteristic = service.getCharacteristic(LBS_UUID_WRITE_CHAR)
                }

                if (uuidValue == UUID_READ_CHAR) {
                    mReadCharacteristic = service.getCharacteristic(LBS_UUID_READ_CHAR)
                }
            }*/

            if (UUID_WRITE_CHAR.isNullOrEmpty()) {
                throw RuntimeException("write uuid must configure,Please setup write uuid!")
            }
            mWriteCharacteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE_CHAR))
            if (mWriteCharacteristic == null) {
                throw RuntimeException("mWriteCharacteristic == null ,please checkout your device support write characteristic")
            }

            var writeRequest = false
            mWriteCharacteristic?.let { mWriteCharacteristic ->
                val rxProperties = mWriteCharacteristic.properties
                writeRequest = (rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) || (rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0)

                // Set the WRITE REQUEST type when the characteristic supports it.
                // This will allow to send long write (also if the characteristic support it).
                // In case there is no WRITE REQUEST property, this manager will divide texts
                // longer then MTU-3 bytes into up to MTU-3 bytes chunks.
                //当特征支持时，设置WRITE REQUEST类型。 这将允许发送长写（如果特性支持的话）。如果没有WRITE REQUEST属性，则此管理器将分割文本
                //将更长的MTU-3字节转换成最多MTU-3字节的块。
                //TODO 需要知道设置是否支持WRITE REQUEST，若不支持，会出现发送指令，接收不到的情况
                if (writeRequest) {
                    //默认写入类型 -  Write characteristic, requesting acknoledgement by the remote device
                    if (rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                        mWriteCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    } else {
                        mWriteCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    }
                    //可以写入特性 -  Characteristic property: Characteristic can be written.
                    //mWriteCharacteristic.writeType = BluetoothGattCharacteristic.PROPERTY_WRITE
                }
            }

            if (!UUID_NOTIFY_CHAR.isNullOrEmpty()) {
                mNotifyCharacteristic = service.getCharacteristic(UUID.fromString(UUID_NOTIFY_CHAR))
            }

            if (!UUID_READ_CHAR.isNullOrEmpty()) {
                mReadCharacteristic = service.getCharacteristic(UUID.fromString(UUID_READ_CHAR))
            }


            //Debug
            if (mNotifyCharacteristic == null) {
                BleLogger.i(TAG, "isRequiredServiceSupported --> mNotifyCharacteristic == null")
            }


            if (mWriteCharacteristic == null) {
                BleLogger.i(TAG, "isRequiredServiceSupported --> mWriteCharacteristic == null")
            }

            if (mReadCharacteristic == null) {
                BleLogger.i(TAG, "isRequiredServiceSupported --> mReadCharacteristic == null")
            }

            var isSupport = false
            if (!UUID_READ_CHAR.isNullOrEmpty() && UUID_NOTIFY_CHAR.isNullOrEmpty()) {
                isSupport = mWriteCharacteristic != null
                        && mReadCharacteristic != null
                        && mUUIDService != null
                        && writeRequest
            }

            if (UUID_READ_CHAR.isNullOrEmpty() && !UUID_NOTIFY_CHAR.isNullOrEmpty()) {
                isSupport = mNotifyCharacteristic != null
                        && mWriteCharacteristic != null
                        && mUUIDService != null
                        && writeRequest
            }

            if (!UUID_READ_CHAR.isNullOrEmpty() && !UUID_NOTIFY_CHAR.isNullOrEmpty()) {
                isSupport = mNotifyCharacteristic != null
                        && mWriteCharacteristic != null
                        && mReadCharacteristic != null
                        && mUUIDService != null
                        && writeRequest
            }

            BleLogger.i(TAG, "isRequiredServiceSupported --> isSupport:$isSupport")

            return isSupport
        }

        override fun onDeviceDisconnected() {
            BleLogger.i(TAG, "onDeviceDisconnected 设备断开连接")
            mNotifyCharacteristic = null
            mWriteCharacteristic = null
            mReadCharacteristic = null
            mUUIDService = null
        }

        override fun onDeviceReady() {
            BleLogger.i(TAG, "onDeviceReady 设备已连接")
        }
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
        isScanning = false
        scanResultFilterList.clear()
        scannerTimeoutHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 开始扫描
     */
    fun startScan(
        scanTimeOut: Long = 10000,
        scanTimeoutCallback: () -> Unit,
        filterBuilderDeviceName: String? = null,
        filterBuilderDeviceAddress: String? = null,
        filterBuilderServiceUuid: String? = null,
        filterScanResultFullName: String? = null,
        filterScanResultCaseName01Text: String? = null,
        filterScanResultCaseName02Text: String? = null,
    ) {
        if (isScanning) {
            BleLogger.i(TAG, "正在扫描中...回调已经扫描的列表")
            scanBleDeviceCallbackList.forEach {
                it.onBatchScanResultsByFilter(scanResultFilterList)
            }
            return
        }
        scannerTimeoutHandler.removeCallbacksAndMessages(null)

        this.filterScanResultFullName = filterScanResultFullName
        this.filterScanResultCaseName01Text = filterScanResultCaseName01Text
        this.filterScanResultCaseName02Text = filterScanResultCaseName02Text
        val isConnect = isBleConnect()
        if (isConnect) {
            BleLogger.i(TAG, "设备已经连接,继续扫描")
        }
        BleLogger.i(TAG, "开始扫描")

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            //SCAN_MODE_BALANCED
            .setUseHardwareBatchingIfSupported(false)
            .setUseHardwareFilteringIfSupported(false)
            .build()
        val filters: MutableList<ScanFilter> = ArrayList()
        val scanBuilder = ScanFilter.Builder()
        if (!filterBuilderDeviceName.isNullOrEmpty()) {
            scanBuilder.setDeviceName(filterBuilderDeviceName)
        }
        if (!filterBuilderDeviceAddress.isNullOrEmpty()) {
            scanBuilder.setDeviceAddress(filterBuilderDeviceAddress)
        }
        if (!filterBuilderServiceUuid.isNullOrEmpty()) {
            scanBuilder.setServiceUuid(ParcelUuid.fromString(filterBuilderServiceUuid))
        }
        filters.add(scanBuilder.build())

        //扫描超时后停止扫描
        scannerTimeoutHandler.postDelayed({
            stopScan()
            scanTimeoutCallback()
        }, scanTimeOut)

        isScanning = true
        scanResultFilterList.clear()

        scanner.startScan(filters, settings, scanCallback)
    }

    private fun addFilterResult(scanResult: ScanResult) {
        val existed = scanResultFilterList.find {
            it.device.address == scanResult.device.address
        }
        //不存在才添加
        if (existed == null) {
            scanResultFilterList.add(scanResult)
        }
        scanBleDeviceCallbackList.forEach {
            it.onBatchScanResultsByFilter(scanResultFilterList)
        }
    }

    /**
     * 扫描回调
     */
    private val scanCallback: ScanCallback = object : ScanCallback() {
        /**
         * 扫描失败
         */
        override fun onScanFailed(errorCode: Int) {
            BleLogger.i(TAG, "ScanCallback --> onScanFailed:errorCode-->:$errorCode")
            scanBleDeviceCallbackList.forEach {
                it.onScanFailed(errorCode)
            }
        }

        /**
         * Callback when a BLE advertisement has been found.
         * 找到BLE advertisement 后进行回调。
         */
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            BleLogger.i(TAG, "ScanCallback --> onScanResult:$result")
            addFilterResult(result)
        }
        //扫描结果回调
        /**
         * Callback when batch results are delivered.
         * 批处理结果交付时回调。
         */
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (i in results.indices) {
                val scanResult: ScanResult = results[i]
                //BleLogger.i(TAG, "ScanCallback --> onBatchScanResults --- >$scanResult")
                val mBluetoothDevice: BluetoothDevice? = scanResult.device
                if (mBluetoothDevice != null) {
                    val devName = mBluetoothDevice.name
                    if (devName != null) {
                        if (!filterScanResultFullName.isNullOrEmpty()
                            && devName == filterScanResultFullName
                        ) {
                            addFilterResult(scanResult)
                        }
                        if (!filterScanResultCaseName01Text.isNullOrEmpty() && devName.contains(
                                filterScanResultCaseName01Text!!)
                        ) {
                            addFilterResult(scanResult)
                        }

                        if (!filterScanResultCaseName02Text.isNullOrEmpty() && devName.contains(
                                filterScanResultCaseName02Text!!)
                        ) {
                            addFilterResult(scanResult)
                        }


                        if (filterScanResultFullName.isNullOrEmpty() && filterScanResultCaseName01Text.isNullOrEmpty()) {
                            addFilterResult(scanResult)
                        }
                    }
                }
            }
        }
    }
}