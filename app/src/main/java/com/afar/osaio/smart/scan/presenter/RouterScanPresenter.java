package com.afar.osaio.smart.scan.presenter;

import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.bean.NooieScanDeviceCache;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.smart.scan.view.IRouterScanView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.encrypt.MD5Util;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.bean.APNetCfg;
import com.nooie.sdk.device.bean.APPairStatus;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RouterScanPresenter implements IRouterScanPresenter {

    public final static int SCAN_LIMIT_TIME = 120;
    public final static int SCAN_PER_TIME_LEN = 5 * 1000;
    private static final String COUNT_DOWN_END_TAG = "end";

    private IRouterScanView mNooieScanView;

    private Subscription mScanSubscription;
    private Subscription mCountDownSubscription;
    private boolean isWaitForOnline = true;

    public RouterScanPresenter(IRouterScanView view) {
        mNooieScanView = view;
    }

    @Override
    public void startScanDevice() {
        //stopScanDevice();
        //startCountDown();
        NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1000");
        mScanSubscription = Observable.just(0L)
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1001");
                        return observable.delay(SCAN_PER_TIME_LEN, TimeUnit.MILLISECONDS);
                    }
                })
                .flatMap(new Func1<Long, Observable<BaseResponse<DeviceBindStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceBindStatusResult>> call(Long time) {
                        NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1002");
                        return DeviceService.getService().getDeviceBindStatus()
                                .onErrorReturn(new Func1<Throwable, BaseResponse<DeviceBindStatusResult>>() {
                                    @Override
                                    public BaseResponse<DeviceBindStatusResult> call(Throwable throwable) {
                                        return null;
                                    }
                                });
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
                        NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1003 e=" + (e != null ? e.toString() : ""));
                        if (mNooieScanView != null) {
                            mNooieScanView.onScanDeviceFailed("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceBindStatusResult> response) {
                        NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1004 code=" + (response != null ? response.getCode() : ""));
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && mNooieScanView != null) {
                            NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1005" + response.getCode() + " type=" + response.getData().getType() + " msg=" + response.getData().getMsg());
                            switch (response.getData().getType()) {
                                case 0: {
                                    break;
                                }
                                case 1: {
                                    NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1006");
                                    DeviceCmdApi.getInstance().refreshDeviceCmdParam();
                                    mNooieScanView.onScanDeviceSuccess();
                                    stopScanDevice();
                                    break;
                                }
                                case 2: {
                                    NooieLog.d("-->> debug NooieScanPresenter startScanDevice: 1007");
                                    //stopScanDevice();
                                    if (response.getData() != null && response.getData().getData() != null) {
                                        BindDevice device = new BindDevice();
                                        device.setUuid(MD5Util.MD5Hash(Long.toString(System.currentTimeMillis())));
                                        device.setType(response.getData().getData().getProduct_model());
                                        device.setAccount(response.getData().getData().getAccount());
                                        device.setOnline(ApiConstant.ONLINE_STATUS_ON);
                                        device.setBind_type(ApiConstant.BIND_TYPE_SHARE);
                                        List<BindDevice> devices = new ArrayList<>();
                                        devices.add(device);
                                        NooieScanDeviceCache.getInstance().updateSearchDevList(devices);
                                    }
                                    mNooieScanView.onScanDeviceByOther(response != null ? response.getData() : null);
                                    break;
                                }
                                case 3: {
                                    mNooieScanView.onScanDeviceFailed("");
                                    break;
                                }
                            }
                        } else if (mNooieScanView != null) {
                            mNooieScanView.onScanDeviceFailed("");
                        }
                    }
                });
    }

    @Override
    public void stopScanDevice() {
        NooieLog.d("-->> debug NooieScanPresenter stopScanDevice: 1");
        if (mScanSubscription != null && !mScanSubscription.isUnsubscribed()) {
            mScanSubscription.unsubscribe();
            mScanSubscription = null;
        }
        stopScanApDevice();
    }

    @Override
    public void startCountDown() {
        NooieLog.d("-->> debug NooieScanPresenter startCountDown: 1000");
        stopCountDown();
        mCountDownSubscription = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Long time) {
                        if (time > SCAN_LIMIT_TIME) {
                            return Observable.error(new Throwable(COUNT_DOWN_END_TAG));
                        }
                        return Observable.just(time);
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
                        NooieLog.d("-->> debug NooieScanPresenter startCountDown: 1002");
                        if (e == null) {
                            return;
                        }
                        NooieLog.d("-->> debug NooieScanPresenter startCountDown: 1003 msg=" + e.getMessage());
                        if (COUNT_DOWN_END_TAG.equalsIgnoreCase(e.getMessage())) {
                            //stopScanDevice();
                            if (mNooieScanView != null) {
                                mNooieScanView.onTimerFinish();
                            }
                        }
                    }

                    @Override
                    public void onNext(Long time) {
                        NooieLog.d("-->> debug NooieScanPresenter startCountDown: 1004 time=" + time);
                        if (mNooieScanView != null) {
                            //mNooieScanView.onUpdateTimer(SCAN_LIMIT_TIME - time.intValue() < 0 ? 0 : SCAN_LIMIT_TIME - time.intValue());
                            // 更新设备时间
                            mNooieScanView.onUpdateTimer(getUpgradeProcess(time < 0 ? 0 : time.intValue()));
                        }
                    }
                });
    }

    @Override
    public void stopCountDown() {
        NooieLog.d("-->> debug NooieScanPresenter stopCountDown: ");
        if (mCountDownSubscription != null && !mCountDownSubscription.isUnsubscribed()) {
            mCountDownSubscription.unsubscribe();
            mCountDownSubscription = null;
        }
    }

    private Subscription mQueryRecenBindDeviceTask = null;
    @Override
    public void loadRecentBindDevice(String user, final boolean isScanSuccess) {
        NooieLog.d("-->> debug NooieScanPresenter loadRecentBindDevice: ");
        stopQueryRecentBindDeviceTask();
        mQueryRecenBindDeviceTask = DeviceService.getService().getRecentBindDevices()
                .flatMap(new Func1<BaseResponse<List<BindDevice>>, Observable<BaseResponse<List<BindDevice>>>>() {
                    @Override
                    public Observable<BaseResponse<List<BindDevice>>> call(BaseResponse<List<BindDevice>> response) {
                        NooieLog.d("-->> NooieScanPresenter query recent bind device code=" + (response != null ? response.getCode() : StateCode.UNKNOWN.code));
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                            List<String> allDeviceIds = DeviceInfoCache.getInstance().getAllDeviceIds();
                            for (BindDevice device : CollectionUtil.safeFor(response.getData())) {
                                if (!allDeviceIds.contains(device.getUuid()) || (allDeviceIds.contains(device.getUuid()) && device.getBind_type() == ApiConstant.BIND_TYPE_SHARE)) {
                                    NooieLog.d("-->> NooieScanPresenter query recent bind device deviceId=" + device.getUuid() + " onLine1=" + device.getOnline());
                                    if (device.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                                        break;
                                    } else {
                                        return Observable.error(new RepeatGetRecentBindDeviceException());
                                    }
                                }
                            }
                            return Observable.just(response);
                        }
                        return Observable.error(new RepeatGetRecentBindDeviceException());
                    }
                })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (throwable instanceof RepeatGetRecentBindDeviceException) {
                                    NooieLog.d("-->> NooieScanPresenter recent device onLine retry");
                                    return Observable.just("").delay(3, TimeUnit.SECONDS);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<BindDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mNooieScanView != null) {
                            mNooieScanView.onLoadRecentBindDeviceFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<List<BindDevice>> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mNooieScanView != null) {
                            List<BindDevice> bindDevices = new ArrayList<>();
                            List<String> allDeviceIds = DeviceInfoCache.getInstance().getAllDeviceIds();
                            List<BindDevice> existDevices = new ArrayList<>();
                            for (BindDevice device : CollectionUtil.safeFor(response.getData())) {
                                if (!allDeviceIds.contains(device.getUuid()) || (allDeviceIds.contains(device.getUuid()) && device.getBind_type() == ApiConstant.BIND_TYPE_SHARE)) {
                                    NooieLog.d("-->> NooieScanPresenter query recent bind device deviceId=" + device.getUuid() + " onLine2=" + device.getOnline());
                                    device.setBind_type(ApiConstant.BIND_TYPE_OWNER);
                                    bindDevices.add(device);
                                } else if (allDeviceIds.contains(device.getUuid()) && device.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                                    existDevices.add(device);
                                }
                            }
                            NooieDeviceHelper.tryConnectionToDevice(user, existDevices, true);
                            DeviceListCache.getInstance().addDevices(NooieDeviceHelper.convertNooieDevice(bindDevices));
                            NooieScanDeviceCache.getInstance().updateSearchDevList(bindDevices);
                            mNooieScanView.onLoadRecentBindDeviceSuccess(isScanSuccess);
                        } else if (mNooieScanView != null) {
                            mNooieScanView.onLoadRecentBindDeviceSuccess(isScanSuccess);
                        }
                    }
                });
    }

    @Override
    public void stopQueryRecentBindDeviceTask() {
        NooieLog.d("-->> debug NooieScanPresenter stopQueryRecentBindDeviceTask: ");
        if (mQueryRecenBindDeviceTask != null && !mQueryRecenBindDeviceTask.isUnsubscribed()) {
            mQueryRecenBindDeviceTask.unsubscribe();
            mQueryRecenBindDeviceTask = null;
        }
    }

    @Override
    public void startScanApDevice(APNetCfg apNetCfg) {
        NooieLog.d("-->> debug NooieScanPresenter startScanApDevice: 1");
        ApHelper.getInstance().tryStartAPPair(apNetCfg, new ApHelper.ApPairListener() {
            @Override
            public void onAPPairResult(int state, int code) {
                NooieLog.d("-->> NooieScanPresenter onAPPairResult state=" + state + " code=" + code);
                if (state == ApHelper.AP_PAIR_STATE_SUCCESS) {
                    startQueryAPPairStatusTask();
                }
            }
        });
    }

    @Override
    public void stopScanApDevice() {
        NooieLog.d("-->> debug NooieScanPresenter stopScanApDevice: ");
        ApHelper.getInstance().stopAPPair();
        stopQueryAPPairStatusTask();
    }

    public void startQueryAPPairStatusTask() {
        NooieLog.d("-->> debug NooieScanPresenter startQueryAPPairStatusTask: 1");
        ApHelper.getInstance().startQueryAPPairStatusTask(new ApHelper.QueryApPairStatusListener() {
            @Override
            public void onQueryAPPairStatus(int code, APPairStatus status) {
                //NooieLog.d("-->> NooieScanPresenter onQueryAPPairStatus code=" + code + " status=" + Optional.fromNullable(status).or(APPairStatus.AP_PAIR_NO_RECV_WIFI).getIntValue());
                NooieLog.d("-->> NooieScanPresenter onQueryAPPairStatus code=" + code + " status=" + (status == null ? APPairStatus.AP_PAIR_NO_RECV_WIFI.getIntValue() : status.getIntValue()));
                if (mNooieScanView != null) {
                    mNooieScanView.onQueryAPPairStatus(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, status);
                }
            }
        });
    }

    public void stopQueryAPPairStatusTask() {
        NooieLog.d("-->> debug NooieScanPresenter stopQueryAPPairStatusTask: ");
        ApHelper.getInstance().stopQueryAPPairStatusTask();
    }

    private final static int SCAN_TIME_SCALE = (int)(SCAN_LIMIT_TIME * (1 / 10f));
    private final static int SCAN_PROCESS_90 = 90;
    private final static int SCAN_PROCESS_10 = 10;
    private int getUpgradeProcess(int time) {
        int process = 0;
        if (time <= SCAN_TIME_SCALE) {
            process = (int)(((float)time / SCAN_TIME_SCALE) * SCAN_PROCESS_90);
        } else {
            process = SCAN_PROCESS_90 + (int)(((float)(time - SCAN_TIME_SCALE) / (SCAN_LIMIT_TIME - SCAN_TIME_SCALE)) * SCAN_PROCESS_10);
        }
        NooieLog.d("-->> NooieScanPresenter getUpgradeProcess time=" + time + " scanTimeScale=" + SCAN_TIME_SCALE + " process=" + process);
        return process;
    }

    @Override
    public void destroy() {
        mNooieScanView = null;
    }

    public class RepeatGetRecentBindDeviceException extends Exception {
    }
}
