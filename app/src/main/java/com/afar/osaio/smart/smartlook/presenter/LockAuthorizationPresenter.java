package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.SmartLockCmd;
import com.afar.osaio.smart.smartlook.contract.LockAuthorizationContract;
import com.afar.osaio.smart.smartlook.db.dao.LockAuthorizationService;
import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.listener.SmartLockManagerListener;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.data.DataHelper;
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

public class LockAuthorizationPresenter extends BaseBlePresenter<SmartLookManager> implements LockAuthorizationContract.Presenter, SmartLookManagerCallbacks {

    private static final int WAIT_FOR_UPDATE_RECORD_FROM_DEVICE = 5000;

    LockAuthorizationContract.View mTaskView;
    private SmartLookManager mSmartLookManager;
    private String mUserAccount;
    private String mDeviceId;
    private List<String> mLockAuthorizationKeys = new ArrayList<>();

    public LockAuthorizationPresenter(LockAuthorizationContract.View view) {
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
                    NooieLog.d("-->> LockAuthorizationPresenter onWriteDataReceive data=" + ParserUtils.parse(data));
                    if (data == null || data.length == 0) {
                        return;
                    }

                    int cmdCode = data[0] & 0xFF;
                    if (cmdCode == SmartLockCmd.LOCK_CMD_B8 && data.length > 1) {
                        int dataMinLength = 7;
                        if (data.length < dataMinLength || (data[1] & 0xFF) == 0) {
                            return;
                        }
                        List<LockAuthorizationEntity> lockAuthorizationEntities = new ArrayList<>();
                        for (int i = 0; i < (data.length - 2) / 5; i++) {
                            int j = i * 5 + 2;
                            byte[] codeBytes = new byte[] {data[j + 1], data[j + 2], data[j + 3], data[j + 4]};
                            String authorizationKey = SmartLookDeviceHelper.parseToHexString(codeBytes);
                            if (mLockAuthorizationKeys != null && mLockAuthorizationKeys.contains(authorizationKey)) {
                                continue;
                            }
                            NooieLog.d("-->> LockAuthorizationPresenter onWriteDataReceive deviceIndex=" + SmartLookDeviceHelper.parseToHexString(data[j]) + " code=" + SmartLookDeviceHelper.parseToHexString(codeBytes));
                            LockAuthorizationEntity lockAuthorizationEntity = new LockAuthorizationEntity();
                            lockAuthorizationEntity.setUser(mUserAccount);
                            lockAuthorizationEntity.setDeviceId(mDeviceId);
                            lockAuthorizationEntity.setCodeIndex(SmartLookDeviceHelper.convertByteToStr(data[j]));
                            lockAuthorizationEntity.setCode(authorizationKey);
                            lockAuthorizationEntities.add(lockAuthorizationEntity);
                        }
                        saveLockAuthorizationInDb(lockAuthorizationEntities);
                    } else if (cmdCode == SmartLockCmd.LOCK_CMD_B7 && data.length > 1) {
                        int dataMinLength = 7;
                        if (data.length < dataMinLength || (data[1] & 0xFF) != 0) {
                            return;
                        }
                        int j = 2;
                        byte[] codeBytes = new byte[] {data[j + 1], data[j + 2], data[j + 3], data[j + 4]};
                        String authorizationKey = SmartLookDeviceHelper.parseToHexString(codeBytes);
                        NooieLog.d("-->> LockAuthorizationPresenter onWriteDataReceive deviceIndex=" + SmartLookDeviceHelper.parseToHexString(data[j]) + " code=" + SmartLookDeviceHelper.parseToHexString(codeBytes));
                        LockAuthorizationEntity lockAuthorizationEntity = new LockAuthorizationEntity();
                        lockAuthorizationEntity.setUser(mUserAccount);
                        lockAuthorizationEntity.setDeviceId(mDeviceId);
                        lockAuthorizationEntity.setCodeIndex(SmartLookDeviceHelper.convertByteToStr(data[j]));
                        lockAuthorizationEntity.setCode(authorizationKey);
                        saveNewLockAuthorizationInDb(lockAuthorizationEntity);
                    } else if (cmdCode == SmartLockCmd.LOCK_CMD_B9 && data.length > 1) {
                        int dataMinLength = 2;
                        if (data.length < dataMinLength) {
                            return;
                        }
                        boolean deleteResult = (data[1] & 0xFF) == 0;
                        NooieLog.d("-->> LockAuthorizationPresenter onWriteDataReceive cmd=" + cmdCode + " delete code success" + deleteResult);
                    }
                }
            });
        }
    }

    public void saveLockAuthorizationInDb(List<LockAuthorizationEntity> lockAuthorizationEntities) {
        if (CollectionUtil.isEmpty(lockAuthorizationEntities)) {
            return;
        }

        Observable.just(lockAuthorizationEntities)
                .flatMap(new Func1<List<LockAuthorizationEntity>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<LockAuthorizationEntity> lockAuthorizationEntities) {
                        for (LockAuthorizationEntity lockAuthorizationEntity : CollectionUtil.safeFor(lockAuthorizationEntities)) {
                            LockAuthorizationService.getInstance().updateAuthorization(lockAuthorizationEntity);
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

    public void saveNewLockAuthorizationInDb(LockAuthorizationEntity lockAuthorizationEntity) {
        if (lockAuthorizationEntity == null) {
            return;
        }

        Observable.just(lockAuthorizationEntity)
                .flatMap(new Func1<LockAuthorizationEntity, Observable<LockAuthorizationEntity>>() {
                    @Override
                    public Observable<LockAuthorizationEntity> call(LockAuthorizationEntity authorizationEntity) {
                        LockAuthorizationService.getInstance().updateAuthorization(authorizationEntity);
                        return Observable.just(authorizationEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LockAuthorizationEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyCreateAuthorization(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(LockAuthorizationEntity authorizationEntity) {
                        if (mTaskView != null) {
                            mTaskView.notifyCreateAuthorization(ConstantValue.SUCCESS, authorizationEntity);
                        }
                    }
                });
    }

    @Override
    public void getLockAuthorizations(String user, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<List<LockAuthorizationEntity>>>() {
                    @Override
                    public Observable<List<LockAuthorizationEntity>> call(String s) {
                        List<LockAuthorizationEntity> lockAuthorizations = LockAuthorizationService.getInstance().getAuthorizations(user, deviceId);
                        if (mLockAuthorizationKeys != null) {
                            mLockAuthorizationKeys.clear();
                            mLockAuthorizationKeys.addAll(SmartLookDeviceHelper.getAuthorizationKeys(lockAuthorizations));
                        }
                        return Observable.just(lockAuthorizations);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LockAuthorizationEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockAuthorization(ConstantValue.ERROR, Collections.emptyList());
                        }
                    }

                    @Override
                    public void onNext(List<LockAuthorizationEntity> lockAuthorizationEntities) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockAuthorization(ConstantValue.SUCCESS, SmartLookDeviceHelper.sortLockAuthorizations(lockAuthorizationEntities));
                        }
                    }
                });
    }

    @Override
    public void getLockAuthorizationsFromDevice(String user, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (mSmartLookManager != null) {
                            mSmartLookManager.getAuthorCode();
                        }
                        return Observable.just(s);
                    }
                })
                .delay(WAIT_FOR_UPDATE_RECORD_FROM_DEVICE, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<String, Observable<List<LockAuthorizationEntity>>>() {
                    @Override
                    public Observable<List<LockAuthorizationEntity>> call(String s) {
                        List<LockAuthorizationEntity> lockAuthorizations = LockAuthorizationService.getInstance().getAuthorizations(user, deviceId);
                        if (mLockAuthorizationKeys != null) {
                            mLockAuthorizationKeys.clear();
                            mLockAuthorizationKeys.addAll(SmartLookDeviceHelper.getAuthorizationKeys(lockAuthorizations));
                        }
                        return Observable.just(lockAuthorizations);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LockAuthorizationEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockAuthorization(ConstantValue.ERROR, Collections.emptyList());
                        }
                    }

                    @Override
                    public void onNext(List<LockAuthorizationEntity> lockAuthorizationEntities) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetLockAuthorization(ConstantValue.SUCCESS, SmartLookDeviceHelper.sortLockAuthorizations(lockAuthorizationEntities));
                        }
                    }
                });
    }

    @Override
    public void updateAuthorization(LockAuthorizationEntity lockAuthorizationEntity) {
        Observable.just(lockAuthorizationEntity)
                .flatMap(new Func1<LockAuthorizationEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(LockAuthorizationEntity lockAuthorizationEntity) {
                        LockAuthorizationService.getInstance().updateAuthorization(lockAuthorizationEntity);
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
                        getLockAuthorizations(lockAuthorizationEntity.getUser(), lockAuthorizationEntity.getDeviceId());
                    }
                });
    }

    @Override
    public void deleteAuthorization(LockAuthorizationEntity lockAuthorizationEntity) {
        Observable.just(lockAuthorizationEntity)
                .flatMap(new Func1<LockAuthorizationEntity, Observable<LockAuthorizationEntity>>() {
                    @Override
                    public Observable<LockAuthorizationEntity> call(LockAuthorizationEntity lockAuthorizationEntity) {
                        if (lockAuthorizationEntity != null && getSmartLookManager() != null) {
                            getSmartLookManager().deleteAuthorCode(DataHelper.toInt(lockAuthorizationEntity.getCodeIndex()));
                            LockAuthorizationService.getInstance().deleteAuthorization(lockAuthorizationEntity.getUser(), lockAuthorizationEntity.getDeviceId(), lockAuthorizationEntity.getCode());
                        }
                        return Observable.just(lockAuthorizationEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LockAuthorizationEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyDeleteAuthorization(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(LockAuthorizationEntity authorizationEntity) {
                        if (mTaskView != null) {
                            mTaskView.notifyDeleteAuthorization(ConstantValue.SUCCESS, authorizationEntity);
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
