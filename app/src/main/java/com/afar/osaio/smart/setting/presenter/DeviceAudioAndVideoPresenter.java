package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.view.DeviceAudioAndVideoContract;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.ICRMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;
import com.nooie.sdk.device.listener.OnICRModeListener;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.listener.OnGetAllSettingsV2Listener;
import com.nooie.sdk.listener.OnGetSubCamInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;

public class DeviceAudioAndVideoPresenter implements DeviceAudioAndVideoContract.Presenter {

    private DeviceAudioAndVideoContract.View mTasksView;

    public DeviceAudioAndVideoPresenter(DeviceAudioAndVideoContract.View tasksView) {
        this.mTasksView = tasksView;
        this.mTasksView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
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
        if (mTasksView == null) {
            return;
        }
        mTasksView.onGetDeviceSetting(code, complexSetting);
    }

    @Override
    public void getAllSetting(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).getCamAllSettingsV2(deviceId, new OnGetAllSettingsV2Listener() {
            @Override
            public void onGetAllSettingsV2(int code, DevAllSettingsV2 settings) {
                if (code == Constant.OK && settings != null && mTasksView != null) {
                    mTasksView.onGetAllSettingResult(ConstantValue.SUCCESS, settings);
                } else if (mTasksView != null) {
                    mTasksView.onGetAllSettingResult(ConstantValue.ERROR, null);
                }
            }
        });
    }

    @Override
    public void getRecordAudioStatus(String deviceId) {
        DeviceCmdApi.getInstance().getRecordAudio(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTasksView != null && code == Constant.OK) {
                    mTasksView.notifyGetRecordWidthAudioSuccess(on);
                } else if (mTasksView != null) {
                    mTasksView.notifyGetRecordWidthAudioFailed("");
                }
            }
        });
    }

    @Override
    public void setRecordWithAudioStatus(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setRecordAudio(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTasksView != null && code == Constant.OK) {
                    mTasksView.notifySetRecordWidthAudioResult(ConstantValue.SUCCESS);
                } else if (mTasksView != null) {
                    mTasksView.notifySetRecordWidthAudioResult("");
                }
            }
        });
    }

    @Override
    public void getRotateImage(String deviceId, boolean isSubDevice) {
        DeviceCmdApi.getInstance().getRotate(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTasksView != null && code == Constant.OK) {
                    mTasksView.notifyGetRotateImageSuccess(on);
                } else if (mTasksView != null) {
                    mTasksView.notifyGetRotateImageFailed("");
                }
            }
        });
    }

    @Override
    public void setRotateImage(String deviceId, boolean open, boolean isSubDevice) {
        DeviceCmdApi.getInstance().setRotate(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTasksView != null && code == Constant.OK) {
                    mTasksView.notifySetRotateImageResult(ConstantValue.SUCCESS);
                } else if (mTasksView != null) {
                    mTasksView.notifySetRotateImageResult("");
                }
            }
        });
    }

    @Override
    public void getNightVision(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).getIcr(deviceId, new OnICRModeListener() {
            @Override
            public void onIcr(int code, ICRMode mode) {
                if (code == Constant.OK && mTasksView != null) {
                    mTasksView.notifyGetNightVisionResult(ConstantValue.SUCCESS, mode);
                } else if (mTasksView != null) {
                    mTasksView.notifyGetNightVisionResult(NooieApplication.get().getString(R.string.network_error0), ICRMode.ICR_MODE_DAY);
                }
            }
        });
    }

    @Override
    public void setNightVision(final String deviceId, String model, int mode, boolean isSubDevice) {
        if (NooieDeviceHelper.isSupportNightVisionLightCmd(model, "")) {
            DeviceCmdApi.getInstance().setLightIcr(deviceId, mode, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    if (code == Constant.OK && mTasksView != null) {
                        mTasksView.notifySetNightVisionResult(ConstantValue.SUCCESS);
                    } else if (mTasksView != null) {
                        mTasksView.notifySetNightVisionResult(NooieApplication.get().getString(R.string.network_error0));
                    }
                }
            });
        } else {
            DeviceCmdApi.getInstance().setIcr(deviceId, model, NooieDeviceHelper.convertIRModeByMode(mode), new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    if (code == Constant.OK && mTasksView != null) {
                        mTasksView.notifySetNightVisionResult(ConstantValue.SUCCESS);
                    } else if (mTasksView != null) {
                        mTasksView.notifySetNightVisionResult(NooieApplication.get().getString(R.string.network_error0));
                    }
                }
            });
        }
    }

    @Override
    public void getCamInfo(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).camGetInfo(deviceId, new OnGetSubCamInfoListener() {
            @Override
            public void onSubCamInfo(int result, CameraInfo info) {
                if (mTasksView != null) {
                    mTasksView.onGetCamInfoResult(result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, info);
                }
            }
        });
    }

    @Override
    public void setMotionTrackingStatus(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setMotionTrack(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTasksView != null) {
                    mTasksView.onSetMotionTrackingResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void setEnergyMode(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setInfraredSavePower(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTasksView != null) {
                    mTasksView.onSetEnergyMode((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR));
                }
            }
        });
    }

    @Override
    public void getEnergyMode(String deviceId) {
        DeviceCmdApi.getInstance().getInfraredSavePower(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean open) {
                if (mTasksView != null) {
                    mTasksView.onGetEnergyMode((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), open);
                }
            }
        });
    }

    @Override
    public void setWaterMark(String deviceId, boolean open) {
        DeviceCmdApi.getInstance().setWaterMark(deviceId, open, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTasksView != null) {
                    mTasksView.onSetWaterMark((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR));
                }
            }
        });
    }

    @Override
    public void getWaterMark(String deviceId) {
        DeviceCmdApi.getInstance().getWaterMark(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean open) {
                if (mTasksView != null) {
                    mTasksView.onGetWaterMark((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), open);
                }
            }
        });
    }

}
