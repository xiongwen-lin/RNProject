package com.android.nordicbluetooth.callback

import no.nordicsemi.android.support.v18.scanner.ScanResult

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
interface ScanBleDeviceCallback {
    fun onScanResult(callbackType: Int, result: ScanResult)
    fun onBatchScanResultsByFilter(results: List<ScanResult>)
    fun onScanFailed(errorCode: Int)
}