package com.afar.osaio.smart.mixipc.presenter;

import android.text.TextUtils;

import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.mixipc.contract.BluetoothScanContract;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.dao.BleApDeviceService;
import com.nooie.sdk.db.entity.BleApDeviceEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BluetoothScanPresenter implements BluetoothScanContract.Presenter {

    private BluetoothScanContract.View mTaskView;

    private Subscription mSendCmdListTask = null;
    private int mCurrentSendCmdListIndex = 0;

    public BluetoothScanPresenter(BluetoothScanContract.View view) {
        super();
        mTaskView = view;
        if (mTaskView != null) {
            mTaskView.setPresenter(this);
        }
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void startSendCmdList(int cmdListSize, BluetoothScanContract.SendCmdListListener listener) {
        stopSendCmdList();
        mSendCmdListTask = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onSendCmd(SDKConstant.ERROR, cmdListSize, mCurrentSendCmdListIndex);
                        }
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mCurrentSendCmdListIndex < cmdListSize) {
                            if (listener != null) {
                                listener.onSendCmd(SDKConstant.SUCCESS, cmdListSize, mCurrentSendCmdListIndex);
                            }
                            mCurrentSendCmdListIndex++;
                            if (mCurrentSendCmdListIndex == cmdListSize) {
                                stopSendCmdList();
                            }
                        } else {
                            stopSendCmdList();
                            if (listener != null) {
                                listener.onSendCmd(SDKConstant.SUCCESS, cmdListSize, mCurrentSendCmdListIndex);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopSendCmdList() {
        mCurrentSendCmdListIndex = 0;
        if (mSendCmdListTask != null && !mSendCmdListTask.isUnsubscribed()) {
            mSendCmdListTask.unsubscribe();
            mSendCmdListTask = null;
        }
    }

    @Override
    public void checkDeleteBleApDevice(String bleApDeviceId) {
        Observable.just(bleApDeviceId)
                .flatMap(new Func1<String, Observable<BleApDeviceEntity>>() {
                    @Override
                    public Observable<BleApDeviceEntity> call(String bleApDeviceId) {
                        List<BleApDeviceEntity> bleDeviceEntityList = BleApDeviceService.getInstance().getDevices();
                        if (TextUtils.isEmpty(bleApDeviceId) || CollectionUtil.isEmpty(bleDeviceEntityList)) {
                            return Observable.just(null);
                        }
                        BleApDeviceEntity result = null;
                        for (BleApDeviceEntity bleApDeviceEntity : CollectionUtil.safeFor(bleDeviceEntityList)) {
                            boolean isDeviceExist = bleApDeviceEntity != null && !TextUtils.isEmpty(bleApDeviceEntity.getBleDeviceId()) && bleApDeviceEntity.getBleDeviceId().equalsIgnoreCase(bleApDeviceId);
                            if (isDeviceExist) {
                                result = bleApDeviceEntity;
                                break;
                            }
                        }
                        return Observable.just(result);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BleApDeviceEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckDeleteBleApDevice(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BleApDeviceEntity result) {
                        if (mTaskView != null) {
                            mTaskView.onCheckDeleteBleApDevice(SDKConstant.SUCCESS, result);
                        }
                    }
                });
    }

    @Override
    public void deleteBleApDevice(String deviceId) {
        Observable.just(deviceId)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String deviceId) {
                        if (!TextUtils.isEmpty(deviceId)) {
                            BleApDeviceService.getInstance().deleteDevice(deviceId);
                            BleApDeviceInfoCache.getInstance().removeCacheById(deviceId);
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
                            mTaskView.onDeleteBleApDevice(SDKConstant.ERROR);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteBleApDevice(SDKConstant.SUCCESS);
                        }
                    }
                });
    }
}
