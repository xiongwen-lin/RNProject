package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.SmartLockCmd;
import com.afar.osaio.smart.smartlook.contract.LockAccountContract;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.listener.SmartLockManagerListener;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import no.nordicsemi.android.ble.utils.ParserUtils;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LockAccountPresenter extends BaseBlePresenter<SmartLookManager> implements LockAccountContract.Presenter, SmartLookManagerCallbacks {

    private LockAccountContract.View mTaskView;
    private SmartLookManager mSmartLookManager;

    public LockAccountPresenter(LockAccountContract.View view) {
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
                    NooieLog.d("-->> LockAccountPresenter onWriteDataReceive data=" + ParserUtils.parse(data));
                    if (data == null || data.length == 0) {
                        return;
                    }

                    int cmdCode = data[0] & 0xFF;
                    if (cmdCode == SmartLockCmd.LOCK_CMD_B8 && data.length > 1) {
                        int dataMinLength = 7;
                        if (data.length < dataMinLength || (data[1] & 0xFF) == 0) {
                            return;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void loadData(String account, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<BleDeviceEntity>>() {
                    @Override
                    public Observable<BleDeviceEntity> call(String s) {
                        BleDeviceEntity bleDeviceEntity = BleDeviceService.getInstance().getDevice(account, deviceId);
                        return Observable.just(bleDeviceEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BleDeviceEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyLoadDataResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BleDeviceEntity bleDeviceEntity) {
                        if (mTaskView != null) {
                            mTaskView.notifyLoadDataResult(ConstantValue.SUCCESS, bleDeviceEntity);
                        }
                    }
                });
    }

    @Override
    public void deleteAccount(BleDeviceEntity deviceEntity) {
        Observable.just(deviceEntity)
                .flatMap(new Func1<BleDeviceEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BleDeviceEntity bleDeviceEntity) {
                        if (bleDeviceEntity != null) {
                            BleDeviceService.getInstance().deleteDevice(bleDeviceEntity.getUser(), bleDeviceEntity.getDeviceId());
                        }
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
                        if (mTaskView != null) {
                            mTaskView.notifyDeleteAccountResult(ConstantValue.ERROR, "");
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTaskView != null) {
                            String deviceId = deviceEntity != null ? deviceEntity.getDeviceId() : "";
                            mTaskView.notifyDeleteAccountResult(ConstantValue.SUCCESS, deviceId);
                        }
                    }
                });
    }

}
