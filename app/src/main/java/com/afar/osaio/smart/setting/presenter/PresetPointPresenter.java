package com.afar.osaio.smart.setting.presenter;


import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.setting.contract.PresetPointContract;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.BitmapUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceConfig;
import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PresetPointPresenter implements PresetPointContract.Presenter {

    private PresetPointContract.View mTaskView;

    public PresetPointPresenter(PresetPointContract.View view) {
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
    public void startCompressScreenShot(String account, String deviceId, String thumbnailPath) {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(thumbnailPath)) {
                            return Observable.just(thumbnailPath);
                        }
                        File screenShotFile = new File(thumbnailPath);
                        if (screenShotFile.exists()) {
                            BitmapUtil.compressImage(thumbnailPath, FileUtil.getPresetPointThumbnailFolderPath(NooieApplication.mCtx, account), "", DisplayUtil.dpToPx(NooieApplication.mCtx, 96), DisplayUtil.dpToPx(NooieApplication.mCtx, 54));
                        }
                        return Observable.just(thumbnailPath);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCompressScreenShot(account, deviceId, thumbnailPath);
                        }
                    }

                    @Override
                    public void onNext(String value) {
                        if (mTaskView != null) {
                            mTaskView.onCompressScreenShot(account, deviceId, thumbnailPath);
                        }
                    }
                });
    }

    @Override
    public void getPresetPoints(String deviceId, boolean isSyncToDevice) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<DeviceConfig>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceConfig>> call(BaseResponse<BindDevice> response) {
                        BaseResponse<DeviceConfig> configResponse = new BaseResponse<>();
                        configResponse.setCode(response != null ? response.getCode() : StateCode.FAILED.code);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            configResponse.setData(deviceConfig);
                        }
                        return Observable.just(configResponse);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceConfig>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetPresetPoints(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceConfig> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onGetPresetPoints(SDKConstant.SUCCESS, (response.getData() != null ? response.getData().getPresetPointList() : null));
                        } else if (mTaskView != null) {
                            mTaskView.onGetPresetPoints(SDKConstant.ERROR, null);
                        }
                    }
                });

    }

    private Observable<BaseResponse<DeviceConfig>> getDeviceConfigure(String deviceId) {
        return DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<DeviceConfig>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceConfig>> call(BaseResponse<BindDevice> response) {
                        BaseResponse<DeviceConfig> configResponse = new BaseResponse<>();
                        configResponse.setCode(response != null ? response.getCode() : StateCode.FAILED.code);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            configResponse.setData(deviceConfig);
                        }
                        return Observable.just(configResponse);
                    }
                });
    }

    @Override
    public void checkAddPresetPointPosition(String deviceId, String name, int position) {
        getDeviceConfigure(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceConfig>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckAddPresetPointPosition(SDKConstant.ERROR, name, position, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceConfig> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onCheckAddPresetPointPosition(SDKConstant.SUCCESS, name, position, (response.getData() != null ? response.getData().getPresetPointList() : null));
                        } else if (mTaskView != null) {
                            mTaskView.onCheckAddPresetPointPosition(SDKConstant.ERROR, name, position, null);
                        }
                    }
                });
    }

    @Override
    public void addPresetPoint(String account, String deviceId, boolean isSetOnPower, String name, int position, String tempPresetPointPath, String presetPointPath) {
        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1000 deviceId=" + deviceId + " isSetOnPower" + isSetOnPower + " name" + name + " position" + position);
        if (TextUtils.isEmpty(deviceId)) {
            return;
        }

        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1001 deviceId=" + deviceId);
        DeviceCmdApi.getInstance().setPtzSavePos(deviceId, position, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1002 deviceId=" + deviceId);
                if (code == Constant.OK) {
                    if (isSetOnPower) {
                        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1003 deviceId" + deviceId);
                        DeviceCmdApi.getInstance().ptzSetPowerOnPos(deviceId, position, new OnActionResultListener() {
                            @Override
                            public void onResult(int codeOne) {
                                NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1004 deviceId" + deviceId);
                                dealAddPresetPoint(codeOne, account, deviceId, name, position, tempPresetPointPath, presetPointPath);
                            }
                        });
                    } else {
                        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1005 deviceId" + deviceId);
                        dealAddPresetPoint(code, account, deviceId, name, position, tempPresetPointPath, presetPointPath);
                    }
                } else {
                    NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 1006 deviceId" + deviceId);
                    dealAddPresetPoint(code, account, deviceId, name, position, tempPresetPointPath, presetPointPath);
                }
            }
        });
    }

    private void dealAddPresetPoint(int code, String account, String deviceId, String name, int position, String tempPresetPointPath, String presetPointPath) {
        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2001 deviceId" + deviceId);
        if (code == Constant.OK) {
            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2002 deviceId" + deviceId);
            getUpdateDeviceConfigureObservable(deviceId, name, position)
                    .flatMap(new Func1<BaseResponse, Observable<BaseResponse<DeviceConfig>>>() {
                        @Override
                        public Observable<BaseResponse<DeviceConfig>> call(BaseResponse response) {
                            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2003 deviceId" + deviceId);
                            if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                                NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2004 deviceId" + deviceId);
                                if (!TextUtils.isEmpty(tempPresetPointPath) && !TextUtils.isEmpty(presetPointPath) && new File(tempPresetPointPath).exists()) {
                                    BitmapUtil.compressImage(tempPresetPointPath, FileUtil.getPresetPointThumbnailFolderPath(NooieApplication.mCtx, account), "", DisplayUtil.dpToPx(NooieApplication.mCtx, 96), DisplayUtil.dpToPx(NooieApplication.mCtx, 54));
                                    FileUtil.renamePreviewThumb(tempPresetPointPath, presetPointPath);
                                }
                                return getDeviceConfigure(deviceId);
                            }
                            return Observable.just(null);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BaseResponse<DeviceConfig>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2005 deviceId" + deviceId);
                            if (mTaskView != null) {
                                mTaskView.onAddPresetPoint(SDKConstant.ERROR, null);
                            }
                        }

                        @Override
                        public void onNext(BaseResponse<DeviceConfig> response) {
                            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2006 deviceId" + deviceId);
                            if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                                NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2007 deviceId" + deviceId);
                                mTaskView.onAddPresetPoint(SDKConstant.SUCCESS, response.getData().getPresetPointList());
                            } else if (mTaskView != null) {
                                NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2008 deviceId" + deviceId);
                                mTaskView.onAddPresetPoint(SDKConstant.ERROR, null);
                            }
                        }
                    });
        } else if (mTaskView != null) {
            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 2009 deviceId" + deviceId);
            mTaskView.onAddPresetPoint(SDKConstant.ERROR, null);
        }
    }

    private Observable<BaseResponse> getUpdateDeviceConfigureObservable(String deviceId, String name, int position) {
        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 3001 deviceId" + deviceId);
        return getDeviceConfigure(deviceId)
                .flatMap(new Func1<BaseResponse<DeviceConfig>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<DeviceConfig> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 3002 deviceId" + deviceId);
                            DeviceConfig deviceConfig = response.getData() != null ? response.getData() : DeviceSettingHelper.createDeviceConfigForPresetPoint();
                            deviceConfig.setPresetPointList(DeviceSettingHelper.updatePresetPointConfigureList(DeviceSettingHelper.filterPresetPointConfigureList(deviceConfig.getPresetPointList()), name, position));
                            return DeviceService.getService().updateTimingConfig(deviceId, GsonHelper.convertToJson(deviceConfig));
                        }
                        NooieLog.d("-->> debug PresetPointPresenter addPresetPoint: 3003 deviceId" + deviceId);
                        return Observable.just(null);
                    }
                });
    }

    @Override
    public void sortPresetPointConfigureList(String deviceId, List<PresetPointConfigure> presetPointConfigures) {
        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1000 deviceId=" + deviceId);
        getDeviceConfigure(deviceId)
                .flatMap(new Func1<BaseResponse<DeviceConfig>, Observable<BaseResponse<List<PresetPointConfigure>>>>() {
                    @Override
                    public Observable<BaseResponse<List<PresetPointConfigure>>> call(BaseResponse<DeviceConfig> response) {
                        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1001 deviceId=" + deviceId);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1002 deviceId=" + deviceId);
                            DeviceConfig deviceConfig = response.getData() != null ? response.getData() : DeviceSettingHelper.createDeviceConfigForPresetPoint();
                            List<PresetPointConfigure> originalPresetPointConfigures = deviceConfig != null ? deviceConfig.getPresetPointList() : null;
                            deviceConfig.setPresetPointList(presetPointConfigures);
                            return DeviceService.getService().updateTimingConfig(deviceId, GsonHelper.convertToJson(deviceConfig))
                                    .flatMap(new Func1<BaseResponse, Observable<BaseResponse<List<PresetPointConfigure>>>>() {
                                        @Override
                                        public Observable<BaseResponse<List<PresetPointConfigure>>> call(BaseResponse response) {
                                            NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1003 deviceId=" + deviceId);
                                            BaseResponse<List<PresetPointConfigure>> updateConfigureResponse = new BaseResponse<>();
                                            updateConfigureResponse.setCode(response != null ? response.getCode() : StateCode.UNKNOWN.code);
                                            updateConfigureResponse.setData(originalPresetPointConfigures);
                                            return Observable.just(updateConfigureResponse);
                                        }
                                    });
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<PresetPointConfigure>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1004 deviceId=" + deviceId);
                        if (mTaskView != null) {
                            mTaskView.onSortPresetPointConfigureList(SDKConstant.ERROR, deviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<List<PresetPointConfigure>> response) {
                        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1005 deviceId=" + deviceId);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1006 deviceId=" + deviceId);
                            boolean isPowerOnPresetPointChange = DeviceSettingHelper.checkPowerOnPresetPointChange(presetPointConfigures, response.getData());
                            int powerOnPosition = CollectionUtil.isNotEmpty(presetPointConfigures) && presetPointConfigures.get(0) != null ? presetPointConfigures.get(0).getPosition() : DeviceSettingHelper.PRESET_POINT_DEFAULT_POSITION;
                            setPowerOnPresetPointAfterSort(deviceId, isPowerOnPresetPointChange, powerOnPosition);
                        } else if (mTaskView != null) {
                            NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 1007 deviceId=" + deviceId);
                            mTaskView.onSortPresetPointConfigureList(SDKConstant.ERROR, deviceId);
                        }
                    }
                });
    }

    private void setPowerOnPresetPointAfterSort(String deviceId, boolean isPowerOnPresetPointChange, int powerOnPosition) {
        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 2000 deviceId=" + deviceId);
        if (!isPowerOnPresetPointChange && mTaskView != null) {
            NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 2001 deviceId=" + deviceId);
            mTaskView.onSortPresetPointConfigureList(SDKConstant.SUCCESS, deviceId);
            return;
        }
        NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 2002 deviceId=" + deviceId);
        DeviceCmdApi.getInstance().ptzSetPowerOnPos(deviceId, powerOnPosition, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug PresetPointPresenter sortPresetPointConfigureList: 2003 deviceId=" + deviceId + " code=" + code);
                if (mTaskView != null) {
                    mTaskView.onSortPresetPointConfigureList(SDKConstant.SUCCESS, deviceId);
                }
            }
        });
    }

    @Override
    public void editPresetPointConfigure(String deviceId, PresetPointConfigure presetPointConfigure) {
        NooieLog.d("-->> debug PresetPointPresenter editPresetPointConfigure: 1000 deviceId=" + deviceId);
        if (TextUtils.isEmpty(deviceId) || presetPointConfigure == null || TextUtils.isEmpty(presetPointConfigure.getName()) || !DeviceSettingHelper.checkPresetPointValid(presetPointConfigure.getPosition())) {
            if (mTaskView != null) {
                mTaskView.onEditPresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
            }
            return;
        }
        NooieLog.d("-->> debug PresetPointPresenter editPresetPointConfigure: 1001");
        getUpdateDeviceConfigureObservable(deviceId, presetPointConfigure.getName(), presetPointConfigure.getPosition())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug PresetPointPresenter editPresetPointConfigure: 1002");
                        if (mTaskView != null){
                            mTaskView.onEditPresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            NooieLog.d("-->> debug PresetPointPresenter editPresetPointConfigure: 1003");
                            mTaskView.onEditPresetPointConfigure(SDKConstant.SUCCESS, deviceId, presetPointConfigure);
                        } else if (mTaskView != null){
                            NooieLog.d("-->> debug PresetPointPresenter editPresetPointConfigure: 1004");
                            mTaskView.onEditPresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
                        }
                    }
                });
    }

    @Override
    public void deletePresetPointConfigure(String account, String deviceId, PresetPointConfigure presetPointConfigure) {
        NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1000 deviceId=" + deviceId);
        if (TextUtils.isEmpty(deviceId) || presetPointConfigure == null) {
            if (mTaskView != null) {
                mTaskView.onEditPresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
            }
            return;
        }
        NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1001");
        getDeviceConfigure(deviceId)
                .flatMap(new Func1<BaseResponse<DeviceConfig>, Observable<BaseResponse<DeviceConfig>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceConfig>> call(BaseResponse<DeviceConfig> response) {
                        NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1002");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            DeviceConfig deviceConfig = response != null ? response.getData() : null;
                            if (deviceConfig == null || CollectionUtil.isEmpty(deviceConfig.getPresetPointList())) {
                                BaseResponse<DeviceConfig> configureResponse = new BaseResponse<>();
                                configureResponse.setData(deviceConfig);
                                configureResponse.setCode(StateCode.SUCCESS.code);
                                return Observable.just(configureResponse);
                            }
                            if (presetPointConfigure != null) {
                                deviceConfig.setPresetPointList(DeviceSettingHelper.sortPresetPointConfigureList(DeviceSettingHelper.deletePresetPointConfigure(presetPointConfigure.getPosition(), deviceConfig.getPresetPointList())));
                            }
                            NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1003");
                            return DeviceService.getService().updateTimingConfig(deviceId, GsonHelper.convertToJson(deviceConfig))
                                    .flatMap(new Func1<BaseResponse, Observable<BaseResponse<DeviceConfig>>>() {
                                        @Override
                                        public Observable<BaseResponse<DeviceConfig>> call(BaseResponse response) {
                                            NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1004");
                                            if (response != null && response.getCode() == StateCode.SUCCESS.code && presetPointConfigure != null) {
                                                FileUtil.deleteFile(FileUtil.getPresetPointThumbnail(NooieApplication.mCtx, account, deviceId, presetPointConfigure.getPosition()));
                                            }
                                            BaseResponse<DeviceConfig> updateConfigureResponse = new BaseResponse<>();
                                            updateConfigureResponse.setCode(response != null ? response.getCode() : StateCode.UNKNOWN.code);
                                            updateConfigureResponse.setData(deviceConfig);
                                            return Observable.just(updateConfigureResponse);
                                        }
                                    });
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceConfig>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1005");
                        if (mTaskView != null) {
                            mTaskView.onDeletePresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceConfig> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1006");
                            dealDeletePresetPointSuccess(deviceId, presetPointConfigure, response.getData());
                        } else if (mTaskView != null) {
                            NooieLog.d("-->> debug PresetPointPresenter deletePresetPointConfigure: 1007");
                            mTaskView.onDeletePresetPointConfigure(SDKConstant.ERROR, deviceId, presetPointConfigure);
                        }
                    }
                });
    }

    private void dealDeletePresetPointSuccess(String deviceId, PresetPointConfigure presetPointConfigure, DeviceConfig deviceConfig) {
        NooieLog.d("-->> debug PresetPointPresenter dealDeletePresetPointSuccess: 1000 deviceId=" + deviceId);
        if (TextUtils.isEmpty(deviceId) || presetPointConfigure == null) {
            if (mTaskView != null) {
                mTaskView.onDeletePresetPointConfigure(SDKConstant.SUCCESS, deviceId, presetPointConfigure);
            }
            return;
        }
        int powerOnPosition = -1;
        if (deviceConfig == null || CollectionUtil.isEmpty(deviceConfig.getPresetPointList())) {
            powerOnPosition = DeviceSettingHelper.PRESET_POINT_DEFAULT_POSITION;
        } else if (presetPointConfigure.getId() == DeviceSettingHelper.PRESET_POINT_ID_START_INDEX || CollectionUtil.size(deviceConfig.getPresetPointList()) == 1) {
            powerOnPosition = deviceConfig.getPresetPointList().get(0) != null ? deviceConfig.getPresetPointList().get(0).getPosition() : DeviceSettingHelper.PRESET_POINT_DEFAULT_POSITION;
        }
        if (powerOnPosition == -1) {
            if (mTaskView != null) {
                mTaskView.onDeletePresetPointConfigure(SDKConstant.SUCCESS, deviceId, presetPointConfigure);
            }
            return;
        }
        NooieLog.d("-->> debug PresetPointPresenter dealDeletePresetPointSuccess: 1000 deviceId=" + deviceId + " powerOnPosition=" + powerOnPosition);
        DeviceCmdApi.getInstance().ptzSetPowerOnPos(deviceId, powerOnPosition, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug PresetPointPresenter dealDeletePresetPointSuccess: 1000 deviceId=" + deviceId + " code=" + code);
                if (mTaskView != null) {
                    mTaskView.onDeletePresetPointConfigure(SDKConstant.SUCCESS, deviceId, presetPointConfigure);
                }
            }
        });
    }

    @Override
    public void turnPresetPoint(String deviceId, PresetPointConfigure presetPointConfigure) {
        NooieLog.d("-->> debug PresetPointPresenter turnPresetPoint: 1000 deviceId=" + deviceId);
        if (TextUtils.isEmpty(deviceId) || !DeviceSettingHelper.checkPresetPointValid(presetPointConfigure)) {
            if (mTaskView != null) {
                mTaskView.onTurnPresetPoint(SDKConstant.ERROR, deviceId);
            }
            return;
        }
        NooieLog.d("-->> debug PresetPointPresenter turnPresetPoint: 1001 deviceId=" + deviceId);
        DeviceCmdApi.getInstance().setPtzTurnPos(deviceId, presetPointConfigure.getPosition(), new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug PresetPointPresenter turnPresetPoint: 1002 deviceId=" + deviceId + " code=" + code);
                if (mTaskView != null) {
                    mTaskView.onTurnPresetPoint((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), deviceId);
                }
            }
        });
    }
}
