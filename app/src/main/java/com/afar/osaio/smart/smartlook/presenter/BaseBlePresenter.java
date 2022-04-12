package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.bean.BleScanState;
import com.afar.osaio.smart.smartlook.contract.BaseBleContract;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.profile.callback.BaseBleManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.manager.BaseBleManager;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BaseBlePresenter<T extends BaseBleManager> implements BaseBleContract.Presenter, BaseBleManagerCallbacks {

    private static final int MAX_SCAN_BLUE_TOOTH_TIME = 15;

    private T mBleManager;
    private BleScanState mBleScanState;
    private Subscription mStartScanTask;

    public BaseBlePresenter() {
        mBleScanState = new BleScanState(BluetoothHelper.isBluetoothEnable(), BluetoothHelper.isLocationEnabled(NooieApplication.mCtx));
    }

    @Override
    public void startScanDeviceByTask(String user, List<String> filterDeviceIds) {
        stopScanDeviceByTask();

        startScan();
        mStartScanTask = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long time) {
                        boolean isStop = time > MAX_SCAN_BLUE_TOOTH_TIME || SmartLookDeviceHelper.isBleDevicesInScanCache(filterDeviceIds);
                        NooieLog.d("-->> BaseBlePresenter startScanDeviceByTask onNext time=" + time + " isStop=" + isStop);
                        if (isStop) {
                            stopScanDeviceByTask();
                            stopScan();
                            scanDeviceFinish(ConstantValue.SUCCESS);
                        }
                    }
                });
    }

    @Override
    public void scanDeviceFinish(String result) {
    }

    @Override
    public void stopScanDeviceByTask() {
        if (mStartScanTask != null && !mStartScanTask.isUnsubscribed()) {
            mStartScanTask.unsubscribe();
            mStartScanTask = null;
        }
    }

    /**
     * Start scanning for Bluetooth devices.
     */
    public void startScan() {
        try {
            if (mBleScanState.isScanning()) {
                return;
            }

            // Scanning settings
            final ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(500)
                    .setUseHardwareBatchingIfSupported(false)
                    // Hardware filtering has some issues on selected devices
                    .setUseHardwareFilteringIfSupported(false)
                    .build();

            List<ScanFilter> scanFilters = new ArrayList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"))
                    .build();
            scanFilters.add(scanFilter);

            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.startScan(scanFilters, settings, scanCallback);
            mBleScanState.scanningStarted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * stop scanning for bluetooth devices.
     */
    public void stopScan() {
        try {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
            mBleScanState.scanningStopped();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            NooieLog.d("-->> AddBluetoothDevicePresenter onScanResult");
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            logScanResults(results);
            // This callback will be called only if the report delay set above is greater then 0.

            for (final ScanResult result : results) {
                SmartLookDeviceHelper.filterAndSaveDevice(result);
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            NooieLog.d("-->> AddBluetoothDevicePresenter onScanFailed");
            mBleScanState.scanningStopped();
        }
    };

    public void setBleManager(T bleManager) {
        mBleManager = bleManager;
    }

    /**
     * Connect to peripheral.
     */
    @Override
    public void connect(Context context, final BleDevice device) {
        if (context == null || mBleManager == null || device == null) {
            return;
        }
        final LogSession logSession = Logger.newSession(context, null, device.getDevice().getAddress(), device.getDevice().getName());
        mBleManager.setLogger(logSession);
        mBleManager.connect(device.getDevice())
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue();
    }

    /**
     * Disconnect from peripheral.
     */
    @Override
    public void disconnect() {
        if (mBleManager == null) {
            return;
        }
        mBleManager.disconnect().enqueue();
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceConnecting");
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceConnected");
    }

    @Override
    public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceDisconnecting");
    }

    @Override
    public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceDisconnected");
    }

    @Override
    public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onLinkLossOccurred");
    }

    @Override
    public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
        NooieLog.d("-->> BaseBlePresenter onServicesDiscovered");
    }

    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceReady");
    }

    @Override
    public void onBondingRequired(@NonNull final BluetoothDevice device) {
        // device does not require bonding
        NooieLog.d("-->> BaseBlePresenter onBondingRequired");
    }

    @Override
    public void onBonded(@NonNull final BluetoothDevice device) {
        // device does not require bonding
        NooieLog.d("-->> BaseBlePresenter onBonded");
    }

    @Override
    public void onBondingFailed(@NonNull final BluetoothDevice device) {
        // device does not require bonding
        NooieLog.d("-->> BaseBlePresenter onBondingFailed");
    }

    @Override
    public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
        // TODO implement
        NooieLog.d("-->> BaseBlePresenter onError");
    }

    @Override
    public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
        NooieLog.d("-->> BaseBlePresenter onDeviceNotSupported");
    }

    private void logScanResults(List<ScanResult> scanResults) {
        if (scanResults == null || scanResults.size() == 0) {
            return;
        }
        for (ScanResult result : scanResults) {
            if (result != null) {
                NooieLog.d("-->> BaseBlePresenter logScanResults result=" + result.toString());
            }
        }
    }
}
