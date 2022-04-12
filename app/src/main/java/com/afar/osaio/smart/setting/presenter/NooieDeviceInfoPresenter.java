package com.afar.osaio.smart.setting.presenter;

import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.DeviceCfg;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.view.INooieDeviceInfoView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.setting.SettingService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * NooieDeviceInfoPresenter
 *
 * @author Administrator
 * @date 2019/4/19
 */
public class NooieDeviceInfoPresenter implements INooieDeviceInfoPresenter {
    private INooieDeviceInfoView mDeviceInfoVew;
    private Subscription mQueryUpdateTask;
    private Subscription mUpdateProcessTask = null;
    private int mCurrentProcess = 0;
    private static final int MAX_PROCESS = 240;
    private static final int PROCESS_PER_LEN = 4;
    private static final int UPGRADE_PROCESS_LEN = 90;

    public NooieDeviceInfoPresenter(INooieDeviceInfoView view) {
        this.mDeviceInfoVew = view;
    }

    @Override
    public void destroy() {
        if (mDeviceInfoVew != null) {
            mDeviceInfoVew = null;
        }
    }

    @Override
    public void rename(final String deviceId, final String alias) {
        DeviceService.getService().updateDeviceName(deviceId, alias)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.notifyDeviceRenameState(NooieApplication.get().getString(R.string.rename_failed));
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mDeviceInfoVew != null) {
                            NooieDeviceHelper.updateDeviceName(deviceId, alias);
                            mDeviceInfoVew.notifyDeviceRenameState(ConstantValue.SUCCESS);
                        } else if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.notifyDeviceRenameState(NooieApplication.get().getString(R.string.rename_failed));
                        }
                    }
                });
    }

    /**
     * 获取设备信息 Get device information
     *
     * @param deviceCfg
     */
    @Override
    public void loadInfos(DeviceCfg deviceCfg) {
        if (deviceCfg == null || TextUtils.isEmpty(deviceCfg.getDeviceId())) {
            if (mDeviceInfoVew != null) {
                mDeviceInfoVew.onLoadDeviceFailed(ConstantValue.ERROR);
            }
            return;
        }
        if (deviceCfg.getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            DeviceCmdApi.getInstance().getDevInfo(deviceCfg.getDeviceId(), new OnGetDevInfoListener() {
                @Override
                public void onDevInfo(int code, DevInfo info) {
                    if (mDeviceInfoVew != null) {
                        mDeviceInfoVew.onGetDeviceInfo(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, info);
                    }
                }
            });
        } else {
            getNooieDevice(deviceCfg.getDeviceId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<NooieDevice>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (mDeviceInfoVew != null) {
                                mDeviceInfoVew.onLoadDeviceFailed(ConstantValue.ERROR);
                            }
                        }

                        @Override
                        public void onNext(NooieDevice nooieDevice) {
                            if (mDeviceInfoVew != null) {
                                mDeviceInfoVew.onLoadDeviceSuccess(nooieDevice);
                            }
                        }
                    });
        }
    }

    private Observable<NooieDevice> getNooieDevice(String deviceId) {
        final NooieDevice device = new NooieDevice();
        return DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            device.setDevice(response.getData());
                            return getAppVersionObservable(deviceId, response.getData().getType(), response.getData());
                        }
                        return Observable.just(null);
                    }
                })
                .flatMap(new Func1<BaseResponse<AppVersionResult>, Observable<NooieDevice>>() {
                    @Override
                    public Observable<NooieDevice> call(BaseResponse<AppVersionResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            device.setAppVersionResult(response.getData());
                        }
                        return Observable.just(device);
                    }
                });
    }

    private Observable<BaseResponse<AppVersionResult>> getAppVersionObservable(String deviceId, String model, BindDevice bindDevice) {
        Observable<BaseResponse<AppVersionResult>> hardVersionObservable = SettingService.getService().getHardVersion(model);
        Observable<BaseResponse<BindDevice>> deviceInfoObservable = null;
        if (bindDevice != null) {
            BaseResponse<BindDevice> response = new BaseResponse<>();
            response.setCode(StateCode.SUCCESS.code);
            response.setData(bindDevice);
            deviceInfoObservable = Observable.just(response);
        } else {
            deviceInfoObservable = DeviceService.getService().getDeviceInfo(deviceId);
        }
        return Observable.zip(hardVersionObservable, deviceInfoObservable, new Func2<BaseResponse<AppVersionResult>, BaseResponse<BindDevice>, BaseResponse<AppVersionResult>>() {
            @Override
            public BaseResponse<AppVersionResult> call(BaseResponse<AppVersionResult> versionResponse, BaseResponse<BindDevice> deviceResponse) {
                if (deviceResponse != null && deviceResponse.getCode() == StateCode.SUCCESS.code && deviceResponse.getData() != null) {
                    if (versionResponse != null && versionResponse.getCode() == StateCode.SUCCESS.code && versionResponse.getData() != null) {
                        versionResponse.getData().setCurrentVersionCode(deviceResponse.getData().getVersion());
                    }
                }
                return versionResponse;
            }
        });
    }

    @Override
    public void loadFirmwareVersion(String deviceId) {
        SettingService.getService().getHardVersion(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<AppVersionResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onLoadFirmwareInfoFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<AppVersionResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mDeviceInfoVew != null) {
                            mDeviceInfoVew.onLoadFirmwareInfoSuccess(response.getData());
                        } else if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onLoadFirmwareInfoFailed(response != null ? response.getMsg() : "");
                        }
                    }
                });
    }

    @Override
    public void startUpdateDevice(String account, final String deviceId, String model, String version, String pkt, String md5, boolean isSubDevice) {
        mIsStartingUpgradeDevice = true;
        DeviceCmdApi.getInstance().upgrade(deviceId, model, version, pkt, md5, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> NooieDeviceInfoPresenter startUpdateDevice onResult code=" + code);
                if (code == Constant.OK) {
                    mIsStartingUpgradeDevice = false;
                    if (mDeviceInfoVew != null) {
                        mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.SUCCESS);
                    }
                    queryDeviceUpgradeTime(deviceId, account, true);
                } else if (mDeviceInfoVew != null) {
                    startQueryUpgradeForTimeout(deviceId, account, true);
                }
            }
        });
    }

    private void startUpdateSubDevice(String account, final String deviceId, String model, String version, String pkt, String md5) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).camUpgrade(deviceId, model, version, pkt, md5, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> NooieDeviceInfoPresenter onResult code=" + code);
                if (code == Constant.OK) {
                    if (mDeviceInfoVew != null) {
                        mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.SUCCESS);
                    }
                    queryDeviceUpgradeTime(deviceId, account, true);
                } else if (mDeviceInfoVew != null) {
                    mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.ERROR);
                }
            }
        });
    }

    private Subscription mCheckDeviceUpgradeScheduleTask;
    private boolean mIsCheckingDeviceUpgradeSchedule = false;
    @Override
    public void checkDeviceUpgradeSchedule(String deviceId, String account) {
        if (mIsCheckingDeviceUpgradeSchedule) {
            return;
        }
        mIsCheckingDeviceUpgradeSchedule = true;
        stopCheckDeviceUpgradeScheduleTask();
        mCheckDeviceUpgradeScheduleTask = DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .flatMap(new Func1<BaseResponse<DeviceUpdateStatusResult>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            boolean isUpdating = NooieDeviceHelper.isDeviceUpdating(response.getData().getType());
                            DeviceConfigureEntity configureEntity = DeviceConfigureService.getInstance().getDeviceConfigure(deviceId, account);
                            long upgradeTime = configureEntity != null ? configureEntity.getUpgradeTime() : 0;
                            int timeLen = (int)((DateTimeUtil.getUtcCalendar().getTimeInMillis() - upgradeTime) / 1000L);
                            NooieLog.d("-->> NooieDeviceInfoPresenter checkDeviceUpgradeSchedule call upgrade type=" + response.getData().getType() + " isUpdating=" + isUpdating + " updateTime=" + upgradeTime + " timeLen=" + timeLen);
                            return Observable.just(!isUpdating && timeLen > MAX_PROCESS);
                        }
                        return Observable.just(false);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<NooieDevice>>() {
                    @Override
                    public Observable<NooieDevice> call(Boolean isUpgradeFinish) {
                        if (isUpgradeFinish) {
                            return getNooieDevice(deviceId);
                        }
                        return Observable.just(null);
                    }
                })
                .flatMap(new Func1<NooieDevice, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(NooieDevice device) {
                        AppVersionResult appVersionResult = device != null ? device.getAppVersionResult() : null;
                        boolean isNewVersion = appVersionResult != null && NooieDeviceHelper.compareVersion(appVersionResult.getVersion_code(), appVersionResult.getCurrentVersionCode()) > 0;
                        NooieLog.d("-->> NooieDeviceInfoPresenter checkDeviceUpgradeSchedule call isNewVersion=" + isNewVersion);
                        return Observable.just(!isNewVersion);
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
                        if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onCheckDeviceUpgradeScheduleResult(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean isUpgradeFinish) {
                        NooieLog.d("-->> NooieDeviceInfoPresenter checkDeviceUpgradeSchedule onNext isUpgradeFinish=" + isUpgradeFinish);
                        if (isUpgradeFinish) {
                            stopQueryDeviceUpdateState();
                        }
                        if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onCheckDeviceUpgradeScheduleResult(ConstantValue.SUCCESS, isUpgradeFinish);
                        }
                    }
                });
    }

    public void stopCheckDeviceUpgradeScheduleTask() {
        if (mCheckDeviceUpgradeScheduleTask != null && !mCheckDeviceUpgradeScheduleTask.isUnsubscribed()) {
            mCheckDeviceUpgradeScheduleTask.unsubscribe();
            mCheckDeviceUpgradeScheduleTask = null;
        }
    }

    private Subscription mQueryDeviceUpgradeTimeTask;
    @Override
    public void queryDeviceUpgradeTime(String deviceId, String account, boolean isUpdateTime) {
        stopQueryDeviceUpgradeTime();
        mQueryDeviceUpgradeTimeTask = Observable.just(isUpdateTime)
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
                        NooieLog.d("-->> NooieDeviceInfoPresenter queryDeviceUpgradeTime onNext currentTime=" + DateTimeUtil.getUtcCalendar().getTimeInMillis() + " upgradeTime=" + upgradeTime + " timeLen=" + timeLen);
                        if (timeLen < MAX_PROCESS) {
                            mCurrentProcess = timeLen;
                        } else {
                            mCurrentProcess = MAX_PROCESS;
                        }
                        startUpdateProcessTask();
                        queryDeviceUpdateStatus(deviceId, account, isUpdateTime);
                    }
                });
    }

    public void stopQueryDeviceUpgradeTime() {
        NooieLog.d("-->> NooieDeviceInfoPresenter stopQueryDeviceUpgradeTime");
        if (mQueryDeviceUpgradeTimeTask != null && !mQueryDeviceUpgradeTimeTask.isUnsubscribed()) {
            mQueryDeviceUpgradeTimeTask.unsubscribe();
        }
        mCurrentProcess = 0;
        stopUpdateProcessTask();
        stopQueryDeviceUpdateState();
    }

    @Override
    public void startUpdateProcessTask() {
        stopUpdateProcessTask();
        int intervalTime = PROCESS_PER_LEN * 1000;// (int)((((float)UPGRADE_PROCESS_LEN) / MAX_PROCESS) * 1000) * PROCESS_PER_LEN;
        NooieLog.d("-->> NooieDeviceInfoPresenter startUpdateProcessTask intervalTime=" + intervalTime);
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
        NooieLog.d("-->> NooieDeviceInfoPresenter stopUpdateProcessTask");
        if (mUpdateProcessTask != null && !mUpdateProcessTask.isUnsubscribed()) {
            mUpdateProcessTask.unsubscribe();
        }
    }

    @Override
    public int getUpdateProcess() {
        float process = Math.round((UPGRADE_PROCESS_LEN * (mCurrentProcess / ((float)MAX_PROCESS))) * 10) / 10;
        process = process < 100 ? process : 100;
        NooieLog.d("-->> NooieDeviceInfoPresenter getUpdateProcess process=" + process);
        return (int)process;
    }

    @Override
    public void queryDeviceUpdateStatus(String deviceId, String account, boolean isUpdateTime) {
        mIsCheckingDeviceUpgradeSchedule = !isUpdateTime;
        stopQueryDeviceUpdateState();
        mQueryUpdateTask = Observable.interval(0, 5000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<BaseResponse<DeviceUpdateStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceUpdateStatusResult>> call(Long aLong) {
                        return DeviceService.getService().getDeviceUpdateStatus(deviceId)
                                .onErrorReturn(new Func1<Throwable, BaseResponse<DeviceUpdateStatusResult>>() {
                                    @Override
                                    public BaseResponse<DeviceUpdateStatusResult> call(Throwable throwable) {
                                        return null;
                                    }
                                });
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
                        if (mDeviceInfoVew != null) {
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            int type = response.getData() != null ? response.getData().getType() : ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
                            NooieLog.d("-->> NooieDeviceInfoPresenter queryDeviceUpdateStatus onNext type=" + type + " currentProcess=" + mCurrentProcess);
                            switch (type) {
                                case ApiConstant.DEVICE_UPDATE_TYPE_NORMAL: {
                                    if (mCurrentProcess >= MAX_PROCESS) {
                                        if (!isUpdateTime) {
                                            stopQueryDeviceUpdateState();
                                        }
                                        //stopQueryDeviceUpdateState();
                                        //stopUpdateProcessTask();
                                        checkDeviceUpgradeSchedule(deviceId, account);
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
                            if (mDeviceInfoVew != null) {
                                mDeviceInfoVew.onQueryNooieDeviceUpdateStatusSuccess(type, getUpdateProcess());
                            }
                        }
                    }
                });
    }

    @Override
    public void stopQueryDeviceUpdateState() {
        NooieLog.d("-->> NooieDeviceInfoPresenter stopQueryDeviceUpdateState");
        if (mQueryUpdateTask != null && !mQueryUpdateTask.isUnsubscribed()) {
            mQueryUpdateTask.unsubscribe();
        }
    }

    private Subscription mStartQueryUpgradeForTimeoutTask = null;
    private boolean mIsStartingUpgradeDevice = false;
    public void startQueryUpgradeForTimeout(String deviceId, String account, boolean isUpdateTime) {
        if (!mIsStartingUpgradeDevice) {
            stopQueryUpgradeForTimeout();
            return;
        }
        mStartQueryUpgradeForTimeoutTask = DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceUpdateStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mIsStartingUpgradeDevice = false;
                        if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        mIsStartingUpgradeDevice = false;
                        boolean isUpdating = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && NooieDeviceHelper.isDeviceUpdating(response.getData().getType());
                        if (isUpdating) {
                            if (mDeviceInfoVew != null) {
                                mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.SUCCESS);
                            }
                            queryDeviceUpgradeTime(deviceId, account, true);
                        } else if (mDeviceInfoVew != null) {
                            mDeviceInfoVew.onStartUpdateDeviceResult(ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void stopQueryUpgradeForTimeout() {
        if (mStartQueryUpgradeForTimeoutTask != null && !mStartQueryUpgradeForTimeoutTask.isUnsubscribed()) {
            mStartQueryUpgradeForTimeoutTask.unsubscribe();
        }
    }

}
