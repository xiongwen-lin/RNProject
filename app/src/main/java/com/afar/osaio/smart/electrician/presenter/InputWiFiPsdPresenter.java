package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.view.IInputWiFiPsdView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InputWiFiPsdPresenter implements IInputWiFiPsdPresenter {

    public IInputWiFiPsdView mView;

    public InputWiFiPsdPresenter(IInputWiFiPsdView view){
        this.mView = view;
    }

    @Override
    public void isConnectWifi() {
        Observable.just(NetworkUtil.isConnected(NooieApplication.mCtx))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.onIsWifiConnected(false);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean isConnectWifi) {
                        NooieLog.d("------->> InputWiFiPsdPresenter isConnectWifi "+isConnectWifi);
                        if (mView != null) {
                            mView.onIsWifiConnected(isConnectWifi);
                        }
                    }
                });
    }

    @Override
    public void getSSID() {
        Observable.just(NetworkUtil.getSSIDAuto(NooieApplication.mCtx))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.onGetSSID(ConstantValue.ERROR,"");
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String ssid) {
                        if (mView != null) {
                            mView.onGetSSID(ConstantValue.SUCCESS, ssid);
                        }
                    }
                });
    }

    @Override
    public void destroy() {

    }
}
