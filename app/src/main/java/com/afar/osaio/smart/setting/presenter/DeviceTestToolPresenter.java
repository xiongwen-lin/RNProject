package com.afar.osaio.smart.setting.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceTestResult;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.contract.DeviceTestToolContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
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
import com.nooie.sdk.listener.OnActionResultListener;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class DeviceTestToolPresenter implements DeviceTestToolContract.Presenter {

    private DeviceTestToolContract.View mTaskView;
    private DeviceTestResult mDeviceTestResult;

    public DeviceTestToolPresenter(DeviceTestToolContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
        mDeviceTestResult = new DeviceTestResult();
    }

    @Override
    public void destroy() {
        mDeviceTestResult = null;
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
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
                            //return SettingService.getService().getHardVersion(response.getData().getType());
                            return getAppVersionObservable(deviceId, response.getData().getType());
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
                })
                .onErrorReturn(new Func1<Throwable, NooieDevice>() {
                    @Override
                    public NooieDevice call(Throwable throwable) {
                        return null;
                    }
                });
    }

    private Observable<BaseResponse<AppVersionResult>> getAppVersionObservable(String deviceId, String model) {
        Observable<BaseResponse<AppVersionResult>> hardVersionObservable = SettingService.getService().getHardVersion(model)
                .onErrorReturn(new Func1<Throwable, BaseResponse<AppVersionResult>>() {
                    @Override
                    public BaseResponse<AppVersionResult> call(Throwable throwable) {
                        return null;
                    }
                });
        Observable<BaseResponse<BindDevice>> deviceInfoObservable = DeviceService.getService().getDeviceInfo(deviceId)
                .onErrorReturn(new Func1<Throwable, BaseResponse<BindDevice>>() {
                    @Override
                    public BaseResponse<BindDevice> call(Throwable throwable) {
                        return null;
                    }
                });
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

    private Observable<NooieDevice> getDeviceDetail(String deviceId) {
        Observable<BaseResponse<DeviceUpdateStatusResult>> deviceUpgradeStatus = DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .onErrorReturn(new Func1<Throwable, BaseResponse<DeviceUpdateStatusResult>>() {
                    @Override
                    public BaseResponse<DeviceUpdateStatusResult> call(Throwable throwable) {
                        return null;
                    }
                });
        return Observable.zip(getNooieDevice(deviceId), deviceUpgradeStatus, new Func2<NooieDevice, BaseResponse<DeviceUpdateStatusResult>, NooieDevice>() {
            @Override
            public NooieDevice call(NooieDevice deviceDetail, BaseResponse<DeviceUpdateStatusResult> deviceUpdateStatusResponse) {
                if (deviceDetail != null && deviceUpdateStatusResponse != null && deviceUpdateStatusResponse.getCode() == StateCode.SUCCESS.code && deviceUpdateStatusResponse.getData() != null) {
                    deviceDetail.setDeviceUpdateStatusResult(deviceUpdateStatusResponse.getData());
                }
                return deviceDetail;
            }
        });
    }

    private Subscription mStartAutoUpgradeTestTask = null;
    @Override
    public void startAutoUpgradeTest(String deviceId) {
        NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest deviceId=" + deviceId);
        stopAutoUpgradeTest();
        if (mDeviceTestResult != null) {
            mDeviceTestResult.reset();
        }
        mLastUpgradeType = ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
        mStartAutoUpgradeTestTask = Observable.interval(0,5 * 60, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<NooieDevice>>() {
                    @Override
                    public Observable<NooieDevice> call(Long time) {
                        if (mDeviceTestResult != null) {
                            mDeviceTestResult.addUpgradeCount();
                        }
                        return getDeviceDetail(deviceId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NooieDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest error=" + (e != null ? e.toString() : ""));
                        if (mTaskView != null) {
                            mTaskView.onStartAutoUpgradeTest(ConstantValue.ERROR, 0);
                        }
                    }

                    @Override
                    public void onNext(NooieDevice device) {
                        NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest onNext deviceId=" + deviceId);
                        if (device != null && device.getDeviceUpdateStatusResult() != null && NooieDeviceHelper.isDeviceUpdating(device.getDeviceUpdateStatusResult().getType())) {
                            NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest onNext deviceId=" + deviceId + " updating");
                            if (mTaskView != null) {
                                mTaskView.onStartAutoUpgradeTest(ConstantValue.SUCCESS, 6);
                            }
                            return;
                        }
                        if (device != null && device.getDevice() != null && device.getAppVersionResult() != null) {
                            device.getDevice().setVersion("1.0.0");
                            if (device.getDevice().getOnline() != ApiConstant.ONLINE_STATUS_ON) {
                                NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest onNext deviceId=" + deviceId + " offline");
                                if (mTaskView != null) {
                                    mTaskView.onStartAutoUpgradeTest(ConstantValue.SUCCESS, 5);
                                }
                                return;
                            }
                            device.getAppVersionResult().setCurrentVersionCode("1.0.0");
                            AppVersionResult appVersionResult = device.getAppVersionResult();
                            boolean isUpgradable = appVersionResult != null && !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(appVersionResult.getModel()) && !TextUtils.isEmpty(appVersionResult.getKey()) && !TextUtils.isEmpty(appVersionResult.getCurrentVersionCode()) && !TextUtils.isEmpty(appVersionResult.getVersion_code()) && NooieDeviceHelper.compareVersion(appVersionResult.getVersion_code(), appVersionResult.getCurrentVersionCode()) > 0;
                            if (mTaskView != null) {
                                mTaskView.onStartAutoUpgradeTest(ConstantValue.SUCCESS, isUpgradable ? 3 : 4);
                            }
                            mSendAutoUpgradeTestCmdCount = 1;
                            sendAutoUpgradeTestCmd(deviceId, appVersionResult);
                        } else if (mTaskView != null) {
                            NooieLog.d("-->> DeviceTestToolPresenter startAutoUpgradeTest onNext fail deviceId=" + deviceId);
                            mTaskView.onStartAutoUpgradeTest(ConstantValue.ERROR, 0);
                        }
                    }
                });
    }

    @Override
    public void stopAutoUpgradeTest() {
        if (mStartAutoUpgradeTestTask != null && !mStartAutoUpgradeTestTask.isUnsubscribed()) {
            mStartAutoUpgradeTestTask.unsubscribe();
        }
    }

    private static final int SEND_AUTO_UPGRADE_TEST_CMD_MAX_COUNT = 3;
    private int mSendAutoUpgradeTestCmdCount = 1;
    private void sendAutoUpgradeTestCmd(String deviceId, AppVersionResult appVersionResult) {
        boolean isUpgradable = appVersionResult != null && !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(appVersionResult.getModel()) && !TextUtils.isEmpty(appVersionResult.getKey()) && !TextUtils.isEmpty(appVersionResult.getCurrentVersionCode()) && !TextUtils.isEmpty(appVersionResult.getVersion_code()) && NooieDeviceHelper.compareVersion(appVersionResult.getVersion_code(), appVersionResult.getCurrentVersionCode()) > 0;
        NooieLog.d("-->> DeviceTestToolPresenter sendAutoUpgradeTestCmd deviceId=" + deviceId + " isUpgradable=" + isUpgradable);
        if (!isUpgradable) {
            return;
        }
        if (mSendAutoUpgradeTestCmdCount <= SEND_AUTO_UPGRADE_TEST_CMD_MAX_COUNT) {
            mSendAutoUpgradeTestCmdCount++;
            DeviceCmdService.getInstance(NooieApplication.mCtx).upgrade(deviceId, appVersionResult.getModel(), appVersionResult.getVersion_code(), appVersionResult.getKey(), new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> DeviceTestToolPresenter sendAutoUpgradeTestCmd deviceId=" + deviceId + " code=" + code);
                    DeviceService.getService().getDeviceUpdateStatus(deviceId)
                            .delay(8 * 1000, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<BaseResponse<DeviceUpdateStatusResult>>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                                    if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                                        if (code == Constant.OK) {
                                            if (mDeviceTestResult != null) {
                                                mDeviceTestResult.addSendUpgradeCmdSuccessCount();
                                            }
                                            if (mTaskView != null) {
                                                mTaskView.onStartAutoUpgradeTest(ConstantValue.SUCCESS, 1);
                                            }
                                        } else {
                                            if (!NooieDeviceHelper.isDeviceUpdating(response.getData().getType())) {
                                                sendAutoUpgradeTestCmd(deviceId, appVersionResult);
                                            } else {
                                            }
                                        }
                                    } else {
                                        if (code == Constant.OK) {
                                        } else {
                                            sendAutoUpgradeTestCmd(deviceId, appVersionResult);
                                        }
                                    }
                                }
                            });
                }
            });
        } else {
            if (mDeviceTestResult != null) {
                mDeviceTestResult.addSendUpgradeCmdFailCount();
            }
            if (mTaskView != null) {
                mTaskView.onStartAutoUpgradeTest(ConstantValue.SUCCESS, 2);
            }
        }
    }

    private Subscription mQueryDeviceUpgradeStatusTask = null;
    private int mLastUpgradeType = ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
    @Override
    public void startQueryDeviceUpgradeStatus(String deviceId) {
        stopQueryDeviceUpgradeStatus();
        mQueryDeviceUpgradeStatusTask = Observable.interval(0,6 * 1000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<BaseResponse<DeviceUpdateStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceUpdateStatusResult>> call(Long time) {
                        return DeviceService.getService().getDeviceUpdateStatus(deviceId);
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
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieLog.d("-->> DeviceTestToolPresenter startQueryDeviceUpgradeStatus onNext type=" + response.getData().getType());
                            if (mTaskView != null) {
                                mTaskView.onQueryDeviceUpgradeStatus(ConstantValue.SUCCESS, response.getData().getType());
                            }
                            if (mLastUpgradeType == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS && response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
                                if (mDeviceTestResult != null) {
                                    mDeviceTestResult.addUpgradeSuccessCount();
                                }
                            }
                            mLastUpgradeType = response.getData().getType();
                            if (response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED || response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS || response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
                                loadDeviceInfo(deviceId);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopQueryDeviceUpgradeStatus() {
        if (mQueryDeviceUpgradeStatusTask != null && !mQueryDeviceUpgradeStatusTask.isUnsubscribed()) {
            mQueryDeviceUpgradeStatusTask.unsubscribe();
        }
    }

    @Override
    public void loadDeviceInfo(String deviceId) {
        getDeviceDetail(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NooieDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(NooieDevice device) {
                        if (mTaskView != null) {
                            mTaskView.onLoadDeviceInfo(ConstantValue.SUCCESS, device);
                        }
                    }
                });
    }

    @Override
    public DeviceTestResult getDeviceTestResult() {
        return mDeviceTestResult;
    }
}
