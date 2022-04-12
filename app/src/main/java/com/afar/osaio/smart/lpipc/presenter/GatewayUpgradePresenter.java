package com.afar.osaio.smart.lpipc.presenter;

import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.smart.lpipc.contract.GatewayUpgradeContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GatewayUpgradePresenter implements GatewayUpgradeContract.Presenter {

    private GatewayUpgradeContract.View mTaskView;

    public GatewayUpgradePresenter(GatewayUpgradeContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void queryDeviceUpgradeTime(String deviceId, String account, boolean isUpdateTime) {
        Observable.just(isUpdateTime)
                .flatMap(new Func1<Boolean, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Boolean isUpdateTime) {
                        long upgradeTime = 0;
                        if (isUpdateTime) {
                            upgradeTime = DateTimeUtil.getUtcCalendar().getTimeInMillis();
                            DeviceConfigureService.getInstance().updateUpgreadeTime(deviceId, account, upgradeTime);
                        } else {
                            DeviceConfigureEntity configureEntity = DeviceConfigureService.getInstance().getDeviceConfigure(deviceId, account);
                            upgradeTime = configureEntity != null ? configureEntity.getUpgradeTime() : 0;
                        }
                        NooieLog.d("-->> GatewayUpgradePresenter queryDeviceUpgradeTime {r call isUpdateTime=" + isUpdateTime + " updateTime=" + upgradeTime);
                        return Observable.just(upgradeTime);
                    }
                })
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
                    public void onNext(Long upgradeTime) {
                        int timeLen = (int)((DateTimeUtil.getUtcCalendar().getTimeInMillis() - upgradeTime) / 1000L);
                        if (timeLen < MAX_PROCESS) {
                            mCurrentProcess = timeLen;
                        } else {
                            mCurrentProcess = MAX_PROCESS;
                        }
                        NooieLog.d("-->> GatewayUpgradePresenter queryDeviceUpgradeTime {r onNext timeLen=" + timeLen + " mCurrentProcess=" + mCurrentProcess);
                        startUpdateProcessTask();
                        queryDeviceUpdateStatus(deviceId);
                    }
                });
    }

    private Subscription mUpdateProcessTask = null;
    private int mCurrentProcess = 0;
    private static final int MAX_PROCESS = 240;
    private static final int PROCESS_PER_LEN = 4;
    private static final int UPGRADE_PROCESS_LEN = 90;

    public void startUpdateProcessTask() {
        stopUpdateProcessTask();
        int intervalTime = PROCESS_PER_LEN * 1000;
        mUpdateProcessTask = Observable.interval(0, intervalTime, TimeUnit.MILLISECONDS)
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
                        /*
                        float process = 90 * (mCurrentProcess / 240f);
                        NooieLog.d("-->> NooieDeviceInfoPresenter startUpdateProcessTask process=" + process);
                        */
                        NooieLog.d("-->> GatewayUpgradePresenter startUpdateProcessTask {r onNext mCurrentProcess=" + mCurrentProcess);
                        //mCurrentProcess++;
                        mCurrentProcess += PROCESS_PER_LEN;
                        if (mCurrentProcess > MAX_PROCESS) {
                            stopUpdateProcessTask();
                        }
                    }
                });
    }

    @Override
    public void stopUpdateProcessTask() {
        if (mUpdateProcessTask != null && !mUpdateProcessTask.isUnsubscribed()) {
            NooieLog.d("-->> GatewayUpgradePresenter {r stopUpdateProcessTask");
            mUpdateProcessTask.unsubscribe();
        }
    }

    public int getUpdateProcess() {
        //float process = Math.round((90 * (mCurrentProcess / 240f)) * 10) / 10;
        float process = Math.round((UPGRADE_PROCESS_LEN * (mCurrentProcess / ((float)MAX_PROCESS))) * 10) / 10;
        process = process < 100 ? process : 100;
        NooieLog.d("-->> GatewayUpgradePresenter {r getUpdateProcess process=" + process);
        return (int)process;
    }

    private static final int MAX_QUERY_UPDATE_STATE_COUNT = 2;
    private int mQueryUpdateStateCount = 1;
    private Subscription mQueryUpdateTask;
    @Override
    public void queryDeviceUpdateStatus(String deviceId) {
        stopQueryDeviceUpdateState();
        mQueryUpdateTask = DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.delay(5 * 1000, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceUpdateStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            int type = response.getData() != null ? response.getData().getType() : ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
                            NooieLog.d("-->> GatewayUpgradePresenter {r onNext type=" + type);
                            switch (type) {
                                case ApiConstant.DEVICE_UPDATE_TYPE_NORMAL: {
                                    if (mCurrentProcess >= MAX_PROCESS) {
                                        //stopQueryDeviceUpdateState();
                                        stopUpdateProcessTask();
                                    }
                                    break;
                                }
                                case ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START:
                                case ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_FINISH:
                                case ApiConstant.DEVICE_UPDATE_TYPE_INSTALL_START:
                                case ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS: {
                                    break;
                                }
                                case ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH: {
                                    stopQueryDeviceUpdateState();
                                    stopUpdateProcessTask();
                                    break;
                                }
                                case ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED: {
                                    stopQueryDeviceUpdateState();
                                    stopUpdateProcessTask();
                                    break;
                                }
                            }
                            if (mTaskView != null) {
                                mTaskView.onQueryDeviceUpdateStatus(type, getUpdateProcess());
                            }
                        } else if (mTaskView != null) {
                        }
                    }
                });
    }

    @Override
    public void stopQueryDeviceUpdateState() {
        if (mQueryUpdateTask != null && !mQueryUpdateTask.isUnsubscribed()) {
            NooieLog.d("-->> GatewayUpgradePresenter {r stopQueryDeviceUpdateState");
            mQueryUpdateTask.unsubscribe();
        }
    }

    @Override
    public void startUpdateDevice(String account, String deviceId, String model, String version, String pkt, String md5) {
        NooieLog.d("-->> debug GatewayUpgradePresenter startUpdateDevice: account=" + account + " deviceId=" + deviceId + " model=" + model + " version=" + version + " pkt=" + pkt + " md5=" + md5);
        DeviceCmdApi.getInstance().upgrade(deviceId, model, version, pkt, md5, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (code == Constant.OK) {
                    if (mTaskView != null) {
                        mTaskView.onStartUpdateDeviceResult(ConstantValue.SUCCESS);
                    }
                    queryDeviceUpgradeTime(deviceId, account, true);
                } else if (mTaskView != null) {
                    mTaskView.onStartUpdateDeviceResult(ConstantValue.ERROR);
                }
            }
        });
    }
}
