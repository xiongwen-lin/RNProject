package com.afar.osaio.smart.bluetooth.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.bluetooth.component.BluetoothCmdComponent;
import com.android.nordicbluetooth.SmartBleManager;
import com.android.nordicbluetooth.callback.ConnectionCallback;
import com.android.nordicbluetooth.callback.EnableNotificationCallback;
import com.android.nordicbluetooth.callback.ScanBleDeviceCallback;
import com.android.nordicbluetooth.observer.BluetoothStateActionObserver;
import com.android.nordicbluetooth.observer.GPSOnOffObserver;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.bluetooth.listener.OnBleConnectListener;
import com.afar.osaio.smart.bluetooth.listener.OnBleStartScanListener;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.mixipc.profile.bean.BleConnectState;
import com.afar.osaio.smart.mixipc.profile.cache.BleDeviceScanCache;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kotlin.Unit;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BaseBluetoothActivity extends BaseActivity implements ScanBleDeviceCallback, ConnectionCallback, DataReceivedCallback, BluetoothStateActionObserver.BluetoothStateEvent {

    public static final int DEFAULT_BLE_SCAN_TIME = 15 * 1000;
    public static final int MIN_BLE_SCAN_TIME = 5 * 1000;
    public static final int BLUETOOTH_CONNECT_TIME_OUT = 15 * 1000;
    public static final int BLUETOOTH_RETRY_DELAY_TIME = 2 * 1000;
    public static final int BLUETOOTH_CONNECT_RETRY_COUNT = 3;

    public static final int BLUETOOTH_OPERATION_TIP_TYPE_FAIL = 0;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_SUCCESS = 1;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL = 2;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_UNBIND = 3;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_DISCONNECT = 4;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_BOUND = 5;
    public static final int BLUETOOTH_OPERATION_TIP_TYPE_BOUND_BY_OTHER = 6;

    private BluetoothCmdComponent mBluetoothCmdComponent = null;

    public BaseBluetoothActivity() {
        initBluetoothCmdComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseBluetoothCmdComponent();
    }

    public void initBle() {
        //?????????????????????????????? - ????????????????????????--???????????????????????????
        new BluetoothStateActionObserver(this, this, this);

        //??????GPS??????
        new GPSOnOffObserver(this, this, new GPSOnOffObserver.GpsStateEvent() {
            @Override
            public void gspStateOnChange() {
                //?????????Gps??????
                NooieLog.d("-->> debug BaseBluetoothActivity gspStateOnChange: ");
            }

            @Override
            public void gspStateOffChange() {
                //?????????Gps??????
                NooieLog.d("-->> debug BaseBluetoothActivity gspStateOffChange: ");
            }
        });

        SmartBleManager.core.observerScanCallback(this, this);

        SmartBleManager.core.observerConnectionCallback(this, this);

        SmartBleManager.core.observerBleNotificationOpenCallback(this, new EnableNotificationCallback() {
            @Override
            public void onRequestCompleted(@NotNull BluetoothDevice device) {
                //??????Notification????????????
                NooieLog.d("-->> debug BaseBluetoothActivity onRequestCompleted: ");
            }

            @Override
            public void onRequestFailed(@NotNull BluetoothDevice device, int status) {
                //??????Notification????????????
                NooieLog.d("-->> debug BaseBluetoothActivity onRequestFailed: ");
            }
        });

        SmartBleManager.core.observerBleMessageCallback(this, this);
    }

    /* BluetoothStateActionObserver.BluetoothStateEvent */
    @Override
    public void bluetoothStateOnChange() {
        //????????????
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothStateOnChange: ");
    }

    @Override
    public void bluetoothStateOffChange() {
        //????????????
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothStateOffChange: ");
    }

    @Override
    public void bluetoothConnected() {
        //???????????????
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothConnected: ");
    }

    @Override
    public void bluetoothDisConnected() {
        //???????????????,????????????????????????????????????
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothDisConnected: ");
    }

    /* ScanBleDeviceCallback */
    @Override
    public void onScanResult(int callbackType, @NotNull ScanResult result) {
        NooieLog.d("-->> debug BaseBluetoothActivity onScanResult: result=" + (result != null ? result.toString() : null));
    }

    @Override
    public void onBatchScanResultsByFilter(@NotNull List<ScanResult> results) {
        NooieLog.d("-->> debug BaseBluetoothActivity onBatchScanResultsByFilter: size=" + (results != null ? results.size() : 0));

        for (ScanResult result : CollectionUtil.safeFor(results)) {
            NooieLog.d("-->> debug BaseBluetoothActivity onBatchScanResultsByFilter: result=" + result.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        NooieLog.d("-->> debug BaseBluetoothActivity onScanFailed: errorCode=" + errorCode);
    }

    /* ConnectionCallback */
    @Override
    public void onDeviceConnecting(@NotNull BluetoothDevice device) {
        //??????????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceConnecting: ");
    }

    @Override
    public void onDeviceFailedToConnectTimeout(@NotNull BluetoothDevice device) {
        //??????????????????->??????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceFailedToConnectTimeout: ");
        onDeviceFailedToConnect(device);
    }

    @Override
    public void onDeviceFailedToConnectNotSupport(@NotNull BluetoothDevice device) {
        //??????????????????->???????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceFailedToConnectNotSupport: ");
        onDeviceFailedToConnect(device);
    }

    @Override
    public void onDeviceFailedToConnectReasonUnknown(@NotNull BluetoothDevice device, int reason) {
        //??????????????????->????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceFailedToConnectReasonUnknown: ");
        onDeviceFailedToConnect(device);
    }

    @Override
    public void onDeviceConnected(@NotNull BluetoothDevice device) {
        //??????????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceConnected: ");
    }

    @Override
    public void onDeviceReady(@NotNull BluetoothDevice device) {
        //??????Ready???????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceReady: ");
    }

    @Override
    public void onDeviceDisconnecting(@NotNull BluetoothDevice device) {
        //????????????????????????
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceDisconnecting: ");
    }

    @Override
    public void onDeviceLinkLost(@NotNull BluetoothDevice device) {
        //??????????????????(???????????????????????????)
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceLinkLost: ");
        onDeviceDisconnected(device);
    }

    @Override
    public void onDeviceTerminateLocalHost(@NotNull BluetoothDevice device) {
        //??????????????????(????????????????????????)
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceTerminateLocalHost: ");
        onDeviceDisconnected(device);
    }

    @Override
    public void onDeviceTerminateRemoteHost(@NotNull BluetoothDevice device) {
        //??????????????????(????????????????????????)
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceTerminateRemoteHost: ");
        onDeviceDisconnected(device);
    }

    @Override
    public void onDeviceDisConnectUnknownReason(@NotNull BluetoothDevice device, int reason) {
        //??????????????????(?????????????????????)
        NooieLog.d("-->> debug BaseBluetoothActivity onDeviceDisConnectUnknownReason: ");
        onDeviceDisconnected(device);
    }

    /* DataReceivedCallback */
    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        //replace end /r
        NooieLog.d("-->> debug BaseBluetoothActivity onDataReceived: data=" + (data != null ? (data.toString() + " text=" + data.getStringValue(0)): null));
        attachBluetoothCmdDataReceiver(device, data);
    }

    public void startScanBle(int scanTimeout, OnBleStartScanListener listener) {
        BleDeviceScanCache.getInstance().clearCache();
        if (scanTimeout < MIN_BLE_SCAN_TIME) {
            scanTimeout = DEFAULT_BLE_SCAN_TIME;
        }
        SmartBleManager.core.startScan(scanTimeout,
                () -> {
                    //??????????????????Timeout
                    if (listener != null) {
                        listener.onTimeFinish();
                    }
                    return Unit.INSTANCE;
                },
                null,
                null,
                null,
                null,
                "",
                "");
    }

    public void stopScanBle() {
        SmartBleManager.core.stopScan();
    }

    public boolean checkBluetoothIsScanning() {
        return SmartBleManager.core.isScanning();
    }

    public void connectDevice(ScanResult result) {
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice");
        if (result == null) {
            return;
        }
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice result=" + result);
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice ????????????????????? name=" + result.getDevice().getName());

        if (SmartBleManager.core.isConnectedBluetoothDevice(result.getDevice())) {
            NooieLog.d("-->> debug BaseBluetoothActivity connectDevice ????????????????????????????????? " + result.getDevice().getName());
            return;
        }

        //??????????????????????????????
        SmartBleManager.core.stopScan();
        //Notice ???????????????????????????????????????????????????????????????????????????????????????
        SmartBleManager.core.connectBluetooth(result.getDevice(), true, 3, 2000, true, 10000);
    }

    public void connectDevice(BluetoothDevice bluetoothDevice, OnBleConnectListener listener) {
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice");
        if (bluetoothDevice == null) {
            return;
        }
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice result=" + bluetoothDevice.toString());
        NooieLog.d("-->> debug BaseBluetoothActivity connectDevice ????????????????????? name=" + bluetoothDevice.getName());

        if (SmartBleManager.core.isConnectedBluetoothDevice(bluetoothDevice)) {
            NooieLog.d("-->> debug BaseBluetoothActivity connectDevice ????????????????????????????????? " + bluetoothDevice.getName());
            if (listener != null) {
                listener.onResult(BleConnectState.DEVICE_READY, bluetoothDevice);
            }
            return;
        }

        //??????????????????????????????
        SmartBleManager.core.stopScan();
        //Notice ???????????????????????????????????????????????????????????????????????????????????????
        SmartBleManager.core.connectBluetooth(bluetoothDevice, true, BLUETOOTH_CONNECT_RETRY_COUNT, BLUETOOTH_RETRY_DELAY_TIME, true, BLUETOOTH_CONNECT_TIME_OUT);
    }

    public void sendCmd(String cmd) {
        SmartBleManager.core.send(cmd);
    }

    public boolean checkMatchBluetooth(BluetoothDevice currentDevice, BluetoothDevice device) {
        boolean isMatch = currentDevice != null && device != null && currentDevice.getAddress() != null && currentDevice.getAddress().equalsIgnoreCase(device.getAddress());
        return isMatch;
    }

    public List<ScanResult> filterDeviceBluetooth(List<ScanResult> scanResultList, String bluetoothAddress) {
        List<ScanResult> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(scanResultList)) {
            return resultList;
        }
        Iterator<ScanResult> resultIterator = scanResultList.iterator();
        while (resultIterator.hasNext()) {
            ScanResult result = resultIterator.next();
            boolean isResultMatching = result != null && result.getDevice() != null && NooieDeviceHelper.checkBluetoothFutureCode(result.getDevice().getName())
                    && (TextUtils.isEmpty(bluetoothAddress) || NooieDeviceHelper.checkBluetoothAddressMatching(bluetoothAddress, result.getDevice().getAddress()));
            if (isResultMatching) {
                NooieLog.d("-->> debug BaseBluetoothActivity filterDeviceBluetooth isResultMatching device bluetoothAddress=" + bluetoothAddress + " name=" + result.getDevice().getName() + " address=" + result.getDevice().getAddress());
                resultList.add(result);
            }
        }
        return resultList;
    }

    public void tryRetrySendBleCmd(String cmd, String cmdRspKey, BluetoothDevice device) {
        if (mBluetoothCmdComponent != null && !TextUtils.isEmpty(cmdRspKey) && device != null) {
            mBluetoothCmdComponent.tryRetryStartSendCmd(cmd, cmdRspKey, device);
        } else {
            sendCmd(cmd);
        }
    }

    public void onDeviceFailedToConnect(@NotNull BluetoothDevice device) {
    }

    public void onDeviceDisconnected(@NotNull BluetoothDevice device) {
    }

    private void initBluetoothCmdComponent() {
        mBluetoothCmdComponent = new BluetoothCmdComponent();
    }

    private void releaseBluetoothCmdComponent() {
        if (mBluetoothCmdComponent != null) {
            mBluetoothCmdComponent.release();
        }
        mBluetoothCmdComponent = null;
    }

    private void attachBluetoothCmdDataReceiver(@NonNull BluetoothDevice device, @NonNull Data data) {
        if (mBluetoothCmdComponent != null) {
            mBluetoothCmdComponent.onDataReceived(device, data);
        }
    }

}
