package com.afar.osaio.smart.scan.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.mixipc.contract.BluetoothScanContract;
import com.afar.osaio.smart.scan.contract.InputWiFiPsdContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.bean.SDKConstant;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class InputWiFiPsdPresenter implements InputWiFiPsdContract.Presenter {

    private InputWiFiPsdContract.View mTaskView;
    private Subscription mSendCmdListTask = null;
    private int mCurrentSendCmdListIndex = 0;

    public InputWiFiPsdPresenter(InputWiFiPsdContract.View view) {
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
    public void getSSID(int useType) {
        NooieLog.d("-->> InputWiFiPsdPresenter getSSID useType=" + useType);
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
                        NooieLog.d("-->> InputWiFiPsdPresenter getSSID onError useType=" + useType);
                        if (mTaskView != null) {
                            mTaskView.onGetSSID(ConstantValue.ERROR, useType, "");
                        }
                    }

                    @Override
                    public void onNext(String ssid) {
                        NooieLog.d("-->> InputWiFiPsdPresenter getSSID onNext useType=" + useType + " ssid=" + ssid);
                        if (mTaskView != null) {
                            mTaskView.onGetSSID(ConstantValue.SUCCESS, useType, ssid);
                        }
                    }
                });
    }

    @Override
    public void startSendCmdList(int cmdListSize, BluetoothScanContract.SendCmdListListener listener) {
        stopSendCmdList();
        mSendCmdListTask = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onSendCmd(SDKConstant.ERROR, cmdListSize, mCurrentSendCmdListIndex);
                        }
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mCurrentSendCmdListIndex < cmdListSize) {
                            if (listener != null) {
                                listener.onSendCmd(SDKConstant.SUCCESS, cmdListSize, mCurrentSendCmdListIndex);
                            }
                            mCurrentSendCmdListIndex++;
                            if (mCurrentSendCmdListIndex == cmdListSize) {
                                stopSendCmdList();
                            }
                        } else {
                            stopSendCmdList();
                            if (listener != null) {
                                listener.onSendCmd(SDKConstant.SUCCESS, cmdListSize, mCurrentSendCmdListIndex);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopSendCmdList() {
        mCurrentSendCmdListIndex = 0;
        if (mSendCmdListTask != null && !mSendCmdListTask.isUnsubscribed()) {
            mSendCmdListTask.unsubscribe();
            mSendCmdListTask = null;
        }
    }
}
