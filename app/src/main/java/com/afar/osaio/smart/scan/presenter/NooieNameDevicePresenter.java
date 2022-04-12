package com.afar.osaio.smart.scan.presenter;

import android.os.Bundle;

import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.view.INooieNameDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.db.dao.BleApDeviceService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * NooieNameDevicePresenter
 *
 * @author Administrator
 * @date 2019/4/22
 */
public class NooieNameDevicePresenter implements INooieNameDevicePresenter {

    private INooieNameDeviceView mNameDeviceView;

    public NooieNameDevicePresenter(INooieNameDeviceView view) {
        this.mNameDeviceView = view;
    }

    @Override
    public void destroy() {
        mNameDeviceView = null;
    }

    @Override
    public void renameDevice(int connectionMode, String user, final String deviceId, final String name) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            Observable.just(1)
                    .flatMap(new Func1<Integer, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Integer integer) {
                            Bundle data = new Bundle();
                            data.putString(BleApDeviceService.KEY_DEVICE_ID, deviceId);
                            data.putString(BleApDeviceService.KEY_NAME, name);
                            BleApDeviceService.getInstance().updateDevice(user, deviceId, data);
                            return Observable.just(true);
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
                        }

                        @Override
                        public void onNext(Boolean result) {
                            if (result) {
                                BleApDeviceInfoCache.getInstance().updateApDeviceName(deviceId, name);
                            }
                            if (mNameDeviceView != null) {
                                mNameDeviceView.notifyUpdateDeviceNameState(result ? ConstantValue.SUCCESS : "");
                            }
                        }
                    });
            return;
        }
        DeviceService.getService().updateDeviceName(deviceId, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mNameDeviceView != null) {
                            mNameDeviceView.notifyUpdateDeviceNameState("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mNameDeviceView != null) {
                            NooieDeviceHelper.updateDeviceName(deviceId, name);
                            mNameDeviceView.notifyUpdateDeviceNameState(ConstantValue.SUCCESS);
                        } else {
                            mNameDeviceView.notifyUpdateDeviceNameState("");
                        }
                    }
                });
    }

    @Override
    public void updateDeviceName(String user, final String deviceId, final String name) {
        DeviceService.getService().updateDeviceName(deviceId, name)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse<BindDevice>>>() {
                    @Override
                    public Observable<BaseResponse<BindDevice>> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            NooieDeviceHelper.updateDeviceName(deviceId, name);
                        }
                        return DeviceService.getService().getDeviceInfo(deviceId);
                    }
                })
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<BindDevice>>>() {
                    @Override
                    public Observable<BaseResponse<BindDevice>> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            List<BindDevice> bindDevices = new ArrayList<>();
                            bindDevices.add(response.getData());
                            DeviceListCache.getInstance().addDevices(NooieDeviceHelper.convertNooieDevice(bindDevices));
                        }
                        return Observable.just((BaseResponse)response);
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
                        if (mNameDeviceView != null) {
                            mNameDeviceView.notifyUpdateDeviceNameState("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mNameDeviceView != null) {
                            BindDevice device = response != null && response.getData() != null ? response.getData() : NooieDeviceHelper.getDeviceById(deviceId);
                            List<BindDevice> devices = new ArrayList<>();
                            devices.add(device);
                            NooieDeviceHelper.tryConnectionToDevice(user, devices, true);
                            mNameDeviceView.notifyUpdateDeviceNameState(ConstantValue.SUCCESS);
                        } else {
                            mNameDeviceView.notifyUpdateDeviceNameState(response != null ? response.getMsg() : "");
                        }
                    }
                });
    }

}
