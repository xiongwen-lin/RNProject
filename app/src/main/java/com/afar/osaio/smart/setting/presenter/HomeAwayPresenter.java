package com.afar.osaio.smart.setting.presenter;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.DeviceStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.afar.osaio.smart.setting.view.HomeAwayContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeAwayPresenter implements HomeAwayContract.Presenter {

    HomeAwayContract.View mTaskView;

    public HomeAwayPresenter(HomeAwayContract.View view) {
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
    public void updateDeviceOpenStatus(final String deviceId, final int status) {
        DeviceService.getService().getDeviceStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyUpdateDeviceOpenStatusResult(ConstantValue.ERROR, deviceId, 0);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        boolean isNeedSetSleep = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getOpen_status() != status;
                        if (isNeedSetSleep) {
                            DeviceCmdApi.getInstance().setSleep(deviceId, (ApiConstant.OPEN_STATUS_ON == status ? false : true), new OnActionResultListener() {
                                @Override
                                public void onResult(int code) {
                                    if (code == Constant.OK && mTaskView != null) {
                                        mTaskView.notifyUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, status);
                                    } else if (mTaskView != null) {
                                        mTaskView.notifyUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, 0);
                                    }
                                }
                            });
                        } else if (mTaskView != null) {
                            mTaskView.notifyUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, status);
                        }
                    }
                });
    }

    @Override
    public void updateApDeviceOpenStatus(String deviceId, String deviceSsid, boolean sleep) {
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK) {
                    if (sleep == on) {
                        if (mTaskView != null) {
                            mTaskView.onUpdateDeviceOpenStatus(ConstantValue.SUCCESS, deviceId, deviceSsid, sleep);
                        }
                    } else {
                        DeviceCmdApi.getInstance().setSleep(deviceId, sleep, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTaskView != null) {
                                    mTaskView.onUpdateDeviceOpenStatus(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, deviceId, deviceSsid, sleep);
                                }
                            }
                        });
                    }
                } else if (mTaskView != null) {
                    mTaskView.onUpdateDeviceOpenStatus(ConstantValue.ERROR, deviceId, deviceSsid, sleep);
                    getAPDeviceOpenStatus(deviceId);
                }
            }
        });
    }

    @Override
    public void getDeviceOpenStatus(final String deviceId) {
        DeviceService.getService().getDeviceStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyGetDeviceOpenStatusFailed(deviceId, e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.notifyGetDeviceOpenStatusSuccess(deviceId, response.getData());
                        } else if (response != null && mTaskView != null){
                            mTaskView.notifyGetDeviceOpenStatusFailed(deviceId, response.getMsg());
                        }
                    }
                });
    }

    @Override
    public void getAPDeviceOpenStatus(String deviceId) {
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetApDeviceOpenStatus(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, on ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON);
                }
            }
        });
    }
}
