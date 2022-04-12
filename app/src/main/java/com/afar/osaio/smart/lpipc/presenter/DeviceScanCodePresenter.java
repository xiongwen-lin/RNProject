package com.afar.osaio.smart.lpipc.presenter;

import com.afar.osaio.smart.lpipc.contract.DeviceScanCodeContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.device.DeviceService;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DeviceScanCodePresenter implements DeviceScanCodeContract.Presenter {

    private DeviceScanCodeContract.View mTaskView;

    public DeviceScanCodePresenter(DeviceScanCodeContract.View view) {
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
    public void bindGatewayDevice(String uuid) {
        DeviceService.getService().bindGatewayDevice(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.ERROR, uuid, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.SUCCESS, uuid, response.getCode());
                        } else if (mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.ERROR, uuid, response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }

    private static final int DELAY_FOR_DEVICE_ONLINE_TIME_LEN = 10 * 1000;
    @Override
    public void bindDevice(String uuid) {
        DeviceService.getService().bindGatewayDevice(uuid)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        return response != null && response.getCode() == StateCode.SUCCESS.code ? Observable.just(response).delay(DELAY_FOR_DEVICE_ONLINE_TIME_LEN, TimeUnit.MILLISECONDS) : Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.ERROR, uuid, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.SUCCESS, uuid, response.getCode());
                        } else if (mTaskView != null) {
                            mTaskView.notifyBindGatewayResult(ConstantValue.ERROR, uuid, response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }
}
