package com.afar.osaio.smart.device.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.listener.ConnectShortLinkDeviceListener;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.contract.DeviceConnectionContract;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.detector.TrackerRouterInfo;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.BindDeviceResult;
import com.nooie.sdk.api.network.base.bean.entity.CompatibleDevice;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.DeviceConnInfo;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class DeviceConnectionPresenter implements DeviceConnectionContract.Presenter {

    private static final int PAGE_MAX_DEVICE_NUM = 100;
    private static final int CHECK_DEVICE_CONNECTION_TIME = 2 * 60;
    private static final int RETRY_CHECK_DEVICE_CMD_CONNECT_COUNT = 3;
    private static final int RETRY_DESTROY_ALL_CONN_COUNT = 3;
    private static final int DELAY_START_DEVICE_CONNECT_TIME = 5 * 1000;
    private static final int DELAY_CONNECT_DEVICE_AFTER_INIT_CONN = 8 * 1000;
    private static final int CHECK_DEVICES_CONNECTION_TIME = 10 * 60;
    private static final int CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_TIME = 30 * 1000;
    private static final int QUICK_CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_TIME = 30 * 1000;
    private static final int SHORT_LINK_KEEP_TIME_LEN = 3 * 60 * 1000;

    private DeviceConnectionContract.View mTaskView;
    private Subscription mAllDeviceConnectionTask = null;
    private Subscription mHubDeviceConnectionTask = null;
    private Subscription mStartConnectShortLinkDeviceTask = null;
    private Subscription mStartQuickConnectShortLinkDeviceTask = null;
    private Subscription mReconnectDeviceTask;
    private Subscription mCheckDeviceConnectionTask = null;
    private Subscription mDeviceConnectTask = null;
    private Subscription mCheckDevicesConnectionTask = null;
    private Subscription mSendHeartBeatTask = null;
    private Subscription mShortLinkKeepTask = null;

    private String mLastConnectSSID = "";
    private boolean mIsPauseCheckConn = false;
    private boolean mIsStopCheckDeviceCmdConnect = false;
    private int mRetryCheckDeviceCmdCount = 0;
    private int mDestroyConnCount = 0;
    private boolean mIsStopDestroyDeviceConn = false;
    private Map<String, Integer> mCheckDeviceMarks = new ArrayMap<>();
    private int mDeviceHbConnectionErrorCount = 0;
    private long mLastSendingHeartBeat = 0;
    private int mSendHeartBeatCount = 1;

    public DeviceConnectionPresenter(DeviceConnectionContract.View view) {
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
    public void startAllDeviceConnectionTask(String user, boolean isForceConnect) {
        stopAllDeviceConnectionTask();
        mAllDeviceConnectionTask = DeviceService.getService().getBindDevices(0, PAGE_MAX_DEVICE_NUM)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindDeviceResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<BindDeviceResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieDeviceHelper.tryConnectionToDevice(user, response.getData().getData(), isForceConnect);
                        }
                        startHubDeviceConnectionTask(user);
                    }
                });
        /*
        stopAllDeviceConnectionTask();
        mAllDeviceConnectionTask = DeviceService.getService().getBindDevices(0, PAGE_MAX_DEVICE_NUM)
                .onErrorReturn(new Func1<Throwable, BaseResponse<BindDeviceResult>>() {
                    @Override
                    public BaseResponse<BindDeviceResult> call(Throwable throwable) {
                        return null;
                    }
                })
                .flatMap(new Func1<BaseResponse<BindDeviceResult>, Observable<BaseResponse<BindDeviceResult>>>() {
                    @Override
                    public Observable<BaseResponse<BindDeviceResult>> call(BaseResponse<BindDeviceResult> response) {
                        if (response == null) {
                            return Observable.error(new RetryException());
                        }
                        return Observable.just(response);
                    }
                })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (throwable instanceof RetryException) {
                                    return Observable.just(1);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindDeviceResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<BindDeviceResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieDeviceHelper.tryConnectionToDevice(user, response.getData().getData(), isForceConnect);
                        }
                        startHubDeviceConnectionTask(user);
                    }
                });

         */
    }

    @Override
    public void stopAllDeviceConnectionTask() {
        if (mAllDeviceConnectionTask != null && !mAllDeviceConnectionTask.isUnsubscribed()) {
            mAllDeviceConnectionTask.unsubscribe();
            mAllDeviceConnectionTask = null;
        }
    }

    @Override
    public void startHubDeviceConnectionTask(String user) {
        stopHubDeviceConnectionTask();
        mHubDeviceConnectionTask = DeviceService.getService().getGatewayDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<List<GatewayDevice>> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieDeviceHelper.tryConnectionToGatewayDevice(user, response.getData(), true);
                        }
                    }
                });
        /*
        stopHubDeviceConnectionTask();
        mHubDeviceConnectionTask = DeviceService.getService().getGatewayDevices()
                .onErrorReturn(new Func1<Throwable, BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public BaseResponse<List<GatewayDevice>> call(Throwable throwable) {
                        return null;
                    }
                })
                .flatMap(new Func1<BaseResponse<List<GatewayDevice>>, Observable<BaseResponse<List<GatewayDevice>>>>() {
                    @Override
                    public Observable<BaseResponse<List<GatewayDevice>>> call(BaseResponse<List<GatewayDevice>> response) {
                        if (response == null) {
                            return Observable.error(new RetryException());
                        }
                        return Observable.just(response);
                    }
                })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (throwable instanceof RetryException) {
                                    return Observable.just(1);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<List<GatewayDevice>> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieDeviceHelper.tryConnectionToGatewayDevice(user, response.getData(), true);
                        }
                    }
                });

         */
    }

    @Override
    public void stopHubDeviceConnectionTask() {
        if (mHubDeviceConnectionTask != null && !mHubDeviceConnectionTask.isUnsubscribed()) {
            mHubDeviceConnectionTask.unsubscribe();
            mHubDeviceConnectionTask = null;
        }
    }


    @Override
    public void tryToReconnectWhenWifiChanged(Context context, String user) {
        stopReconnectDeviceTask();
        try {
            mReconnectDeviceTask = Observable.just(1)
                    .flatMap(new Func1<Integer, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Integer integer) {
                            NooieLog.d("-->> DeviceConnectionPresenter tryToReconnectWhenWifiChanged user=" + user + " isWifiConnecton=" + NetworkUtil.isWifiConnected(context) + " ssid=" + NetworkUtil.getSSIDAuto(context) + " lastSSID=" + mLastConnectSSID);
                            boolean isReconnect = false;
                            if (NetworkUtil.isWifiConnected(context)) {
                                isReconnect = !NooieDeviceHelper.checkApFutureCode(NetworkUtil.getSSIDAuto(context)) && NooieDeviceHelper.checkApFutureCode(mLastConnectSSID) && DeviceConnectionCache.getInstance().isEmpty();
                                mLastConnectSSID = NetworkUtil.getSSIDAuto(context);
                            }
                            return Observable.just(isReconnect);
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
                            NooieLog.d("-->> DeviceConnectionPresenter tryToReconnectWhenWifiChanged user=" + user + " result=" + result);
                            if (result) {
                                startAllDeviceConnectionTask(user, true);
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stopReconnectDeviceTask() {
        if (mReconnectDeviceTask != null && !mReconnectDeviceTask.isUnsubscribed()) {
            mReconnectDeviceTask.unsubscribe();
            mReconnectDeviceTask = null;
        }
    }

    @Override
    public void setIsPauseCheckConn(boolean isPauseCheckConn) {
        mIsPauseCheckConn = isPauseCheckConn;
    }

    public boolean isPauseCheckConn() {
        return mIsPauseCheckConn;
    }

    @Override
    public void checkDeviceConnection(Context context, String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1001 user=" + user);
        stopCheckDeviceConnection();
        mCheckDeviceConnectionTask = Observable.interval(CHECK_DEVICE_CONNECTION_TIME, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<CompatibleDevice>>() {
                    @Override
                    public Observable<CompatibleDevice> call(Long value) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1002 user=" + user + " isPauseCheckConn=" + isPauseCheckConn());
                        if (isPauseCheckConn() || !NetworkUtil.isConnected(context)) {
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1003 user=" + user + " p2purl=" + GlobalData.getInstance().getP2pUrl());
                        //TrackerRouterInfo trackerRouterInfo = NetworkUtil.getTrackRouterInfoByPing(APIConfig.BASE_HOST_NAME, 3);
                        TrackerRouterInfo trackerRouterInfo = NetworkUtil.getTrackRouterInfoByPing(NetConfigure.getInstance().getBaseHostName(), 3);
                        if (trackerRouterInfo != null && trackerRouterInfo.getResult() == 0) {
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1004 user=" + user);
                            List<DeviceConnInfo> deviceConnInfos = DeviceConnectionCache.getInstance().getAllDeviceConnInfo();
                            String deviceId = "";
                            for (DeviceConnInfo deviceConnInfo : CollectionUtil.safeFor(deviceConnInfos)) {
                                if (deviceConnInfo != null && !TextUtils.isEmpty(deviceConnInfo.getUuid())){
                                    deviceId = deviceConnInfo.getUuid();
                                    break;
                                }
                            }
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1005 user=" + user + " deviceId=" + deviceId);
                            return TextUtils.isEmpty(deviceId) ? Observable.just(null) : getConnectDevice(deviceId)
                                    .onErrorReturn(new Func1<Throwable, CompatibleDevice>() {
                                        @Override
                                        public CompatibleDevice call(Throwable throwable) {
                                            return null;
                                        }
                                    });
                        }
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1006 user=" + user);
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompatibleDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(CompatibleDevice result) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1007 user=" + user);
                        if (result != null) {
                            boolean isNeedToCheck = false;
                            String deviceId = "";
                            String pDeviceId = "";
                            if (result.getBindDevice() != null && result.getGatewayDevice() != null) {
                                isNeedToCheck = result.getBindDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON && result.getGatewayDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON;
                                deviceId = result.getBindDevice().getUuid();
                                pDeviceId = result.getGatewayDevice().getUuid();
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1008 user=" + user + " deviceId=" + deviceId + " pDeviceId=" + pDeviceId);
                            } else if (result.getBindDevice() != null) {
                                isNeedToCheck = result.getBindDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON;
                                deviceId = result.getBindDevice().getUuid();
                                pDeviceId = result.getBindDevice().getPuuid();
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1009 user=" + user + " deviceId=" + deviceId + " pDeviceId=" + pDeviceId);
                            }
                            if (isNeedToCheck) {
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1010 user=" + user);
                                mRetryCheckDeviceCmdCount = RETRY_CHECK_DEVICE_CMD_CONNECT_COUNT;
                                mIsStopCheckDeviceCmdConnect = false;
                                checkDeviceCmdConnect(user, deviceId, pDeviceId);
                            } else if (NooieDeviceHelper.isSubDevice(pDeviceId)) {
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1011 user=" + user);
                                DeviceConnectionCache.getInstance().removeConnection(pDeviceId);
                            } else if (!TextUtils.isEmpty(deviceId)) {
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1012 user=" + user);
                                DeviceConnectionCache.getInstance().removeConnection(deviceId);
                            }
                        } else if (CollectionUtil.isEmpty(DeviceConnectionCache.getInstance().getAllDeviceConnInfo())) {
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 1013 user=" + user);
                            startAllDeviceConnectionTask(user, true);
                        }
                    }
                });
    }

    @Override
    public void stopCheckDeviceConnection() {
        if (mCheckDeviceConnectionTask != null && !mCheckDeviceConnectionTask.isUnsubscribed()) {
            mCheckDeviceConnectionTask.unsubscribe();
            mCheckDeviceConnectionTask = null;
        }
        mIsStopCheckDeviceCmdConnect = true;
        mIsStopDestroyDeviceConn = true;
    }

    private void checkDeviceCmdConnect(String user, String deviceId, String pDeviceId) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2000 user=" + user + " retryCheckDeviceCmdCount=" + mRetryCheckDeviceCmdCount);
        mRetryCheckDeviceCmdCount--;
        DeviceCmdService.getInstance(NooieApplication.mCtx).checkConn(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2001 user=" + user + " code=" + code);
                if (mIsStopCheckDeviceCmdConnect) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2002 user=" + user);
                    return;
                }
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2003 user=" + user);
                } else if (mRetryCheckDeviceCmdCount > 0) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2004 user=" + user);
                    checkDeviceCmdConnect(user, deviceId, pDeviceId);
                } else {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2005 user=" + user);
                    mDestroyConnCount = RETRY_DESTROY_ALL_CONN_COUNT;
                    mIsStopDestroyDeviceConn = false;
                    reInitDeviceConn(user);
                    if (mTaskView != null) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 2006 user=" + user);
                        mTaskView.onCheckDeviceConnectResult(true, user, deviceId, pDeviceId);
                    }
                }
            }
        });
    }

    public void reInitDeviceConn(String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 3000 user=" + user + " destoryConnCount=" + mDestroyConnCount);
        mDestroyConnCount--;
        DeviceCmdService.getInstance(NooieApplication.mCtx).destroyAllConn(new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 3001 user=" + user + " code=" + code);
                if (mIsStopDestroyDeviceConn) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 3002 user=" + user);
                    return;
                }
                if (code == Constant.OK || mDestroyConnCount <= 0) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 3003 user=" + user);

                    tryToInitDeviceConn(user);
                } else if (mDestroyConnCount > 0) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 3004 user=" + user);
                    reInitDeviceConn(user);
                }
            }
        });
    }

    public void tryToInitDeviceConn(String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4001");

        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer value) {
                        try {
                            NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4002");
                            NooieDeviceHelper.initNativeConnect();
                            NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4003");
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        return Observable.just(true);
                    }
                })
                .delay(DELAY_CONNECT_DEVICE_AFTER_INIT_CONN, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4006");
                        NooieLog.printStackTrace(new Exception(e));
                    }

                    @Override
                    public void onNext(Boolean result) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4004");
                        startAllDeviceConnectionTask(user, true);
                        NooieLog.d("-->> debug DeviceConnectionPresenter testDeviceConnect: 4005");
                    }
                });
    }

    @Override
    public void startDeviceConnectTask(String user, String deviceId) {
        stopDeviceConnectTask();
        mDeviceConnectTask = getConnectDevice(deviceId)
                .delay(DELAY_START_DEVICE_CONNECT_TIME, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompatibleDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(CompatibleDevice compatibleDevice) {
                        if (compatibleDevice != null && compatibleDevice.getGatewayDevice() != null && !DeviceConnectionCache.getInstance().isExisted(compatibleDevice.getGatewayDevice().getUuid())) {
                            List<GatewayDevice> gatewayDevices = new ArrayList<>();
                            gatewayDevices.add(compatibleDevice.getGatewayDevice());
                            NooieDeviceHelper.tryConnectionToGatewayDevice(user, gatewayDevices, false);
                        } else if (compatibleDevice != null && compatibleDevice.getBindDevice() != null && !DeviceConnectionCache.getInstance().isExisted(compatibleDevice.getBindDevice().getUuid())) {
                            List<BindDevice> bindDevices = new ArrayList<>();
                            bindDevices.add(compatibleDevice.getBindDevice());
                            NooieDeviceHelper.tryConnectionToDevice(user, bindDevices, false);
                        }
                    }
                });
    }

    @Override
    public void stopDeviceConnectTask() {
        if (mDeviceConnectTask != null && !mDeviceConnectTask.isUnsubscribed()) {
            mDeviceConnectTask.unsubscribe();
            mDeviceConnectTask = null;
        }
    }

    private Observable<CompatibleDevice> getConnectDevice(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return Observable.just(null);
        }
        return DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<CompatibleDevice>>() {
                    @Override
                    public Observable<CompatibleDevice> call(BaseResponse<BindDevice> response) {
                        CompatibleDevice compatibleDevice = new CompatibleDevice();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            compatibleDevice.setBindDevice(response.getData());
                            if (NooieDeviceHelper.isSubDevice(response.getData().getPuuid())) {
                                return getGatewayDevice(response.getData().getPuuid())
                                        .flatMap(new Func1<GatewayDevice, Observable<CompatibleDevice>>() {
                                            @Override
                                            public Observable<CompatibleDevice> call(GatewayDevice gatewayDevice) {
                                                if (gatewayDevice != null && compatibleDevice != null) {
                                                    compatibleDevice.setGatewayDevice(gatewayDevice);
                                                }
                                                return Observable.just(compatibleDevice);
                                            }
                                        });
                            }
                        }
                        return Observable.just(compatibleDevice);
                    }
                });
    }

    private Observable<GatewayDevice> getGatewayDevice(String pDeviceId) {
        return DeviceService.getService().getDeviceInfo(pDeviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<GatewayDevice>>() {
                    @Override
                    public Observable<GatewayDevice> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            GatewayDevice gatewayDevice = new GatewayDevice();
                            gatewayDevice.setBindDevice(response.getData());
                            Observable<GatewayDevice> parentDeviceObservable = Observable.just(gatewayDevice);
                            Observable<BaseResponse<List<BindDevice>>> subDevicesObservable = DeviceService.getService().getSubDevices(response.getData().getUuid());
                            return Observable.zip(parentDeviceObservable, subDevicesObservable, new Func2<GatewayDevice, BaseResponse<List<BindDevice>>, GatewayDevice>() {
                                @Override
                                public GatewayDevice call(GatewayDevice parentDevice, BaseResponse<List<BindDevice>> subResponse) {
                                    if (subResponse != null && subResponse.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(subResponse.getData()) && parentDevice != null) {
                                        parentDevice.setChild(subResponse.getData());
                                    }
                                    return gatewayDevice;
                                }
                            });
                        }
                        return Observable.just(null);
                    }
                });
    }

    private Observable<List<CompatibleDevice>> getAllDevices() {
        Observable<BaseResponse<BindDeviceResult>> getBindDeviceObservable = DeviceService.getService().getBindDevices(1, PAGE_MAX_DEVICE_NUM)
                .onErrorReturn(new Func1<Throwable, BaseResponse<BindDeviceResult>>() {
                    @Override
                    public BaseResponse<BindDeviceResult> call(Throwable throwable) {
                        return null;
                    }
                });

        Observable<BaseResponse<List<GatewayDevice>>> getGatewayDeviceObservable = DeviceService.getService().getGatewayDevices()
                .onErrorReturn(new Func1<Throwable, BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public BaseResponse<List<GatewayDevice>> call(Throwable throwable) {
                        return null;
                    }
                });

        return Observable.zip(getBindDeviceObservable, getGatewayDeviceObservable, new Func2<BaseResponse<BindDeviceResult>, BaseResponse<List<GatewayDevice>>, List<CompatibleDevice>>() {
            @Override
            public List<CompatibleDevice> call(BaseResponse<BindDeviceResult> normalResponse, BaseResponse<List<GatewayDevice>> gatewayResponse) {
                List<CompatibleDevice> devices = new ArrayList<>();
                List<String> gatewayDeviceIds = new ArrayList<>();
                if (gatewayResponse != null && gatewayResponse.getCode() == StateCode.SUCCESS.code) {
                    for (GatewayDevice gatewayDevice : CollectionUtil.safeFor(gatewayResponse.getData())) {
                        if (gatewayDevice != null && gatewayDevice.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                            CompatibleDevice compatibleDevice = new CompatibleDevice();
                            compatibleDevice.setGatewayDevice(gatewayDevice);
                            devices.add(compatibleDevice);
                            if (!TextUtils.isEmpty(gatewayDevice.getUuid())) {
                                gatewayDeviceIds.add(gatewayDevice.getUuid());
                            }
                        }
                    }
                }

                if (normalResponse != null && normalResponse.getCode() == StateCode.SUCCESS.code && normalResponse.getData() != null) {
                    for (BindDevice bindDevice : CollectionUtil.safeFor(normalResponse.getData().getData())) {
                        if (bindDevice != null && bindDevice.getOnline() == ApiConstant.ONLINE_STATUS_ON && !gatewayDeviceIds.contains(bindDevice.getPuuid())) {
                            CompatibleDevice compatibleDevice = new CompatibleDevice();
                            compatibleDevice.setBindDevice(bindDevice);
                            devices.add(compatibleDevice);
                        }
                    }
                }
                return devices;
            }
        });
    }

    @Override
    public void checkDevicesConnection(Context context, String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesConnection 5001 user=" + user);
        stopCheckDevicesConnection();
        mCheckDevicesConnectionTask = Observable.interval(CHECK_DEVICES_CONNECTION_TIME, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<List<CompatibleDevice>>>() {
                    @Override
                    public Observable<List<CompatibleDevice>> call(Long value) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesConnection 5002 user=" + user + " isPauseCheckConn=" + isPauseCheckConn());
                        if (isPauseCheckConn() || !NetworkUtil.isConnected(context) || mCheckDeviceMarks == null || !mCheckDeviceMarks.isEmpty() || ApHelper.getInstance().checkIsApDirectConnectionMode()) {
                            if (mCheckDeviceMarks != null) {
                                mCheckDeviceMarks.clear();
                            }
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesConnection 5003 user=" + user + " p2purl=" + GlobalData.getInstance().getP2pUrl());
                        TrackerRouterInfo trackerRouterInfo = NetworkUtil.getTrackRouterInfoByPing(NetConfigure.getInstance().getBaseHostName(), 3);
                        if (trackerRouterInfo != null && trackerRouterInfo.getResult() == 0) {
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesConnection 5004 user=" + user);
                            return getAllDevices();
                        }
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesConnection 5006 user=" + user);
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CompatibleDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<CompatibleDevice> result) {
                        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5007 user=" + user + " size=" + CollectionUtil.size(result));
                        if (CollectionUtil.isNotEmpty(result) && !DeviceConnectionCache.getInstance().isEmpty()) {
                            boolean isConnectAllDevice = true;
                            for (CompatibleDevice compatibleDevice : CollectionUtil.safeFor(result)) {
                                if (compatibleDevice != null && compatibleDevice.getGatewayDevice() != null) {
                                    if (DeviceConnectionCache.getInstance().isConnectionExist(compatibleDevice.getGatewayDevice().getUuid())) {
                                        mCheckDeviceMarks.put(compatibleDevice.getGatewayDevice().getUuid(), 0);
                                    } else if (!TextUtils.isEmpty(compatibleDevice.getGatewayDevice().getUuid())) {
                                        isConnectAllDevice = false;
                                    }
                                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5008 user=" + user + " deviceId=" + compatibleDevice.getGatewayDevice().getUuid());
                                } else if (compatibleDevice != null && compatibleDevice.getBindDevice() != null) {
                                    if (NooieDeviceHelper.isSubDevice(compatibleDevice.getBindDevice().getPuuid(), compatibleDevice.getBindDevice().getType()) && DeviceConnectionCache.getInstance().isConnectionExist(compatibleDevice.getBindDevice().getPuuid())) {
                                        mCheckDeviceMarks.put(compatibleDevice.getBindDevice().getPuuid(), 0);
                                    } else if (DeviceConnectionCache.getInstance().isConnectionExist(compatibleDevice.getBindDevice().getUuid())) {
                                        mCheckDeviceMarks.put(compatibleDevice.getBindDevice().getUuid(), 0);
                                    } else if (!TextUtils.isEmpty(compatibleDevice.getBindDevice().getUuid())) {
                                        isConnectAllDevice = false;
                                    }
                                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5009 user=" + user + " deviceId=" + compatibleDevice.getBindDevice().getUuid());
                                }
                            }
                            isConnectAllDevice = mCheckDeviceMarks != null && !mCheckDeviceMarks.isEmpty() && isConnectAllDevice;
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5010 user=" + user + " isConnectAllDevice=" + isConnectAllDevice);
                            if (isConnectAllDevice) {
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5011 user=" + user);
                                mRetryCheckDeviceCmdCount = RETRY_CHECK_DEVICE_CMD_CONNECT_COUNT;
                                mIsStopCheckDeviceCmdConnect = false;
                                checkDevicesCmdConnect(user);
                            } else {
                                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5012 user=" + user);
                                startAllDeviceConnectionTask(user, false);
                            }
                        } else if (CollectionUtil.isNotEmpty(result)) {
                            NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 5013 user=" + user);
                            startAllDeviceConnectionTask(user, true);
                        }
                    }
                });
    }

    @Override
    public void stopCheckDevicesConnection() {
        if (mCheckDevicesConnectionTask != null && !mCheckDevicesConnectionTask.isUnsubscribed()) {
            mCheckDevicesConnectionTask.unsubscribe();
            mCheckDevicesConnectionTask = null;
        }
        mIsStopCheckDeviceCmdConnect = true;
        mIsStopDestroyDeviceConn = true;
        if (mCheckDeviceMarks != null) {
            mCheckDeviceMarks.clear();
        }
    }

    private void checkDevicesCmdConnect(String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesCmdConnect 6000 user=" + user);
        String deviceId = null;
        for (Map.Entry<String, Integer> checkDeviceMark : mCheckDeviceMarks.entrySet()) {
            if (checkDeviceMark != null && checkDeviceMark.getValue() == 0) {
                deviceId = checkDeviceMark.getKey();
                break;
            }
        }
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesCmdConnect 6001 user=" + user + " deviceId=" + deviceId);
        if (TextUtils.isEmpty(deviceId)) {
            tryToReconnectDevice(user);
            return;
        }
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesCmdConnect 6002 user=" + user + " deviceId=" + deviceId);
        mRetryCheckDeviceCmdCount = RETRY_CHECK_DEVICE_CMD_CONNECT_COUNT;
        checkDeviceCmdConnect(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug DeviceConnectionPresenter checkDevicesCmdConnect 6003 user=" + user + " code=" + code);
                checkDevicesCmdConnect(user);
            }
        });
    }

    private void checkDeviceCmdConnect(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7000 deviceId=" + deviceId + " retryCheckDeviceCmdCount=" + mRetryCheckDeviceCmdCount);
        if (TextUtils.isEmpty(deviceId)) {
            return;
        }
        mRetryCheckDeviceCmdCount--;
        DeviceCmdService.getInstance(NooieApplication.mCtx).checkConn(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7001 deviceId=" + deviceId + " code=" + code);
                if (mIsStopCheckDeviceCmdConnect) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7002 deviceId=" + deviceId);
                    return;
                }
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7003 deviceId=" + deviceId);
                    if (mCheckDeviceMarks != null && mCheckDeviceMarks.containsKey(deviceId)) {
                        mCheckDeviceMarks.put(deviceId, 1);
                    }
                } else if (mRetryCheckDeviceCmdCount > 0) {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7004 deviceId=" + deviceId);
                    checkDeviceCmdConnect(deviceId, listener);
                    return;
                } else {
                    NooieLog.d("-->> debug DeviceConnectionPresenter checkDeviceConnection 7005 deviceId=" + deviceId);
                    if (mCheckDeviceMarks != null && mCheckDeviceMarks.containsKey(deviceId)) {
                        mCheckDeviceMarks.put(deviceId, 2);
                    }
                }
                if (listener != null) {
                    listener.onResult(code);
                }
            }
        });
    }

    private void tryToReconnectDevice(String user) {
        NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8000");
        boolean isNeedToReconnect = mCheckDeviceMarks != null && !mCheckDeviceMarks.isEmpty();
        NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8001 isNeedToReconnect=" + isNeedToReconnect);
        for (Map.Entry<String, Integer> checkDeviceMark : mCheckDeviceMarks.entrySet()) {
            if (checkDeviceMark != null && checkDeviceMark.getValue() < 2) {
                isNeedToReconnect = false;
                break;
            }
        }
        NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8003 isNeedToReconnect=" + isNeedToReconnect);
        if (isNeedToReconnect) {
            NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8004");
            mDestroyConnCount = RETRY_DESTROY_ALL_CONN_COUNT;
            mIsStopDestroyDeviceConn = false;
            reInitDeviceConn(user);
            NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8005");
        } else {
            NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8006");
        }
        NooieLog.d("-->> debug DeviceConnectionPresenter tryToReconnectDevice: 8007");
        if (mCheckDeviceMarks != null) {
            mCheckDeviceMarks.clear();
        }
    }

    @Override
    public void startConnectShortLinkDevice(String taskId, String account, BindDevice device, ConnectShortLinkDeviceListener listener) {
        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || device == null) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, new String());
            }
            return;
        }
        stopConnectShortLinkDevice();
        List<BindDevice> devices = new ArrayList<>();
        devices.add(device);
        boolean isConnectSuccess = NooieDeviceHelper.tryConnectToSingleDevice(account, devices, true);
        if (!isConnectSuccess) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_CONNECT_P2P_FAIL, taskId, account, device.getUuid());
            }
            return;
        }
        mStartConnectShortLinkDeviceTask = Observable.just(1)
                .delay(CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_TIME, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_ERROR, taskId, account, device.getUuid());
                        }
                    }

                    @Override
                    public void onNext(Integer value) {
                        if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_SUCCESS, taskId, account, device.getUuid());
                        }
                    }
                });
    }

    @Override
    public void stopConnectShortLinkDevice() {
        if (mStartConnectShortLinkDeviceTask != null && !mStartConnectShortLinkDeviceTask.isUnsubscribed()) {
            mStartConnectShortLinkDeviceTask.unsubscribe();
            mStartConnectShortLinkDeviceTask = null;
        }
    }

    @Override
    public void startQuickConnectShortLinkDevice(String taskId, String account, BindDevice device, ConnectShortLinkDeviceListener listener) {
        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || device == null) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, new String());
            }
            return;
        }
        stopQuickConnectShortLinkDevice();
        List<BindDevice> devices = new ArrayList<>();
        devices.add(device);
        boolean isConnectSuccess = NooieDeviceHelper.tryConnectToSingleDevice(account, devices, true);
        if (!isConnectSuccess) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_CONNECT_P2P_FAIL, taskId, account, device.getUuid());
            }
            return;
        }
        mStartQuickConnectShortLinkDeviceTask = Observable.just(1)
                .delay(QUICK_CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_TIME, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_ERROR, taskId, account, device.getUuid());
                        }
                    }

                    @Override
                    public void onNext(Integer value) {
                        if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_SUCCESS, taskId, account, device.getUuid());
                        }
                    }
                });
    }

    @Override
    public void stopQuickConnectShortLinkDevice() {
        if (mStartQuickConnectShortLinkDeviceTask != null && !mStartQuickConnectShortLinkDeviceTask.isUnsubscribed()) {
            mStartQuickConnectShortLinkDeviceTask.unsubscribe();
            mStartQuickConnectShortLinkDeviceTask = null;
        }
    }

    @Override
    public void connectShortLinkDevice(String taskId, String account, String deviceId, ConnectShortLinkDeviceListener listener) {
        NooieLog.d("-->> debug BaseActivityPresenterImpl connectShortLinkDevice: 1000 sortLinkDevice account=" + account + " deviceId=" + deviceId + " taskId=" + taskId);
        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || TextUtils.isEmpty(deviceId)) {
            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
            return;
        }
        BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
        if (device != null) {
            startQuickConnectShortLinkDevice(taskId, account, device, listener);
            return;
        }
        DeviceService.getService().getDeviceInfo(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            startQuickConnectShortLinkDevice(taskId, account, response.getData(), listener);
                        } else if (listener != null) {
                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
                        }
                    }
                });
    }

    public void startShortLinkKeepTask(String taskId) {
        stopShortLinkKeepTask();
        if (mTaskView != null) {
            mTaskView.onShortLinkKeepResult(taskId, DeviceConnectionHelper.SHORT_LINK_KEEP_TYPE_START);
        }
        mShortLinkKeepTask = Observable.just(1)
                .delay(SHORT_LINK_KEEP_TIME_LEN, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onShortLinkKeepResult(taskId, DeviceConnectionHelper.SHORT_LINK_KEEP_TYPE_ERROR);
                        }
                    }

                    @Override
                    public void onNext(Integer value) {
                        if (mTaskView != null) {
                            mTaskView.onShortLinkKeepResult(taskId, DeviceConnectionHelper.SHORT_LINK_KEEP_TYPE_TIME_END);
                        }
                    }
                });
    }

    @Override
    public void stopShortLinkKeepTask() {
        if (mShortLinkKeepTask != null && !mShortLinkKeepTask.isUnsubscribed()) {
            mShortLinkKeepTask.unsubscribe();
        }
        mShortLinkKeepTask = null;
    }

    @Override
    public void startSendHeartBeat(String deviceId) {
        startSendHeartBeat(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onSendHeartBeatResult(code);
                }
            }
        });
    }

    private void startSendHeartBeat(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1000");
        stopSendHeartBeat();
        updateDirectConnectionErrorCount(true);
        mSendHeartBeatTask = Observable.interval(0, DeviceConnectionHelper.SEND_HEART_BEAT, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Long value) {
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1001");
                        if (checkIsSendingHeartBeat()) {
                            NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1002");
                            DeviceCmdApi.getInstance().apXHeartBeat(deviceId, null);
                            return Observable.just(DeviceConnectionHelper.RESULT_SEND_HEART_BEAT_SENDING);
                        }
                        if (checkIsSendingHeartBeatTimeOut()) {
                            NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1003");
                            setLastSendingHeartBeat(0);
                            return Observable.just(DeviceConnectionHelper.RESULT_SEND_HEART_BEAT_TIME_OUT);
                        }
                        mSendHeartBeatCount = 1;
                        setLastSendingHeartBeat(System.currentTimeMillis());
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1004");
                        sendHeartBeat(deviceId, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1005 code=" + code);
                                updateDirectConnectionErrorCount(code == Constant.OK);
                                if (listener != null) {
                                    listener.onResult(code);
                                }
                                NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1006");
                            }
                        });
                        return Observable.just(DeviceConnectionHelper.RESULT_SEND_HEART_BEAT_FINISH);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1007");
                    }

                    @Override
                    public void onNext(Integer result) {
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1008 result=" + result);
                        if (result == DeviceConnectionHelper.RESULT_SEND_HEART_BEAT_TIME_OUT) {
                            updateDirectConnectionErrorCount(false);
                            if (listener != null) {
                                listener.onResult(Constant.ERROR);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopSendHeartBeat() {
        NooieLog.d("-->> debug ApHelper stopSendHeartBeat: 1000");
        if (mSendHeartBeatTask != null && !mSendHeartBeatTask.isUnsubscribed()) {
            mSendHeartBeatTask.unsubscribe();
            mSendHeartBeatTask = null;
        }
        updateDirectConnectionErrorCount(true);
    }

    @Override
    public boolean checkDirectConnectionIsError() {
        return mDeviceHbConnectionErrorCount > DeviceConnectionHelper.DEVICE_HB_CONNECTION_ERROR_MAX_COUNT;
    }

    private void updateDirectConnectionErrorCount(boolean isReset) {
        if (isReset) {
            mDeviceHbConnectionErrorCount = 0;
            return;
        }
        mDeviceHbConnectionErrorCount++;
    }

    private boolean checkIsSendingHeartBeat() {
        return System.currentTimeMillis() - mLastSendingHeartBeat < DeviceConnectionHelper.SENDING_HEART_BEAT_LIMIT_TIME;
    }

    private boolean checkIsSendingHeartBeatTimeOut() {
        return mLastSendingHeartBeat > 0 && !checkIsSendingHeartBeat();
    }

    private void setLastSendingHeartBeat(long time) {
        mLastSendingHeartBeat = time;
    }

    private void sendHeartBeat(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1000 deviceId=" + deviceId + " mCheckIsSendingApHeartBeat=" + checkIsSendingHeartBeat() + " mSendApHeartBeatCount=" + mSendHeartBeatCount);
        DeviceCmdApi.getInstance().apXHeartBeat(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1001 code=" + code);
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1002");
                    setLastSendingHeartBeat(0);
                    if (listener != null) {
                        listener.onResult(code);
                    }
                } else if (mSendHeartBeatCount <= DeviceConnectionHelper.HB_CMD_RETRY_MAX_COUNT) {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1010");
                    mSendHeartBeatCount++;
                    sendHeartBeat(deviceId, listener);
                } else {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1011");
                    setLastSendingHeartBeat(0);
                    if (listener != null) {
                        listener.onResult(code);
                    }
                }
            }
        });
    }

    private class RetryException extends Exception {

        public RetryException() {
        }
    }
}
