package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.smart.setting.contract.FileSettingContract;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.listener.OnGetMediaModeListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class FileSettingPresenter implements FileSettingContract.Presenter {

    private FileSettingContract.View mTaskView;

    public FileSettingPresenter(FileSettingContract.View view) {
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
}
