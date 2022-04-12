package com.afar.osaio.smart.lpipc.presenter;

import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.smart.lpipc.contract.GatewayFirmwareContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GatewayFirmwarePresenter implements GatewayFirmwareContract.Presenter {

    private GatewayFirmwareContract.View mTaskView;

    public GatewayFirmwarePresenter(GatewayFirmwareContract.View view) {
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

    private static final int MAX_PROCESS = 240;
    @Override
    public void checkDeviceUpdateStatus(String deviceId, String account) {
        DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .flatMap(new Func1<BaseResponse<DeviceUpdateStatusResult>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(BaseResponse<DeviceUpdateStatusResult> response) {
                        int type = ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            type = response.getData().getType();
                            if (type == ApiConstant.DEVICE_UPDATE_TYPE_NORMAL) {
                                DeviceConfigureEntity configureEntity = DeviceConfigureService.getInstance().getDeviceConfigure(deviceId, account);
                                long upgradeTime = configureEntity != null ? configureEntity.getUpgradeTime() : 0;
                                int timeLen = (int)((DateTimeUtil.getUtcCalendar().getTimeInMillis() - upgradeTime) / 1000L);
                                type = timeLen < (MAX_PROCESS - 15) ? ApiConstant.DEVICE_UPDATE_TYPE_INSTALL_START : ApiConstant.DEVICE_UPDATE_TYPE_NORMAL;
                            }
                        }
                        return Observable.just(type);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckDeviceUpdateStatusResult(ConstantValue.ERROR, ApiConstant.DEVICE_UPDATE_TYPE_UNKNOWN);
                        }
                    }

                    @Override
                    public void onNext(Integer type) {
                        if (mTaskView != null) {
                            mTaskView.onCheckDeviceUpdateStatusResult(ConstantValue.SUCCESS, type);
                        }
                    }
                });
    }

    @Override
    public void startUpdateDevice(String account, String deviceId, String model, String version, String pkt, String md5) {
        NooieLog.d("-->> debug GatewayFirmwarePresenter startUpdateDevice: account=" + account + " deviceId=" + deviceId + " model=" + model + " version=" + version + " pkt=" + pkt + " md5=" + md5);
        DeviceCmdApi.getInstance().upgrade(deviceId, model, version, pkt, md5, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> GatewayFirmwarePresenter startUpdateDevice {r onResult code=" + code);
                if (code == Constant.OK) {
                    updateDeviceUpgradeTime(account, deviceId);
                } else if (mTaskView != null) {
                    mTaskView.onStartUpdateDeviceResult(ConstantValue.ERROR);
                }
            }
        });
    }

    private void updateDeviceUpgradeTime(String account, String deviceId) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        DeviceConfigureService.getInstance().updateUpgreadeTime(deviceId, account, DateTimeUtil.getUtcCalendar().getTimeInMillis());
                        return Observable.just(s);
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
                            mTaskView.onStartUpdateDeviceResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        if (mTaskView != null) {
                            mTaskView.onStartUpdateDeviceResult(ConstantValue.SUCCESS);
                        }
                    }
                });
    }
}
