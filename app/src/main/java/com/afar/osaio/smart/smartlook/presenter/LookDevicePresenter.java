package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.SmartLockCmd;
import com.afar.osaio.smart.smartlook.contract.LookDeviceContract;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.listener.SmartLockManagerListener;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.log.NooieLog;

import no.nordicsemi.android.ble.utils.ParserUtils;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LookDevicePresenter extends BaseBlePresenter<SmartLookManager> implements LookDeviceContract.Presenter, SmartLookManagerCallbacks {

    LookDeviceContract.View mTaskView;
    private SmartLookManager mSmartLookManager;

    public LookDevicePresenter(LookDeviceContract.View view) {
        super();
        mTaskView = view;
        mSmartLookManager = new SmartLookManager(NooieApplication.mCtx, this);
        setBleManager(mSmartLookManager);
        setManagerListener();
        mTaskView.setPresenter(this);
    }

    @Override
    public SmartLookManager getSmartLookManager() {
        return mSmartLookManager;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void scanDeviceFinish(String result) {
        if (mTaskView != null) {
            mTaskView.onScanDeviceFinish(result);
        }
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
        super.onDeviceConnecting(device);
        if (mTaskView != null) {
            mTaskView.notifyBleDeviceState(BleConnectState.CONNECTING);
        }
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        super.onDeviceConnected(device);
        if (mTaskView != null) {
            mTaskView.notifyBleDeviceState(BleConnectState.CONNECTED);
        }
    }

    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        super.onDeviceReady(device);
        if (mTaskView != null) {
            mTaskView.notifyBleDeviceState(BleConnectState.DEVICE_READY);
        }
    }

    public void setManagerListener() {
        if (mSmartLookManager != null) {
            mSmartLookManager.setListener(new SmartLockManagerListener() {

                @Override
                public void onWriteDataReceive(byte[] data) {
                    NooieLog.d("-->> LookDevicePresenter onWriteDataReceive data=" + ParserUtils.parse(data));
                    if (data == null || data.length == 0) {
                        return;
                    }

                    int cmdCode = data[0] & 0xFF;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (cmdCode == SmartLockCmd.LOCK_CMD_BC) {
                        stringBuilder.setLength(0);
                        String id = "";
                        String notifyResult = ConstantValue.ERROR;
                        if (data.length > 6 && data[1] == 0) {
                            notifyResult = ConstantValue.SUCCESS;
                            id = SmartLookDeviceHelper.convertByteToStr(data[2]);
                            byte[] temp = new byte[4];
                            for (int i = 3; i <= 6; i++) {
                                temp[i-3] = data[i];
                            }
                            stringBuilder.append(SmartLookDeviceHelper.parseToHexString(temp));
                            NooieLog.d("-->> LookDevicePresenter onWriteDataReceive temp=" + SmartLookDeviceHelper.parseToHexString(temp) + " id=" + id);
                        }
                        if (mTaskView != null) {
                            mTaskView.notifyGetTemporaryPassword(notifyResult, id, stringBuilder.toString());
                        }
                    } else if (cmdCode == SmartLockCmd.LOCK_CMD_B5) {
                        int dataMinLength = 3;
                        if (data.length < dataMinLength && data[1] != 0) {
                            return;
                        }
                        int battery = DataHelper.toInt(SmartLookDeviceHelper.convertByteToStr(data[2]), 16);
                        if (mTaskView != null) {
                            mTaskView.notifyGetBattery(ConstantValue.SUCCESS, battery);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void updateLockBattery(String user, String deviceId, int battery) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        BleDeviceService.getInstance().updateDeviceBattery(user, deviceId, battery);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

}
