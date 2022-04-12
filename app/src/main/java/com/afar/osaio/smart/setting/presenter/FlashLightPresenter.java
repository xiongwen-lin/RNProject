package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.smart.setting.contract.FlashLightContract;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetPirStateV2Listener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class FlashLightPresenter implements FlashLightContract.Presenter {

    private FlashLightContract.View mTaskView;

    public FlashLightPresenter(FlashLightContract.View view) {
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
    public void getPirState(String deviceId) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int code, PirStateV2 state) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetPirState(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, state);
                }
            }
        });
    }

    @Override
    public void setFlashLightMode(String deviceId, int mode) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int code, PirStateV2 pirState) {
                if (code == Constant.OK && pirState != null) {
                    pirState.lightMode = mode;
                    setPirState(deviceId, pirState, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            dealSetFlashLightMode(code);
                        }
                    });
                } else {
                    dealSetFlashLightMode(Constant.OK);
                }
            }
        });
    }

    private void setPirState(String deviceId, PirStateV2 pirState, OnActionResultListener listener) {
        if (pirState == null) {
            if (listener != null) {
                listener.onResult(Constant.ERROR);
            }
        }
        DeviceCmdApi.getInstance().setPir(deviceId, pirState, listener);
    }

    private void dealSetFlashLightMode(int code) {
        if (mTaskView != null) {
            mTaskView.onSetFlashLightMode(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR);
        }
    }
}
