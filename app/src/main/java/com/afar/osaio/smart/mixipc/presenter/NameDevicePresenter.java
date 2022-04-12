package com.afar.osaio.smart.mixipc.presenter;

import android.os.Bundle;

import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.mixipc.contract.NameDeviceContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.db.dao.BleApDeviceService;

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
public class NameDevicePresenter implements NameDeviceContract.Presenter {

    private NameDeviceContract.View mNameDeviceView;

    public NameDevicePresenter(NameDeviceContract.View view) {
        this.mNameDeviceView = view;
        this.mNameDeviceView.setPresenter(this);
    }

    @Override
    public void destroy() {
        mNameDeviceView = null;
    }

    @Override
    public void renameDevice(final String deviceId, final String name) {
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
                        if (mNameDeviceView != null) {
                            mNameDeviceView.notifyUpdateDeviceNameState(ConstantValue.SUCCESS);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (result) {
                            BleApDeviceInfoCache.getInstance().updateApDeviceName(deviceId, name);
                        }
                        if (mNameDeviceView != null) {
                            mNameDeviceView.notifyUpdateDeviceNameState(ConstantValue.SUCCESS);
                        }
                    }
                });
    }

}
