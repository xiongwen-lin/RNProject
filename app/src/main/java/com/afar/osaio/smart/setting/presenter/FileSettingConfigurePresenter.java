package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.bean.FileSettingConfigureParam;
import com.afar.osaio.smart.setting.contract.FileSettingConfigureContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetMediaModeListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class FileSettingConfigurePresenter implements FileSettingConfigureContract.Presenter {

    private FileSettingConfigureContract.View mTaskView;

    public FileSettingConfigurePresenter(FileSettingConfigureContract.View view) {
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
    public void getFileSettingMode(String deviceId) {
        DeviceCmdApi.getInstance().getMediaMode(deviceId, new OnGetMediaModeListener() {
            @Override
            public void onResult(int code, NooieMediaMode nooieMediaMode) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK && mTaskView != null) {
                    mTaskView.onGetFileSettingMode(SDKConstant.SUCCESS, nooieMediaMode);
                } else if (mTaskView != null) {
                    mTaskView.onGetFileSettingMode(SDKConstant.ERROR, null);
                }
            }
        });
    }

    @Override
    public void setFileSettingConfigure(String deviceId, FileSettingConfigureParam configure) {
        DeviceCmdApi.getInstance().getMediaMode(deviceId, new OnGetMediaModeListener() {
            @Override
            public void onResult(int code, NooieMediaMode nooieMediaMode) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK && nooieMediaMode != null && configure != null) {
                    if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
                        nooieMediaMode.mode = configure.getMode();
                    } else if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
                        nooieMediaMode.picNum = configure.getSnapNumber();
                    } else if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
                        nooieMediaMode.vidDur = configure.getRecordingTime();
                    }
                    DeviceCmdApi.getInstance().setMediaMode(deviceId, nooieMediaMode, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            if (code == Constant.OK && mTaskView != null) {
                                mTaskView.onSetFileSettingMode(SDKConstant.SUCCESS, nooieMediaMode);
                            } else if (mTaskView != null){
                                mTaskView.onSetFileSettingMode(SDKConstant.ERROR, null);
                            }
                        }
                    });
                } else if (mTaskView != null){
                    mTaskView.onSetFileSettingMode(SDKConstant.ERROR, null);
                }
            }
        });

    }
}
