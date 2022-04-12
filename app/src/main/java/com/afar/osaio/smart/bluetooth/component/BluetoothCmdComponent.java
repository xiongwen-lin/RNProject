package com.afar.osaio.smart.bluetooth.component;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.BluetoothCmdMonitorBean;
import com.android.nordicbluetooth.SmartBleManager;
import com.nooie.common.utils.log.NooieLog;

import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.ble.data.Data;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BluetoothCmdComponent {

    private static final int BLUETOOTH_CMD_TIMEOUT = 3 * 1000;
    private static final int BLUETOOTH_CMD_CODE_TIMEOUT = 1;
    private static final int BLUETOOTH_CMD_CODE_ERROR = 2;
    private static final int BLUETOOTH_CMD_CODE_RETRY_END = 3;
    private static final int RETRY_START_SEND_CMD_MAX_COUNT = 3;

    private BluetoothCmdMonitorBean mBluetoothCmdMonitor = null;
    private Subscription mBluetoothCmdMonitorTask = null;
    private BluetoothCmdMonitorListener mListener = null;
    private int mRetryStartSendCmdCount = 1;

    public BluetoothCmdComponent() {
    }

    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        NooieLog.d("-->> debug BluetoothCmdComponent onDataReceived 1001 data=" + (data != null ? data.getStringValue(0) : ""));
        boolean cmdRspValid = checkCmdRspValid(mBluetoothCmdMonitor, device, data);
        NooieLog.d("-->> debug BluetoothCmdComponent onDataReceived 1002 cmdRspValid=" + cmdRspValid);
        if (!cmdRspValid) {
            return;
        }
        NooieLog.d("-->> debug BluetoothCmdComponent onDataReceived 1003");
        if (mBluetoothCmdMonitor != null) {
            NooieLog.d("-->> debug BluetoothCmdComponent onDataReceived 1004");
            mBluetoothCmdMonitor.setSuccess(true);
        }
        if (mListener != null) {
            NooieLog.d("-->> debug BluetoothCmdComponent onDataReceived 1005");
            mListener.onSuccess(mBluetoothCmdMonitor);
        }
    }

    public void startSendCmd(String cmd, String cmdRspKey, BluetoothDevice device, BluetoothCmdMonitorListener listener) {
        NooieLog.d("-->> debug BluetoothCmdComponent startSendCmd 1001");
        sendCmd(cmd);
        mBluetoothCmdMonitor = createBluetoothCmdMonitor(cmdRspKey, device);
        startBluetoothCmdMonitorTask(mBluetoothCmdMonitor);
        mListener = listener;
    }

    public void tryRetryStartSendCmd(String cmd, String cmdRspKey, BluetoothDevice device) {
        NooieLog.d("-->> debug BluetoothCmdComponent tryRetryStartSendCmd 1001");
        mRetryStartSendCmdCount = 1;
        retryStartSendCmd(cmd, cmdRspKey, device);
    }

    public void release() {
        stopBluetoothCmdMonitorTask();
        mBluetoothCmdMonitor = null;
        mListener = null;
    }

    private void sendCmd(String cmd) {
        SmartBleManager.core.send(cmd);
    }

    private BluetoothCmdMonitorBean createBluetoothCmdMonitor(String cmdRspKey, BluetoothDevice device) {
        BluetoothCmdMonitorBean bluetoothCmdMonitor = new BluetoothCmdMonitorBean();
        bluetoothCmdMonitor.setCmdRspKey(cmdRspKey);
        bluetoothCmdMonitor.setDevice(device);
        bluetoothCmdMonitor.setTime(System.currentTimeMillis());
        return bluetoothCmdMonitor;
    }

    private boolean checkCmdRspValid(BluetoothCmdMonitorBean bluetoothCmdMonitor, BluetoothDevice device, Data data) {
        if (bluetoothCmdMonitor == null || !checkMatchBluetooth(bluetoothCmdMonitor.getDevice(), device) || data == null) {
            return false;
        }
        String cmdResponse = data != null ? data.getStringValue(0) : null;
        try {
            boolean cmdRspValid = !TextUtils.isEmpty(cmdResponse) && !TextUtils.isEmpty(bluetoothCmdMonitor.getCmdRspKey())
                    && cmdResponse.startsWith(bluetoothCmdMonitor.getCmdRspKey());
            return cmdRspValid;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    private boolean checkMatchBluetooth(BluetoothDevice currentDevice, BluetoothDevice device) {
        boolean isMatch = currentDevice != null && device != null && currentDevice.getAddress() != null && currentDevice.getAddress().equalsIgnoreCase(device.getAddress());
        return isMatch;
    }

    private boolean checkMatchBluetoothCmdMonitor(BluetoothCmdMonitorBean currentMonitor, BluetoothCmdMonitorBean monitor) {
        return currentMonitor != null && monitor != null && checkMatchBluetooth(currentMonitor.getDevice(), monitor.getDevice())
                && currentMonitor.getCmdRspKey() != null && currentMonitor.getCmdRspKey().equalsIgnoreCase(monitor.getCmdRspKey());
    }

    private void startBluetoothCmdMonitorTask(BluetoothCmdMonitorBean bluetoothCmdMonitor) {
        NooieLog.d("-->> debug BluetoothCmdComponent startBluetoothCmdMonitorTask 1001");
        stopBluetoothCmdMonitorTask();
        mBluetoothCmdMonitorTask = Observable.just(bluetoothCmdMonitor)
                .delay(BLUETOOTH_CMD_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BluetoothCmdMonitorBean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        dealOnBluetoothCmdMonitorTask(BLUETOOTH_CMD_CODE_ERROR, bluetoothCmdMonitor);
                    }

                    @Override
                    public void onNext(BluetoothCmdMonitorBean bluetoothCmdMonitorBean) {
                        dealOnBluetoothCmdMonitorTask(BLUETOOTH_CMD_CODE_TIMEOUT, bluetoothCmdMonitor);
                    }
                });
    }

    private void dealOnBluetoothCmdMonitorTask(int code, BluetoothCmdMonitorBean bluetoothCmdMonitor) {
        NooieLog.d("-->> debug BluetoothCmdComponent dealOnBluetoothCmdMonitorTask 1001");
        boolean isValidResult = checkMatchBluetoothCmdMonitor(mBluetoothCmdMonitor, bluetoothCmdMonitor);
        if (!isValidResult) {
            NooieLog.d("-->> debug BluetoothCmdComponent dealOnBluetoothCmdMonitorTask 1002");
            return;
        }
        NooieLog.d("-->> debug BluetoothCmdComponent dealOnBluetoothCmdMonitorTask 1003");
        if (mListener != null) {
            mListener.onFailure(code, bluetoothCmdMonitor);
        }
    }

    private void stopBluetoothCmdMonitorTask() {
        NooieLog.d("-->> debug BluetoothCmdComponent stopBluetoothCmdMonitorTask 1001");
        if (mBluetoothCmdMonitorTask != null && !mBluetoothCmdMonitorTask.isUnsubscribed()) {
            NooieLog.d("-->> debug BluetoothCmdComponent stopBluetoothCmdMonitorTask 1002");
            mBluetoothCmdMonitorTask.unsubscribe();
            mBluetoothCmdMonitorTask = null;
        }
    }

    private void retryStartSendCmd(String cmd, String cmdRspKey, BluetoothDevice device) {
        NooieLog.d("-->> debug BluetoothCmdComponent retryStartSendCmd 1001 cmdRspKey=" + cmdRspKey);
        startSendCmd(cmd, cmdRspKey, device, new BluetoothCmdMonitorListener() {
            @Override
            public void onSuccess(BluetoothCmdMonitorBean bluetoothCmdMonitor) {
                NooieLog.d("-->> debug BluetoothCmdComponent retryStartSendCmd 1002");
                stopBluetoothCmdMonitorTask();
            }

            @Override
            public void onFailure(int code, BluetoothCmdMonitorBean bluetoothCmdMonitor) {
                NooieLog.d("-->> debug BluetoothCmdComponent retryStartSendCmd 1003");
                boolean isRetrySend = code == BLUETOOTH_CMD_CODE_TIMEOUT && checkMatchBluetoothCmdMonitor(mBluetoothCmdMonitor, bluetoothCmdMonitor)
                        && mRetryStartSendCmdCount < RETRY_START_SEND_CMD_MAX_COUNT;
                NooieLog.d("-->> debug BluetoothCmdComponent retryStartSendCmd 1004");
                mRetryStartSendCmdCount++;
                if (isRetrySend) {
                    retryStartSendCmd(cmd, cmdRspKey, device);
                }
            }
        });
    }

    public interface BluetoothCmdMonitorListener {

        void onSuccess(BluetoothCmdMonitorBean bluetoothCmdMonitor);

        void onFailure(int code, BluetoothCmdMonitorBean bluetoothCmdMonitor);
    }
}
