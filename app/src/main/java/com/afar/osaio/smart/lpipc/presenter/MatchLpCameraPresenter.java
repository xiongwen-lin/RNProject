package com.afar.osaio.smart.lpipc.presenter;

import android.text.TextUtils;

import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.lpipc.contract.MatchLpCameraContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MatchLpCameraPresenter implements MatchLpCameraContract.Presenter {

    private static final int MAX_QUERY_BIND_STATUS_TIME = 4;
    public final static int SCAN_LIMIT_TIME = 25;
    public final static int SCAN_PER_TIME_LEN = 5;

    private MatchLpCameraContract.View mTaskView;
    private Subscription mQueryDeviceBindStatusTask;
    private long mQueryDeviceBindStatusTaskTime = 0;
    private Subscription mCountDownSubscription;

    public MatchLpCameraPresenter(MatchLpCameraContract.View view) {
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
    public void queryDeviceBindStatus() {
        stopQueryDeviceBindStatusTask();
        mQueryDeviceBindStatusTask = Observable.just(0L)
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.delay(SCAN_PER_TIME_LEN, TimeUnit.SECONDS);
                    }
                })
                .flatMap(new Func1<Long, Observable<BaseResponse<DeviceBindStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceBindStatusResult>> call(Long time) {
                        return DeviceService.getService().getDeviceBindStatus();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceBindStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            if (mTaskView != null) {
                                DeviceBindStatusResult bindStatusResult = new DeviceBindStatusResult();
                                bindStatusResult.setType(-1);
                                //mTaskView.onQueryDeviceBindStatus(ConstantValue.ERROR, bindStatusResult);
                            }
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceBindStatusResult> response) {
                        NooieLog.d("-->> MatchLpCameraPresenter queryDeviceBindStatus scan onNext code=" + response.getCode());
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && mTaskView != null) {
                            NooieLog.d("-->> MatchLpCameraPresenter queryDeviceBindStatus scan onNext code=" + response.getCode() + " type=" + response.getData().getType() + " msg=" + response.getData().getMsg());
                            int type = response.getData().getType();
                            if (type == 0) {
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, response.getData());
                            } else if (type == 1) {
                                stopQueryDeviceBindStatusTask();
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, response.getData());
                            } else if (type == 2) {
                                stopQueryDeviceBindStatusTask();
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, response.getData());
                            } else if (type == 3) {
                                //stopQueryDeviceBindStatusTask();
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, response.getData());
                            } else {
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, response.getData());
                            }
                        } else if (mTaskView != null) {
                            if (mTaskView != null) {
                                DeviceBindStatusResult bindStatusResult = new DeviceBindStatusResult();
                                bindStatusResult.setType(-1);
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.ERROR, bindStatusResult);
                            }
                        }
                    }
                });


    }

    public void stopQueryDeviceBindStatusTask() {
        if (mQueryDeviceBindStatusTask != null && !mQueryDeviceBindStatusTask.isUnsubscribed()) {
            mQueryDeviceBindStatusTask.unsubscribe();
        }

        stopCountDown();
    }

    @Override
    public void startCountDown() {
        stopCountDown();
        mCountDownSubscription = Observable.interval(0, SCAN_PER_TIME_LEN, TimeUnit.SECONDS)
                .takeUntil(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long time) {
                        NooieLog.d("-->> MatchLpCameraPresenter queryDeviceBindStatus startCountDown time=" + time);
                        return time >= MAX_QUERY_BIND_STATUS_TIME;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopQueryDeviceBindStatusTask();
                        if (mTaskView != null) {
                            DeviceBindStatusResult bindStatusResult = new DeviceBindStatusResult();
                            bindStatusResult.setType(100);
                            mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, bindStatusResult);
                        }
                    }

                    @Override
                    public void onNext(Long time) {
                        NooieLog.d("-->> MatchLpCameraPresenter queryDeviceBindStatus startCountDown onNext time=" + time);
                        if (time >= MAX_QUERY_BIND_STATUS_TIME) {
                            stopQueryDeviceBindStatusTask();
                            if (mTaskView != null) {
                                DeviceBindStatusResult bindStatusResult = new DeviceBindStatusResult();
                                bindStatusResult.setType(101);
                                mTaskView.onQueryDeviceBindStatus(ConstantValue.SUCCESS, bindStatusResult);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopCountDown() {
        if (mCountDownSubscription != null && !mCountDownSubscription.isUnsubscribed()) {
            mCountDownSubscription.unsubscribe();
        }
    }

    @Override
    public void getRecentBindDevice() {
        DeviceService.getService().getRecentBindDevices()
                .flatMap(new Func1<BaseResponse<List<BindDevice>>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BaseResponse<List<BindDevice>> response) {
                        boolean result = false;
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                            List<String> allDeviceIds = DeviceInfoCache.getInstance().getAllDeviceIds();
                            for (BindDevice device : CollectionUtil.safeFor(response.getData())) {
                                if (device != null && !allDeviceIds.contains(device.getUuid()) && !TextUtils.isEmpty(device.getPuuid())) {
                                    result = true;
                                    break;
                                }
                            }
                        }
                        return Observable.just(result);
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
                        if (mTaskView != null) {
                            mTaskView.onGetBindDeviceSuccess(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTaskView != null) {
                            mTaskView.onGetBindDeviceSuccess(ConstantValue.SUCCESS, result);
                        }
                    }
                });
    }
}
