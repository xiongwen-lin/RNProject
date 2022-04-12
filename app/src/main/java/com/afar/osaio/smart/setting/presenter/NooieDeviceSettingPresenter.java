package com.afar.osaio.smart.setting.presenter;

import android.text.TextUtils;

import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.tool.RxUtil;
import com.nooie.sdk.api.network.base.bean.entity.DeviceConfig;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;
import com.nooie.sdk.listener.OnGetAllSettingsV2Listener;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;

import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.afar.osaio.smart.setting.view.INooieDeviceSettingView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.listener.OnGetPirStateV2Listener;
import com.nooie.sdk.listener.OnGetMediaModeListener;
import com.nooie.sdk.listener.OnGetSubCamInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;
import com.nooie.sdk.processor.device.DeviceApi;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/7/4
 * Email is victor.qiao.0604@gmail.com
 */
public class NooieDeviceSettingPresenter implements INooieDeviceSettingPresenter {

    private static final int RETRY_UNBIND_DEVICE_MAX_TIME = 3;
    private static final int QUERY_DEVICE_DELAY_TIME = 5 * 1000;
    private static final int QUERY_DEVICE_RETRY_MAX_COUNT = 3;

    private INooieDeviceSettingView mSettingView;
    private int mRetryUnbindDeviceTime = 1;
    private int mRetryQueryDeviceCount = 1;
    private Subscription mQueryDeviceTask = null;

    public NooieDeviceSettingPresenter(INooieDeviceSettingView mSettingView) {
        this.mSettingView = mSettingView;
    }

    @Override
    public void detachView() {
        if (mSettingView != null) {
            mSettingView = null;
        }
    }

