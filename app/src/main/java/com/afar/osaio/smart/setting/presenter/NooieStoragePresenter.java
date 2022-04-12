package com.afar.osaio.smart.setting.presenter;

import android.text.TextUtils;

import com.afar.osaio.smart.player.component.DeviceCmdComponent;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.nooie.common.bean.DataEffect;
import com.nooie.common.bean.DataEffectCache;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.view.INooieStorageView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.DeviceOfOrderResult;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.device.DeviceApi;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * NooieStoragePresenter
 *
 * @author Administrator
 * @date 2019/4/18
 */
public class NooieStoragePresenter implements INooieStoragePresenter {

    private INooieStorageView mStorageView;

    private static final String KEY_DE_CLOUD = "KEY_DE_CLOUD";
    private static final String KEY_DE_SD = "KEY_DE_SD";
    DataEffectCache mDataEffectCache = new DataEffectCache();
    private boolean mIsShowCloudAndSdState = false;
    private Subscription mQuerySDCardTask;
    private DeviceCmdComponent mDeviceCmdComponent = null;

    public NooieStoragePresenter(INooieStorageView mStorageView) {
        this.mStorageView = mStorageView;
        setupDataEffect();
        mDeviceCmdComponent = new DeviceCmdComponent();
    }

    @Override
    public void destroy() {
        this.mStorageView = null;
    }

