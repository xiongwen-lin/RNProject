package com.afar.osaio.smart.lpipc.presenter;

import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.afar.osaio.smart.cache.GatewayDeviceCache;
import com.nooie.sdk.db.dao.GatewayDeviceService;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.GatewayInfoContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.setting.SettingService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class GatewayInfoPresenter implements GatewayInfoContract.Presenter {

    private static final int RETRY_UNBIND_DEVICE_MAX_TIME = 3;
    private static final int QUERY_DEVICE_DELAY_TIME = 5 * 1000;
    private static final int QUERY_DEVICE_RETRY_MAX_COUNT = 3;

    private GatewayInfoContract.View mTaskView;
    private int mRetryUnbindDeviceTime = 1;
    private int mRetryQueryDeviceCount = 1;
    private Subscription mQueryDeviceTask = null;

    public GatewayInfoPresenter(GatewayInfoContract.View view) {
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

    /**
     * 获取设备信息 Get device information
     *
     * @param deviceId
     */
    @Override
    public void loadDeviceInfo(String account, final String deviceId) {
        getDeviceInfo(account, deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NooieDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyDeviceInfoResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(NooieDevice nooieDevice) {
                        if (mTaskView != null) {
                            mTaskView.notifyDeviceInfoResult(ConstantValue.SUCCESS, nooieDevice);
                        }
                    }
                });
    }

    private Observable<NooieDevice> getDeviceInfo(String account, String deviceId) {
        final NooieDevice device = new NooieDevice();
        return Observable.just("")
                .flatMap(new Func1<String, Observable<GatewayDevice>>() {
                    @Override
                    public Observable<GatewayDevice> call(String s) {
                        GatewayDevice gatewayDevice = GatewayDeviceCache.getInstance().getCacheById(deviceId);
                        if (gatewayDevice == null) {
                            gatewayDevice = GatewayDeviceService.getInstance().getGatewayDevice(account, deviceId);
                        }
                        return Observable.just(gatewayDevice);
                    }
                })
                .flatMap(new Func1<GatewayDevice, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(GatewayDevice gatewayDevice) {
                        if (gatewayDevice != null) {
                            device.setGatewayDevice(gatewayDevice);
                            return getAppVersionObservable(deviceId, gatewayDevice.getType());
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

    private Observable<BaseResponse<AppVersionResult>> getAppVersionObservable(String deviceId, String model) {
        Observable<BaseResponse<AppVersionResult>> hardVersionObservable = SettingService.getService().getHardVersion(model);
        Observable<BaseResponse<BindDevice>> deviceInfoObservable = DeviceService.getService().getDeviceInfo(deviceId);
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
    public void getGatewayInfo(String deviceId) {
        DeviceCmdApi.getInstance().getDeviceSetting(deviceId, new OnGetDeviceSetting() {
            @Override
            public void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting) {
                if (code == SDKConstant.CODE_CACHE) {
                    FormatInfo formatInfo = complexSetting != null && complexSetting.getHubInfo() != null ? complexSetting.getHubInfo().fmtInfo : null;
                    if (mTaskView != null && formatInfo != null) {
                        NooieLog.d("-->> GatewayInfoPresenter getGatewayInfo free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
                        double free = Math.floor((formatInfo.getFree() / 1024.0) * 10 + 0.5) / 10;
                        double total = Math.floor((formatInfo.getTotal() / 1024.0) * 10 + 0.5) / 10;
                        free = Math.max(0, free);
                        total = Math.max(free, total);
                        int status = NooieDeviceHelper.compateSdStatus(formatInfo.getFormatStatus());
                        double used = total - free < 0 ? 0 : total - free;
                        used = Math.floor(used * 10 + 0.5) / 10;
                        mTaskView.onQuerySDStatusSuccess(formatInfo, status, String.valueOf(used), String.valueOf(total), formatInfo.getProgress());
                    }
                    return;
                }
                if (code == Constant.OK && mTaskView != null && complexSetting != null && complexSetting.getHubInfo() != null) {
                    mTaskView.onGetGatewayInfoResult(ConstantValue.SUCCESS, complexSetting.getHubInfo());
                } else if (mTaskView != null) {
                    mTaskView.onGetGatewayInfoResult(ConstantValue.ERROR, null);
                }
            }
        });
    }

    @Override
    public void unbindDevice(String deviceId, String uid, String account) {
        NooieLog.d("-->> GatewayInfoPresenter removeDevice unbindDevice from server deviceId=" + deviceId + " account=" + account);
        DeviceService.getService().deleteGatewayDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            GatewayDeviceCache.getInstance().removeDeviceCacheWithDb(account, deviceId);
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteGatewayDeviceResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            DeviceCmdService.getInstance(NooieApplication.mCtx).hubUnbind(deviceId, uid, new OnActionResultListener() {
                                @Override
                                public void onResult(int code) {
                                    if (mTaskView != null) {
                                        mTaskView.onDeleteGatewayDeviceResult(ConstantValue.SUCCESS);
                                    }
                                    DeviceConnectionCache.getInstance().removeConnection(deviceId);
                                }
                            });
                        } else if (mTaskView != null) {
                            mTaskView.onDeleteGatewayDeviceResult(ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void unbindDevice(String deviceId, String uid, String account, boolean isOnline) {
        NooieLog.d("-->> GatewayInfoPresenter removeDevice unbindDevice by device deviceId=" + deviceId + " account=" + account + " isOnline=" + isOnline);
        DeviceCmdApi.getInstance().removeDevice(deviceId, "", isOnline, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug GatewayInfoPresenter unbindDevice onResult: code=" + code);
                if (code == Constant.OK && mTaskView != null) {
                    GatewayDeviceCache.getInstance().removeDeviceCache(deviceId);
                    mTaskView.onDeleteGatewayDeviceResult(ConstantValue.SUCCESS);
                } else if (mTaskView != null) {
                    mTaskView.onDeleteGatewayDeviceResult(ConstantValue.ERROR);
                }
            }
        });
    }

    public void unbindDeviceOffline(String deviceId, String uid, String account) {
        NooieLog.d("-->> GatewayInfoPresenter removeDevice unbindDevice from server");
        DeviceService.getService().deleteGatewayDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            GatewayDeviceCache.getInstance().removeDeviceCacheWithDb(account, deviceId);
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteGatewayDeviceResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            DeviceConnectionCache.getInstance().removeConnection(deviceId);
                            if (mTaskView != null) {
                                mTaskView.onDeleteGatewayDeviceResult(ConstantValue.SUCCESS);
                            }
                        } else if (mTaskView != null) {
                            mTaskView.onDeleteGatewayDeviceResult(ConstantValue.ERROR);
                        }
                    }
                });
    }

    public void startQueryDevice(String deviceId, String uid, String account, boolean isOnline) {
        NooieLog.d("-->> GatewayInfoPresenter removeDevice startQueryDevice deviceId=" + deviceId + " account=" + account + " isOnline=" + isOnline);
        mRetryQueryDeviceCount = 1;
        stopQueryDeviceTask();
        mQueryDeviceTask = DeviceService.getService().getDeviceInfo(deviceId)
                .delay(QUERY_DEVICE_DELAY_TIME, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<BindDevice>>>() {
                    @Override
                    public Observable<BaseResponse<BindDevice>> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code) {
                            GatewayDeviceCache.getInstance().removeDeviceCacheWithDb(account, deviceId);
                            return Observable.just(response);
                        }
                        mRetryQueryDeviceCount++;
                        return mRetryQueryDeviceCount <= QUERY_DEVICE_RETRY_MAX_COUNT ? Observable.error(new RetryQueryDeviceException()) : Observable.just(response);
                    }
                })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (throwable instanceof RetryQueryDeviceException) {
                                    NooieLog.d("-->> GatewayInfoPresenter removeDevice startQueryDevice retry");
                                    return Observable.just("");
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> GatewayInfoPresenter removeDevice onError");
                        unbindDevice(deviceId, uid, account, isOnline);
                    }

                    @Override
                    public void onNext(BaseResponse<BindDevice> response) {
                        NooieLog.d("-->> GatewayInfoPresenter removeDevice onNext");
                        if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code && mTaskView != null) {
                            NooieLog.d("-->> GatewayInfoPresenter removeDevice onNext success");
                            mTaskView.onDeleteGatewayDeviceResult(ConstantValue.SUCCESS);
                        } else {
                            boolean isDeviceOnline = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null ? true : isOnline;
                            NooieLog.d("-->> GatewayInfoPresenter removeDevice onNext fail isDeviceOnline=" + isDeviceOnline);
                            unbindDevice(deviceId, uid, account, isDeviceOnline);
                        }
                    }
                });
    }

    public void stopQueryDeviceTask() {
        if (mQueryDeviceTask != null && !mQueryDeviceTask.isUnsubscribed()) {
            mQueryDeviceTask.unsubscribe();
            mQueryDeviceTask = null;
        }
    }

    @Override
    public void restartDevice(String deviceId) {
        DeviceCmdApi.getInstance().reboot(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onRestartGatewayDeviceResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void clearDeviceUseSpace(String deviceId) {
        DeviceCmdApi.getInstance().formatSDCard(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onClearDeviceUserSpaceResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    private Subscription mQuerySDCardTask;
    @Override
    public void startQuerySDCardFormatState(final String deviceId) {
        stopQuerySDCardFormatState();
        mQuerySDCardTask = Observable.interval(3 * 1000, TimeUnit.MILLISECONDS)
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
                        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
                            @Override
                            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                                if (code == SDKConstant.CODE_CACHE) {
                                    return;
                                }
                                if (code == Constant.OK && formatInfo != null && mTaskView != null) {
                                    NooieLog.d("-->> GatewayInfoPresenter startQuerySDCardFormatState free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
                                    double free = Math.floor((formatInfo.getFree() / 1024.0) * 10 + 0.5) / 10;
                                    double total = Math.floor((formatInfo.getTotal() / 1024.0) * 10 + 0.5) / 10;
                                    free = Math.max(0, free);
                                    total = Math.max(free, total);
                                    int status = NooieDeviceHelper.compateSdStatus(formatInfo.getFormatStatus());
                                    double used = total - free < 0 ? 0 : total - free;
                                    used = Math.floor(used * 10 + 0.5) / 10;
                                    mTaskView.onQuerySDStatusSuccess(formatInfo, status, String.valueOf(used), String.valueOf(total), formatInfo.getProgress());
                                    if (status != ConstantValue.HUB_SD_STATUS_FORMATING) {
                                        stopQuerySDCardFormatState();
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void stopQuerySDCardFormatState() {
        if (mQuerySDCardTask != null && !mQuerySDCardTask.isUnsubscribed()) {
            mQuerySDCardTask.unsubscribe();
        }
    }

    @Override
    public void setSyncTime(String uuid, final int mode, float timeZone, int timeOffset) {
        DeviceCmdApi.getInstance().setSyncTime(uuid, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onSetSyncTimeResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void setLed(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setLed(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onSetLedResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void checkDeviceUpdateStatus(String deviceId) {
        DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceUpdateStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckDeviceUpdateStatus(ConstantValue.ERROR, ApiConstant.DEVICE_UPDATE_TYPE_UNKNOWN);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && mTaskView != null) {
                            mTaskView.onCheckDeviceUpdateStatus(ConstantValue.SUCCESS, response.getData().getType());
                        } else if (mTaskView != null) {
                            mTaskView.onCheckDeviceUpdateStatus(ConstantValue.ERROR, ApiConstant.DEVICE_UPDATE_TYPE_UNKNOWN);
                        }
                    }
                });
    }

    public class RetryQueryDeviceException extends Exception {
    }
}
