package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.SmartLockCmd;
import com.afar.osaio.smart.smartlook.contract.LockRecordContract;
import com.afar.osaio.smart.smartlook.db.dao.LockRecordService;
import com.nooie.sdk.db.entity.LockRecordEntity;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.listener.SmartLockManagerListener;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.encrypt.MD5Util;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.ble.utils.ParserUtils;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LockRecordPresenter extends BaseBlePresenter<SmartLookManager> implements LockRecordContract.Presenter, SmartLookManagerCallbacks {

    private static final int WAIT_FOR_UPDATE_RECORD_FROM_DEVICE = 5000;

    LockRecordContract.View mTaskView;
    private SmartLookManager mSmartLookManager;
    private String mUserAccount;
    private String mDeviceId;
    private List<String> mLockRecordKeys = new ArrayList<>();

    public LockRecordPresenter(LockRecordContract.View view) {
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
                    NooieLog.d("-->> LockRecordPresenter onWriteDataReceive data=" + ParserUtils.parse(data));
                    if (data == null || data.length == 0) {
                        return;
                    }

                    int cmdCode = data[0] & 0xFF;
                    int dataMinLength = 8;
                    if (cmdCode == SmartLockCmd.LOCK_CMD_B6 && data.length > 1) {
                        if (data.length < dataMinLength || (data[1] & 0xFF) == 0) {
                            return;
                        }
                        List<LockRecordEntity> lockRecordEntities = new ArrayList<>();
                        for (int i = 0; i < (data.length - 2) / 6; i++) {
                            int j = i * 6 + 2;
                            byte[] recordKeyBytes = new byte[] {data[j], data[j + 1], data[j + 2], data[j + 3], data[j + 4], data[j + 5]};
                            String recordKey = MD5Util.MD5Hash(SmartLookDeviceHelper.parseToHexString(recordKeyBytes));
                            if (mLockRecordKeys != null && mLockRecordKeys.contains(recordKey)) {
                                continue;
                            }
                            byte[] timeBytes = new byte[] {data[j + 1], data[j + 2], data[j + 3], data[j + 4]};
                            int nameType = data[j + 5] & 0xFF;
                            NooieLog.d("-->> LockRecordPresenter onWriteDataReceive deviceIndex=" + SmartLookDeviceHelper.parseToHexString(data[j]) + " name=" + SmartLookDeviceHelper.parseToHexString(data[j + 5])
                                    + " time=" + SmartLookDeviceHelper.convertRecordTime(timeBytes));
                            LockRecordEntity lockRecordEntity = new LockRecordEntity();
                            lockRecordEntity.setRecordKey(recordKey);
                            lockRecordEntity.setUser(mUserAccount);
                            lockRecordEntity.setDeviceId(mDeviceId);
                            lockRecordEntity.setDeviceIndex(SmartLookDeviceHelper.parseToHexString(data[j]));
                            lockRecordEntity.setName(SmartLookDeviceHelper.convertRecordName(NooieApplication.mCtx, nameType));
                            lockRecordEntity.setNameType(nameType);
                            lockRecordEntity.setTime(SmartLookDeviceHelper.convertRecordTime(timeBytes));
                            lockRecordEntities.add(lockRecordEntity);
                        }
                        saveLockRecordInDb(lockRecordEntities);
                    }
                }
            });
        }
    }

    public void saveLockRecordInDb(List<LockRecordEntity> lockRecordEntities) {
        if (CollectionUtil.isEmpty(lockRecordEntities)) {
            return;
        }

        Observable.just(lockRecordEntities)
                .flatMap(new Func1<List<LockRecordEntity>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<LockRecordEntity> lockRecordEntities) {
                        for (LockRecordEntity lockRecordEntity : CollectionUtil.safeFor(lockRecordEntities)) {
                            LockRecordService.getInstance().addRecord(lockRecordEntity.getRecordKey(), lockRecordEntity.getUser(), lockRecordEntity.getDeviceId(), lockRecordEntity.getDeviceIndex(), lockRecordEntity.getName(), lockRecordEntity.getNameType(), lockRecordEntity.getTime());
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
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

    @Override
    public void getLockRecords(String user, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<List<LockRecordEntity>>>() {
                    @Override
                    public Observable<List<LockRecordEntity>> call(String s) {
                        List<LockRecordEntity> lockRecords = LockRecordService.getInstance().getRecords(user, deviceId);
                        if (mLockRecordKeys != null) {
                            mLockRecordKeys.clear();
                            mLockRecordKeys.addAll(SmartLookDeviceHelper.getRecordKeys(lockRecords));
                        }
                        return Observable.just(lockRecords);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LockRecordEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockRecord(ConstantValue.ERROR, Collections.emptyList());
                        }
                    }

                    @Override
                    public void onNext(List<LockRecordEntity> lockRecordEntities) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockRecord(ConstantValue.SUCCESS, SmartLookDeviceHelper.sortLockRecords(lockRecordEntities));
                        }
                    }
                });
    }

    @Override
    public void getLockRecordsFromDevice(String user, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (mSmartLookManager != null) {
                            mSmartLookManager.getSmartLookRecord();
                        }
                        return Observable.just(s);
                    }
                })
                .delay(WAIT_FOR_UPDATE_RECORD_FROM_DEVICE, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<String, Observable<List<LockRecordEntity>>>() {
                    @Override
                    public Observable<List<LockRecordEntity>> call(String s) {
                        List<LockRecordEntity> lockRecords = LockRecordService.getInstance().getRecords(user, deviceId);
                        if (mLockRecordKeys != null) {
                            mLockRecordKeys.clear();
                            mLockRecordKeys.addAll(SmartLookDeviceHelper.getRecordKeys(lockRecords));
                        }
                        return Observable.just(lockRecords);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LockRecordEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockRecord(ConstantValue.ERROR, Collections.emptyList());
                        }
                    }

                    @Override
                    public void onNext(List<LockRecordEntity> lockRecordEntities) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockRecord(ConstantValue.SUCCESS, SmartLookDeviceHelper.sortLockRecords(lockRecordEntities));
                        }
                    }
                });
    }

    @Override
    public void setBaseInfo(String userAccount, String deviceId) {
        mUserAccount = userAccount;
        mDeviceId = deviceId;
    }

}
