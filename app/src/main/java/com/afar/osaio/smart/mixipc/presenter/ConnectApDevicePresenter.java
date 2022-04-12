package com.afar.osaio.smart.mixipc.presenter;

import android.os.Bundle;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.mixipc.contract.ConnectApDeviceContract;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.bean.SDKConstant;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ConnectApDevicePresenter implements ConnectApDeviceContract.Presenter {

    private ConnectApDeviceContract.View mTaskView;

    public ConnectApDevicePresenter(ConnectApDeviceContract.View view) {
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
    public void checkConnectAp() {
        Observable.just("")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        String ssid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                        return Observable.just(ssid);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckConnectAp(ConstantValue.ERROR, "");
                        }
                    }

                    @Override
                    public void onNext(String ssid) {
                        if (mTaskView != null) {
                            mTaskView.onCheckConnectAp(ConstantValue.SUCCESS, ssid);
                        }
                    }
                });
    }

    @Override
    public void startBluetoothAPConnect(Bundle param) {
        ApHelper.getInstance().trySwitchApConnectMode(param, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                if (mTaskView != null) {
                    mTaskView.onStartBluetoothAPConnect((result ? SDKConstant.SUCCESS : SDKConstant.ERROR), param, deviceId);
                }
            }
        });
    }
}