    @Override
    public void loadSDCardInfo(String user, final String deviceId, boolean isShortLinkDevice) {
        mDeviceCmdComponent.getFormatInfo(deviceId, !isShortLinkDevice, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                dealGetFormatInfo(code, formatInfo);
            }
        });
    }

    private void dealGetFormatInfo(int code, FormatInfo formatInfo) {
        if (mStorageView == null) {
            return;
        }
        if ((code == SDKConstant.CODE_CACHE || code == Constant.OK)) {
            if (formatInfo == null) {
                mStorageView.notifyQuerySDStatusSuccess(ConstantValue.NOOIE_SD_STATUS_NO_SD, null, null, 0);
                return;
            }
            NooieLog.d("-->> NooieStoragePresenter dealGetFormatInfo free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
            double free = Math.floor((formatInfo.getFree() / 1024.0) * 10 + 0.5) / 10;
            double total = Math.floor((formatInfo.getTotal() / 1024.0) * 10 + 0.5) / 10;
            free = Math.max(0, free);
            total = Math.max(free, total);
            //double used = total - free < 0 ? 0 : total - free;
            //used = Math.floor(used * 10 + 0.5) / 10;
            int status = NooieDeviceHelper.compateSdStatus(formatInfo.getFormatStatus());
            mStorageView.notifyQuerySDStatusSuccess(status, String.valueOf(free), String.valueOf(total), formatInfo.getProgress());
            updateSdDe(formatInfo);
            showCouldAndSdState();
        } else {
            mStorageView.notifyQuerySDStatusFailed(NooieApplication.get().getString(R.string.camera_settings_warn_msg_get_sd_fail));
        }
    }

    @Override
    public void getLoopRecordStatus(String deviceId) {
        if (mStorageView != null) {
            mStorageView.showLoadingDialog();
        }
        DeviceCmdApi.getInstance().getLoopRecord(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (mStorageView != null && (code == SDKConstant.CODE_CACHE || code == Constant.OK)) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifyGetLoopRecordingSuccess(on);
                } else if (mStorageView != null) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifyGetLoopRecordingFailed("");
                }
            }
        });
    }

    @Override
    public void setLoopRecordStatus(String deviceId, boolean open) {
        if (mStorageView != null) {
            mStorageView.showLoadingDialog();
        }
        DeviceCmdApi.getInstance().setLoopRecord(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mStorageView != null && code == Constant.OK) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifySetLoopRecordingResult(ConstantValue.SUCCESS);
                } else if (mStorageView != null) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifySetLoopRecordingResult("");
                }
            }
        });
    }

    @Override
    public void startQuerySDCardFormatState(final String deviceId) {
        stopQuerySDCardFormatState();
        mQuerySDCardTask = Observable.interval(0, 5 * 1000, TimeUnit.MILLISECONDS)
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
                                dealQueryFormatInfo(code, formatInfo);
                            }
                        });
                    }
                });
    }

    private void dealQueryFormatInfo(int code, FormatInfo formatInfo) {
        if (mStorageView == null) {
            return;
        }
        if (code == Constant.OK && formatInfo != null) {
            NooieLog.d("-->> NooieStoragePresenter dealGetFormatInfo free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
            double free = Math.floor((formatInfo.getFree() / 1024.0) * 10 + 0.5) / 10;
            double total = Math.floor((formatInfo.getTotal() / 1024.0) * 10 + 0.5) / 10;
            free = Math.max(0, free);
            total = Math.max(free, total);
            //double used = total - free < 0 ? 0 : total - free;
            //used = Math.floor(used * 10 + 0.5) / 10;
            int status = NooieDeviceHelper.compateSdStatus(formatInfo.getFormatStatus());
            mStorageView.notifyQuerySDStatusSuccess(status, String.valueOf(free), String.valueOf(total), formatInfo.getProgress());
            if (status != ConstantValue.NOOIE_SD_STATUS_FORMATING) {
                stopQuerySDCardFormatState();
            }
        }
    }

    @Override
    public void stopQuerySDCardFormatState() {
        if (mQuerySDCardTask != null && !mQuerySDCardTask.isUnsubscribed()) {
            mQuerySDCardTask.unsubscribe();
        }
    }

    @Override
    public void formatSDCard(String deviceId) {
        if (mStorageView != null) {
            mStorageView.showLoadingDialog();
        }
        DeviceCmdApi.getInstance().formatSDCard(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mStorageView != null && code == Constant.OK) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifyFormatCardResult(ConstantValue.SUCCESS);
                } else if (mStorageView != null) {
                    mStorageView.hideLoadingDialog();
                    mStorageView.notifyFormatCardResult(NooieApplication.get().getResources().getString(R.string.network_error0));
                }
            }
        });
    }

    /**
     * 获取设备的云服务状态 check cloud states of the device list;
     *
     * @param deviceId
     */
    @Override
    public void getCloudState(String user, String deviceId, int bindType) {
        if (TextUtils.isEmpty(deviceId)) {
            //mStorageView.notifyGetCloudInfoFailed(NooieApplication.mCtx.getString(R.string.unknown));
            return;
        }

        getCloudStateFromConfigure(deviceId);
        PackService.getService().getPackInfo(deviceId, bindType)
                .flatMap(new Func1<BaseResponse<PackInfoResult>, Observable<BaseResponse<PackInfoResult>>>() {
                    @Override
                    public Observable<BaseResponse<PackInfoResult>> call(BaseResponse<PackInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, user, deviceId, response.getData());
                        } else if (response != null && response.getCode() == StateCode.DEVICE_UNSUBSCRIBE_CLOUD.code) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, user, deviceId, NooieCloudHelper.createPackInfoResult());
                            response.setData(NooieCloudHelper.createPackInfoResult());
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<PackInfoResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mStorageView != null) {
                            mStorageView.notifyGetCloudInfoFailed(NooieApplication.mCtx.getString(R.string.network_error0));
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<PackInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mStorageView != null) {
                            updateCloudDe(response.getData());
                            mStorageView.notifyGetCloudInfoSuccess(response);
                        } else if (mStorageView != null) {
                            if (response != null && response.getCode() == StateCode.DEVICE_UNSUBSCRIBE_CLOUD.code) {
                                updateCloudDe(response.getData());
                            }
                            mStorageView.notifyGetCloudInfoFailed(response != null ? response.getMsg() : "");
                        }
                        showCouldAndSdState();
                    }
                });
    }

    private void getCloudStateFromConfigure(String deviceId) {
        DeviceConfigureEntity configureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(deviceId);
        if (configureEntity != null && mStorageView != null) {
            BaseResponse<PackInfoResult> response = new BaseResponse<>();
            PackInfoResult packInfoResult = new PackInfoResult();
            packInfoResult.setEnd_time(configureEntity.getEndTime());
            packInfoResult.setTotal_time(configureEntity.getTotalTime());
            packInfoResult.setStatus(configureEntity.getStatus());
            mStorageView.notifyGetCloudInfoSuccess(response);
        }
    }

    @Override
    public void unsubscribePack(String account, String deviceId) {
        PackService.getService().cancelOrder(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            DeviceApi.getInstance().updateConfigureStatus(false, account, deviceId, ApiConstant.CLOUD_STATE_UNSUBSCRIBE);
                            return Observable.just(response).delay(5, TimeUnit.SECONDS);
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
                        if (mStorageView != null) {
                            mStorageView.notifyUnsubscribePackResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mStorageView != null) {
                            mStorageView.notifyUnsubscribePackResult(ConstantValue.SUCCESS);
                        } else if (mStorageView != null) {
                            mStorageView.notifyUnsubscribePackResult("");
                        }
                    }
                });
    }

    @Override
    public void getDeviceOfOrder(String deviceId) {
        PackService.getService().getDeviceOfOrder(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<DeviceOfOrderResult>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mStorageView != null) {
                            mStorageView.onLoadDeviceOfOrder(ConstantValue.ERROR, deviceId, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<List<DeviceOfOrderResult>> response) {
                        NooieLog.d("-->> NooieStoragePresenter unsubscribePack call device of order");
                        if (mStorageView != null) {
                            mStorageView.onLoadDeviceOfOrder(ConstantValue.SUCCESS, deviceId, (response != null ? response.getData() : null));
                        }
                    }
                });
    }

    private void setupDataEffect() {
        DataEffect<PackInfoResult> cloudDataEf = new DataEffect<>();
        cloudDataEf.setKey(KEY_DE_CLOUD);
        cloudDataEf.setEffective(false);
        DataEffect<FormatInfo> sdDataEf = new DataEffect<>();
        sdDataEf.setKey(KEY_DE_SD);
        sdDataEf.setEffective(false);
        mDataEffectCache.put(KEY_DE_CLOUD, cloudDataEf);
        mDataEffectCache.put(KEY_DE_SD, sdDataEf);
    }

    private void updateCloudDe(PackInfoResult value) {
        DataEffect<PackInfoResult> cloudDataEf = new DataEffect<>();
        cloudDataEf.setKey(KEY_DE_CLOUD);
        cloudDataEf.setValue(value);
        cloudDataEf.setEffective(true);
        mDataEffectCache.put(KEY_DE_CLOUD, cloudDataEf);
    }

    private void updateSdDe(FormatInfo value) {
        DataEffect<FormatInfo> sdDataEf = new DataEffect<>();
        sdDataEf.setKey(KEY_DE_CLOUD);
        sdDataEf.setValue(value);
        sdDataEf.setEffective(true);
        mDataEffectCache.put(KEY_DE_SD, sdDataEf);
    }
    private void showCouldAndSdState() {
        if (!mIsShowCloudAndSdState && mDataEffectCache.isDataEffective(KEY_DE_CLOUD) && mDataEffectCache.isDataEffective(KEY_DE_SD) && mStorageView != null) {
            mIsShowCloudAndSdState = true;
            mStorageView.notifyNoStorage();
        }
    }
}