    @Override
    public void getDeviceSetting(String deviceId, String model) {
        DeviceCmdApi.getInstance().getDeviceSetting(deviceId, model, new OnGetDeviceSetting() {
            @Override
            public void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting) {
                dealDeviceSetting(code, complexSetting);
            }
        });
    }

    private void dealDeviceSetting(int code, DeviceComplexSetting complexSetting) {
        if (mSettingView == null) {
            return;
        }
        mSettingView.onGetDeviceSetting(code, complexSetting);
    }

    @Override
    public void getAllSetting(String account, String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).getCamAllSettingsV2(deviceId, new OnGetAllSettingsV2Listener() {
            @Override
            public void onGetAllSettingsV2(int code, DevAllSettingsV2 settings) {
                if (code == Constant.OK && settings != null && mSettingView != null) {
                    //DeviceConfigureCache.getInstance().updateDeviceInfoInDb(account, deviceId, settings, null);
                    RxUtil.wrapperObservable("NooieDeviceSettingPresenter getAllSetting", DeviceApi.getInstance().updateConfigureSettings(true, account, deviceId, settings));
                    NooieLog.d("-->> NooieDeviceSettingPresenter onGetAllSettings settings=" + settings.toString());
                    mSettingView.onGetAllSettingResult(ConstantValue.SUCCESS, settings);
                } else if (mSettingView != null) {
                    mSettingView.onGetAllSettingResult(ConstantValue.ERROR, null);
                }
            }
        });
    }

    @Override
    public void getDevInfo(String deviceId) {
        DeviceCmdApi.getInstance().getDevInfo(deviceId, new OnGetDevInfoListener() {
            @Override
            public void onDevInfo(int code, DevInfo info) {
                if (mSettingView != null) {
                    mSettingView.onGetDeviceInfo(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, info);
                }
            }
        });
    }

    @Override
    public void getDevice(String deviceId, String account) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<BindDevice>>>() {
                    @Override
                    public Observable<BaseResponse<BindDevice>> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code) {
                            NooieLog.d("-->> NooieDeviceSettingPresenter getDevice device id invalid deviceId=" + deviceId + " account=" + account);
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
                            DeviceConnectionCache.getInstance().removeConnection(deviceId);
                        }
                        return Observable.just(response);
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
                        if (mSettingView != null) {
                            mSettingView.onNotifyGetDeviceFailed("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSettingView != null) {
                            mSettingView.onNotifyGetDeviceSuccess(response.getData());
                        } else if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code && mSettingView != null) {
                            mSettingView.notifyRemoveCameraResult(ConstantValue.SUCCESS);
                        } else if (mSettingView != null) {
                            mSettingView.onNotifyGetDeviceFailed("");
                        }
                    }
                });
    }

    @Override
    public void removeCamera(final String deviceId, final String uid, final String account, final boolean isOnline, final boolean isMyDevice, boolean isSubDevice, String pDeviceId) {
        DeviceService.getService().deleteDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code)) {
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
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
                        if (mSettingView != null) {
                            mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code) && mSettingView != null) {
                            boolean isRemoveDeviceSuccess = response != null && response.getCode() == StateCode.SUCCESS.code;
                            if (isSubDevice) {
                                unbindSubDevice(deviceId, pDeviceId, uid, isOnline, isMyDevice, isRemoveDeviceSuccess);
                            } else {
                                unbindDevice(deviceId, uid, isOnline, isMyDevice, isRemoveDeviceSuccess, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        if (isRemoveDeviceSuccess) {
                                            DeviceConnectionCache.getInstance().removeConnection(deviceId);
                                        }
                                    }
                                });
                            }
                            mSettingView.notifyRemoveCameraResult(ConstantValue.SUCCESS);
                        } else if (mSettingView != null) {
                            mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
                        }
                    }
                });
    }

    private void unbindDevice(String deviceId, String uid, boolean isOnline, boolean isMyDevice, boolean isRemoveDeviceSuccess, OnActionResultListener listener) {
        if (isRemoveDeviceSuccess && isOnline && isMyDevice && !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(uid)) {
            DeviceCmdService.getInstance(NooieApplication.mCtx).unbindDevice(deviceId, uid, listener);
        } else if (!TextUtils.isEmpty(deviceId)) {
            listener.onResult(Constant.OK);
        }
    }

    private void unbindSubDevice(String deviceId, String pDeviceId, String uid, boolean isOnline, boolean isMyDevice, boolean isRemoveDeviceSuccess) {
        if (isRemoveDeviceSuccess && isOnline && isMyDevice && !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(uid)) {
            NooieLog.d("-->> NooieDeviceSettingPresenter unbindSubDevice deviceId" + deviceId);
            DeviceCmdService.getInstance(NooieApplication.mCtx).camUnbind(deviceId, uid, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> NooieDeviceSettingPresenter {r deviceId=" + deviceId + " onResult code=" + code);
                    DeviceCmdService.getInstance(NooieApplication.mCtx).hubRemoveSubDev(deviceId, pDeviceId);
                }
            });
        } else if (isRemoveDeviceSuccess) {
            DeviceCmdService.getInstance(NooieApplication.mCtx).hubRemoveSubDev(deviceId, pDeviceId);
        }
    }

    @Override
    public void removeDevice(final String deviceId, final String uid, final String account, final boolean isOnline, final boolean isMyDevice, boolean isSubDevice, String pDeviceId) {
        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice deviceId=" + deviceId + " uid=" + uid + " account=" + account + " isOnline=" + isOnline + " isMyDevice=" + isMyDevice + " isSubDevice=" + isSubDevice + " pDeviceId=" + pDeviceId);
        /*
        if (isMyDevice && isOnline) {
            mRetryUnbindDeviceTime = 1;
            if (isSubDevice) {
                unbindSubDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
            } else {
                unbindDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
            }
            return;
        }

        removeDeviceForOffLineOrShared(deviceId, uid, account, isSubDevice, pDeviceId);
         */

        if (isMyDevice) {
            DeviceCmdApi.getInstance().removeDevice(deviceId, pDeviceId, isOnline, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    dealRemoveDeviceResult(code, deviceId);
                }
            });
        } else {
            DeviceCmdApi.getInstance().removeDeviceForOffLineOrShared(deviceId, isSubDevice, pDeviceId, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    dealRemoveDeviceResult(code, deviceId);
                }
            });
        }
    }

    private void dealRemoveDeviceResult(int code, String deviceId) {
        if (code == Constant.OK && mSettingView != null) {
            DeviceInfoCache.getInstance().removeCacheById(deviceId);
            DeviceListCache.getInstance().removeCacheById(deviceId);
            mSettingView.notifyRemoveCameraResult(ConstantValue.SUCCESS);
        } else if (mSettingView != null) {
            mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
        }
    }

    public void removeDeviceForOffLineOrShared(String deviceId, String uid, String account, boolean isSubDevice, String pDeviceId) {
        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice removeDeviceForOffLineOrShared deviceId=" + deviceId + " uid=" + uid + " account=" + account + " isSubDevice=" + isSubDevice + " pDeviceId=" + pDeviceId);
        DeviceService.getService().deleteDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code)) {
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
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
                        if (mSettingView != null) {
                            mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code)) {
                            if (isSubDevice) {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).hubRemoveSubDev(deviceId, pDeviceId);
                            } else {
                                DeviceConnectionCache.getInstance().removeConnection(deviceId);
                            }
                            if (mSettingView != null) {
                                mSettingView.notifyRemoveCameraResult(ConstantValue.SUCCESS);
                            }
                        } else if (mSettingView != null) {
                            mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
                        }
                    }
                });
    }

    public void unbindDevice(String deviceId, String uid, String account, boolean isOnline, boolean isSubDevice, String pDeviceId) {
        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice unbindDevice deviceId=" + deviceId + " uid=" + uid + " account=" + account + " isSubDevice=" + isSubDevice + " pDeviceId=" + pDeviceId);
        if (isOnline) {
            if (mRetryUnbindDeviceTime < RETRY_UNBIND_DEVICE_MAX_TIME) {
                mRetryUnbindDeviceTime++;
                DeviceCmdService.getInstance(NooieApplication.mCtx).unbindDevice(deviceId, uid, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice unbindDevice onResult code=" + code);
                        startQueryDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
                    }
                });
            } else if (mSettingView != null) {
                mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
            }
        } else {
            removeDeviceForOffLineOrShared(deviceId, uid, account, isSubDevice, pDeviceId);
        }
    }

    public void unbindSubDevice(String deviceId, String uid, String account, boolean isOnline, boolean isSubDevice, String pDeviceId) {
        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice unbindSubDevice deviceId=" + deviceId + " uid=" + uid + " account=" + account + " isSubDevice=" + isSubDevice + " pDeviceId=" + pDeviceId);
        if (isOnline) {
            if (mRetryUnbindDeviceTime < RETRY_UNBIND_DEVICE_MAX_TIME) {
                mRetryUnbindDeviceTime++;
                DeviceCmdService.getInstance(NooieApplication.mCtx).camUnbind(deviceId, uid, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice unbindSubDevice onResult code=" + code);
                        startQueryDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
                    }
                });
            } else if (mSettingView != null) {
                mSettingView.notifyRemoveCameraResult(NooieApplication.get().getString(R.string.network_error0));
            }
        } else {
            removeDeviceForOffLineOrShared(deviceId, uid, account, isSubDevice, pDeviceId);
        }
    }

    public void startQueryDevice(String deviceId, String uid, String account, boolean isOnline, boolean isSubDevice, String pDeviceId) {
        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice startQueryDevice deviceId=" + deviceId + " uid=" + uid + " account=" + account + " isSubDevice=" + isSubDevice + " pDeviceId=" + pDeviceId);
        mRetryQueryDeviceCount = 1;
        stopQueryDeviceTask();
        mQueryDeviceTask = DeviceService.getService().getDeviceInfo(deviceId)
                .delay(QUERY_DEVICE_DELAY_TIME, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<BindDevice>>>() {
                    @Override
                    public Observable<BaseResponse<BindDevice>> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code) {
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
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
                                    NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice startQueryDevice retry");
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
                        NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice startQueryDevice onError");
                        if (isSubDevice) {
                            unbindSubDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
                        } else {
                            unbindDevice(deviceId, uid, account, isOnline, isSubDevice, pDeviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.UUID_NOT_EXISTED.code && mSettingView != null) {
                            NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice startQueryDevice onNext success deviceId=" + deviceId + " account=" + account);
                            if (isSubDevice) {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).hubRemoveSubDev(deviceId, pDeviceId);
                            } else {
                                DeviceConnectionCache.getInstance().removeConnection(deviceId);
                            }
                            mSettingView.notifyRemoveCameraResult(ConstantValue.SUCCESS);
                        } else {
                            boolean isDeviceOnline = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null ? response.getData().getOnline() == ApiConstant.ONLINE_STATUS_ON : isOnline;
                            NooieLog.d("-->> NooieDeviceSettingPresenter removeDevice startQueryDevice onNext fail deviceId=" + deviceId + " account=" + account + " isOnline=" + isDeviceOnline);
                            if (isSubDevice) {
                                unbindSubDevice(deviceId, uid, account, isDeviceOnline, isSubDevice, pDeviceId);
                            } else {
                                unbindDevice(deviceId, uid, account, isDeviceOnline, isSubDevice, pDeviceId);
                            }
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
    public void getLedStatus(String deviceId) {
        DeviceCmdApi.getInstance().getLed(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mSettingView != null && code == Constant.OK) {
                    mSettingView.notifyGetLedSuccess(on);
                } else if (mSettingView != null) {
                    mSettingView.notifyGetLedFailed(NooieApplication.get().getString(R.string.network_error0));
                }
            }
        });
    }

    @Override
    public void setLedStatus(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setLed(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mSettingView != null) {
                    mSettingView.notifySetLedResult((code == Constant.OK) ? ConstantValue.SUCCESS : NooieApplication.get().getString(R.string.network_error0));
                }
            }
        });
    }

    @Override
    public void setFactoryReset(String account, String deviceId) {
        DeviceConfig deviceConfig = new DeviceConfig();
        DeviceService.getService().updateTimingConfig(deviceId, GsonHelper.convertToJson(deviceConfig))
                .flatMap(new Func1<BaseResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BaseResponse response) {
                        boolean result = response != null && response.getCode() == StateCode.SUCCESS.code;
                        if (result) {
                            FileUtil.deleteFile(FileUtil.getPresetPointThumbnailFolderPath(NooieApplication.mCtx, account));
                        }
                        return Observable.just(result);
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
                        if (mSettingView != null) {
                            mSettingView.notifyFactoryResetResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (!result && mSettingView != null) {
                            mSettingView.notifyFactoryResetResult(ConstantValue.ERROR);
                            return;
                        }
                        DeviceCmdApi.getInstance().resetDevice(deviceId, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mSettingView != null) {
                                    mSettingView.notifyFactoryResetResult((code == Constant.OK) ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void setFactoryResetForAp(String deviceId, String model) {
        DeviceCmdApi.getInstance().resetDevice(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (code == Constant.OK) {
                    ApHelper.getInstance().tryResetApConnectMode(model, new ApHelper.APDirectListener() {
                        @Override
                        public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                            if (mSettingView != null) {
                                mSettingView.notifyFactoryResetResult(ConstantValue.SUCCESS);
                            }
                        }
                    });
                } else if (mSettingView != null) {
                    mSettingView.notifyFactoryResetResult(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void setSyncTime(String uuid, final int mode, float timeZone, int timeOffset) {
        DeviceCmdApi.getInstance().setSyncTime(uuid, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mSettingView != null) {
                    mSettingView.notifySetSyncTimeResult((code == Constant.OK) ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void getDeviceStorageState(String deviceId, int bindType) {
        PackService.getService().getPackInfo(deviceId, bindType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<PackInfoResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSettingView != null) {
                            mSettingView.notifyLoadPackInfoResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<PackInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSettingView != null) {
                            mSettingView.notifyLoadPackInfoResult(ConstantValue.SUCCESS, response.getData());
                        } else if (mSettingView != null) {
                            mSettingView.notifyLoadPackInfoResult(ConstantValue.ERROR, null);
                        }
                    }
                });
    }

    @Override
    public void getCamInfo(String account, String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).camGetInfo(deviceId, new OnGetSubCamInfoListener() {
            @Override
            public void onSubCamInfo(int result, CameraInfo info) {
                if (mSettingView != null) {
                    //DeviceConfigureCache.getInstance().updateDeviceInfoInDb(account, deviceId, (result == Constant.OK ? info : null), null);
                    RxUtil.wrapperObservable("NooieDeviceSettingPresenter getCamInfo", DeviceApi.getInstance().updateConfigureCameraInfo(true, account, deviceId, info));
                    mSettingView.onGetCamInfoResult(result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, info);
                }
            }
        });
    }

    @Override
    public void setDeviceAiMode(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setPirAi(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mSettingView != null) {
                    mSettingView.onSetDeviceFDModeResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void updateDeviceNotice(String deviceId, int isNotice) {
        DeviceService.getService().updateDeviceNotice(deviceId, isNotice)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSettingView != null) {
                            mSettingView.onUpdateDeviceNoticeResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mSettingView != null) {
                            mSettingView.onUpdateDeviceNoticeResult((response != null && response.getCode() == StateCode.SUCCESS.code) ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void getPirState(String deviceId) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int code, PirStateV2 state) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mSettingView != null) {
                    mSettingView.onGetPirState(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, state);
                }
            }
        });
    }

    @Override
    public void setSiren(String deviceId, boolean on) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int code, PirStateV2 state) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK && state != null) {
                    state.siren = on;
                    setPirState(deviceId, state);
                } else if (mSettingView != null) {
                    mSettingView.onSetSiren(SDKConstant.ERROR);
                }
            }
        });
    }

    private void setPirState(String deviceId, PirStateV2 state) {
        DeviceCmdApi.getInstance().setPir(deviceId, state, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mSettingView != null) {
                    mSettingView.onSetSiren(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR);
                }
            }
        });
    }

    @Override
    public void getFileSettingMode(String deviceId) {
        DeviceCmdApi.getInstance().getMediaMode(deviceId, new OnGetMediaModeListener() {
            @Override
            public void onResult(int code, NooieMediaMode nooieMediaMode) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK && mSettingView != null) {
                    mSettingView.onGetFileSettingMode(SDKConstant.SUCCESS, nooieMediaMode);
                } else if (mSettingView != null) {
                    mSettingView.onGetFileSettingMode(SDKConstant.ERROR, null);
                }
            }
        });
    }

    @Override
    public void removeApDevice(String user, String uid, String deviceId, String model) {
        ApHelper.getInstance().removeBleApDeviceConnection(user, uid, deviceId, model, true, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                if (mSettingView != null) {
                    mSettingView.onRemoveApDevice(SDKConstant.SUCCESS);
                }
            }
        });
    }

    public class RetryQueryDeviceException extends Exception {
    }
}
