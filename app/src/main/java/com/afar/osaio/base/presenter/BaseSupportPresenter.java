package com.afar.osaio.base.presenter;

import android.os.Bundle;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.contract.BaseSupportContract;
import com.nooie.sdk.bean.SDKConstant;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.detector.NetworkDetector;
import com.nooie.common.detector.NetworkTrackerConfig;
import com.nooie.common.detector.TrackerRouterInfo;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.processor.device.DeviceApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BaseSupportPresenter implements BaseSupportContract.Presenter {

    private BaseSupportContract.View mTaskView;

    public BaseSupportPresenter(BaseSupportContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void updateShareMsgState(int msgId, int shareId, int status) {
        DeviceService.getService().feedbackShare(msgId, shareId, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onUpdateShareMsgState(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTaskView != null) {
                            mTaskView.onUpdateShareMsgState(response != null && response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void changeDeviceUpgradeState(final String user, final String deviceId, final int platform, final int upgradeState) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        //DeviceCacheService.getInstance().updateUpgradeState(user, deviceId, platform, upgradeState);
                        DeviceApi.getInstance().updateDeviceUpgradeStatus(false, user, deviceId, platform, upgradeState);
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
                    }
                });
    }

    @Override
    public void startNetworkDetector() {
        NetworkTrackerConfig trackerConfig = NetworkDetector.getInstance().getTrackerConfig();
        trackerConfig.setDelayTL(120L);
        trackerConfig.setDetectCount(0);
        trackerConfig.setPingCount(2);
        List<String> addresses = new ArrayList<>();
        addresses.add("www.baidu.com");
        addresses.add("www.google.com");
        trackerConfig.setAddresses(addresses);
        NetworkDetector.getInstance().setOnNetworkDetectionListener(new NetworkDetector.OnNetworkDetectionListener() {
            @Override
            public void onNetworkDetectInfo(TrackerRouterInfo trackerRouterInfo) {
                Bundle data = trackerRouterInfo != null ? trackerRouterInfo.convert() : null;
                NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_DETECTED, data);
            }
        });
        NetworkDetector.getInstance().startNetworkDetectingTask(trackerConfig);
    }

    @Override
    public void stopNetworkDetector() {
        NetworkDetector.getInstance().setOnNetworkDetectionListener(null);
        NetworkDetector.getInstance().stopNetworkDetectingTask();
    }
}
