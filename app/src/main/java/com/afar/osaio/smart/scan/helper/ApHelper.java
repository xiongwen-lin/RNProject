package com.afar.osaio.smart.scan.helper;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.cache.ApDeviceInfoCache;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.bean.RequestSsidResult;
import com.afar.osaio.smart.scan.helper.contract.ApHelperContract;
import com.afar.osaio.smart.scan.helper.presenter.ApHelperPresenter;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.base.AppStateManager;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.APNetCfg;
import com.nooie.sdk.device.bean.APPairStatus;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.DeviceConnInfo;
import com.nooie.sdk.listener.OnAPPairStatusResultListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ApHelper implements ApHelperContract.View {

    public static final String KEY_PARAM_DEVICE_TYPE = "KEY_PARAM_DEVICE_TYPE";
    public static final String KEY_PARAM_CONNECTION_MODE = "KEY_PARAM_CONNECTION_MODE";
    public static final String KEY_PARAM_SSID = "KEY_PARAM_SSID";
    public static final String KEY_PARAM_DEFAULT_PW = "KEY_PARAM_DEFAULT_PW";
    public static final String KEY_PARAM_DEVICE_ID = "KEY_PARAM_DEVICE_ID";
    public static final String KEY_PARAM_SERVER = "KEY_PARAM_SERVER";
    public static final String KEY_PARAM_PORT = "KEY_PARAM_PORT";
    public static final String KEY_PARAM_DEVICE_MODEL = "KEY_PARAM_DEVICE_MODEL";
    public static final String INTENT_KEY_BLE_DEVICE = "INTENT_KEY_BLE_DEVICE";
    public static final String INTENT_KEY_OPEN_STATUS = "INTENT_KEY_OPEN_STATUS";
    public static final String KEY_PARAM_IS_RESTART_TASK = "KEY_PARAM_IS_RESTART";
    public static final String KEY_PARAM_UID = "KEY_PARAM_UID";
    public static final String KEY_PARAM_ACCOUNT = "KEY_PARAM_ACCOUNT";
    public static final String KEY_PARAM_IS_REMOVE_CACHE = "KEY_PARAM_IS_REMOVE_CACHE";

    private static final int MAX_AP_PAIR_RETRY_COUNT = 3;
    public static final int AP_PAIR_STATE_SUCCESS = 1;
    public static final int AP_PAIR_STATE_RETRY = 2;
    public static final int AP_PAIR_STATE_STOP = 3;
    private static final int SEND_HEART_BEAT = 5 * 1000;
    private final static long SENDING_AP_HEART_BEAT_LIMIT_TIME = 30 * 1000;

    public static final String ENCRYPT_OPEN = "OPEN";
    public static final String ENCRYPT_WPA = "WPA";

    public static final int AP_DIRECT_CONNECTION_CHECK_NORMAL = 1;
    public static final int AP_DIRECT_CONNECTION_CHECK_STARTING = 2;
    public static final int AP_DIRECT_CONNECTION_CHECK_FINISH = 3;

    public static final int CHECK_AP_DIRECT_CONNECTION_RESULT_ERROR = 1;
    public static final int CHECK_AP_DIRECT_CONNECTION_RESULT_GET_SSID_FAIL = 2;
    public static final int CHECK_AP_DIRECT_CONNECTION_RESULT_SSID_ILLEGAL = 3;
    public static final int CHECK_AP_DIRECT_CONNECTION_RESULT_GET_AP_STATUS = 4;

    private static final int RESULT_SEND_HEART_BEAT_SENDING = 1;
    private static final int RESULT_SEND_HEART_BEAT_TIME_OUT = 2;
    private static final int RESULT_SEND_HEART_BEAT_FINISH = 3;

    public static final int AP_DIRECT_CONNECTION_ERROR_MAX_COUNT = 3;
    public static final int AP_CMD_RETRY_MAX_COUNT = 3;

    public static final int BLE_AP_CONNECTION_KEEPING_FRONT_STATE_TIME_OUT = 1;
    public static final int BLE_AP_CONNECTION_KEEPING_FRONT_STATE_ERROR = 2;

    public static final int BLE_AP_CONNECTION_KEEPING_BACKGROUND_STATE_DISCONNECTED = 1;

    private ApHelperContract.Presenter mPresenter;
    private boolean mIsStopApPair = false;
    private Subscription mRetryStartAPPairTask;
    private Subscription mSendHeartBeatTask = null;
    private List<ApHelperListener> mApHelperListeners = null;
    private int mApDirectConnectionCheckState = AP_DIRECT_CONNECTION_CHECK_NORMAL;
    private Subscription mCheckApDirectConnectionTask = null;
    private boolean mStopCheckApDirectHeartBeat = false;
    private int mCheckApDirectHeartBeatCount = 1;
    private long mLastSendingApHeartBeat = 0;
    private int mSendApHeartBeatCount = 1;
    private int mApDirectConnectionErrorCount = 0;
    private Subscription mStartQueryAPPairStatusTask;
    private APPairStatusResultListener mAPPairStatusResultListener;
    private int mSetApDeviceInfoRetryCount = 1;
    private String mCurrentDvDeviceId;
    private Subscription mCheckNetworkWhenChangedTask = null;
    private CustomAppStateManagerListener mAppStateManagerListener = null;
    private boolean mIsEnterApDevicePage = false;

    private ApHelper() {
        new ApHelperPresenter(this);
    }

    private static class ApHelperHolder {
        private static final ApHelper INSTANCE = new ApHelper();
    }

    public static ApHelper getInstance() {
        return ApHelperHolder.INSTANCE;
    }

    @Override
    public void setPresenter(@NonNull ApHelperContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    public void tryStartAPPair(APNetCfg apNetCfg, ApPairListener listener) {
        mIsStopApPair = false;
        mApHelperListeners = new ArrayList<>();
        startAPPair(apNetCfg, listener);
    }

    public void startAPPair(APNetCfg apNetCfg, ApPairListener listener) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).startAPPair(apNetCfg, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> ApHelper startAPPair onResult code=" + code);
                if (mIsStopApPair) {
                    if (listener != null) {
                        listener.onAPPairResult(AP_PAIR_STATE_STOP, code);
                    }
                    return;
                }
                if (code == Constant.OK) {
                    if (listener != null) {
                        listener.onAPPairResult(AP_PAIR_STATE_SUCCESS, code);
                    }
                } else {
                    if (listener != null) {
                        listener.onAPPairResult(AP_PAIR_STATE_RETRY, code);
                    }
                    retryStartAPPair(apNetCfg, listener);
                }
            }
        });
    }

    public void stopAPPair() {
        mIsStopApPair = true;
        stopRetryStartAPPairTask();
    }

    public void retryStartAPPair(APNetCfg apNetCfg, ApPairListener listener) {
        stopRetryStartAPPairTask();
        mRetryStartAPPairTask = Observable.just("")
                .delay(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String s) {
                        startAPPair(apNetCfg, listener);
                    }
                });
    }

    public void stopRetryStartAPPairTask() {
        if (mRetryStartAPPairTask != null && !mRetryStartAPPairTask.isUnsubscribed()) {
            mRetryStartAPPairTask.unsubscribe();
        }
    }

    private void initAPPairStatusResultListener(QueryApPairStatusListener listener) {
        unInitAPPairStatusResultListener();
        mAPPairStatusResultListener = new APPairStatusResultListener();
        mAPPairStatusResultListener.setListener(listener);
    }

    private void unInitAPPairStatusResultListener() {
        if (mAPPairStatusResultListener != null) {
            mAPPairStatusResultListener.setListener(null);
            mAPPairStatusResultListener = null;
        }
    }

    public void startQueryAPPairStatus(QueryApPairStatusListener listener) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).getAPPairStatus(mAPPairStatusResultListener);
    }

    public void startQueryAPPairStatusTask(QueryApPairStatusListener listener) {
        stopQueryAPPairStatusTask();
        initAPPairStatusResultListener(listener);
        mStartQueryAPPairStatusTask = Observable.interval(0, 5 * 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long time) {
                        //NooieLog.d("-->> ApHelper startQueryAPPairStatusTask onNext time=" + time);
                        startQueryAPPairStatus(listener);
                    }
                });
    }

    public void stopQueryAPPairStatusTask() {
        unInitAPPairStatusResultListener();
        if (mStartQueryAPPairStatusTask != null && !mStartQueryAPPairStatusTask.isUnsubscribed()) {
            mStartQueryAPPairStatusTask.unsubscribe();
        }
    }

    public void trySwitchApConnectMode(Bundle param, APDirectListener listener) {
        if (param == null) {
            if (listener != null) {
                listener.onSwitchConnectionMode(false, ConstantValue.CONNECTION_MODE_QC, null);
            }
            return;
        }
        if (NooieDeviceHelper.mergeIpcType(param.getString(ConstantValue.INTENT_KEY_IPC_MODEL)) == IpcType.HC320) {
            switchBluetoothApConnectMode(param, listener);
        } else {
            switchNormalApConnectMode(param, listener);
        }
    }

    public void switchBluetoothApConnectMode(Bundle param, APDirectListener listener) {
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1000");
        if (mPresenter != null) {
            mPresenter.switchApDirectConnectMode(param, listener);
        } else if (listener != null) {
            int connectionMode = ConstantValue.CONNECTION_MODE_QC;
            listener.onSwitchConnectionMode(false, connectionMode, null);
        }
    }

    public void switchNormalApConnectMode(Bundle param, APDirectListener listener) {
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1000");
        if (mPresenter != null) {
            mPresenter.switchApDirectConnectMode(param, listener);
        } else if (listener != null) {
            int connectionMode = ConstantValue.CONNECTION_MODE_QC;
            listener.onSwitchConnectionMode(false, connectionMode, null);
        }
    }

    public void tryResetApConnectMode(Bundle param, APDirectListener listener) {
        if (mPresenter != null) {
            mPresenter.resetApDirectConnectMode(param, listener);
        }
    }

    public void tryResetApConnectMode(String model, APDirectListener listener) {
        if (mPresenter != null) {
            mPresenter.resetApDirectConnectMode(model, listener);
        }
    }

    public void removeBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener) {
        if (mPresenter != null) {
            mPresenter.removeBleApDeviceConnection(param, listener);
        }
    }

    public void removeBleApDeviceConnection(String account, String uid, String deviceId, String model, boolean isRemoveCache, ApHelper.APDirectListener listener) {
        Bundle param = new Bundle();
        param.putString(ApHelper.KEY_PARAM_ACCOUNT, account);
        param.putString(ApHelper.KEY_PARAM_UID, uid);
        param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, model);
        param.putString(ApHelper.KEY_PARAM_DEVICE_ID, deviceId);
        param.putBoolean(ApHelper.KEY_PARAM_IS_REMOVE_CACHE, isRemoveCache);
        removeBleApDeviceConnection(param, listener);
    }

    public void disconnectBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener) {
        if (mPresenter != null) {
            mPresenter.disconnectBleApDeviceConnection(param, listener);
        }
    }

    public void setLastApDirectConnectingExist(boolean exist) {
        if (mPresenter != null) {
            mPresenter.setLastApDirectConnectingExist(exist);
        }
    }

    public boolean getLastApDirectConnectingExist() {
        return mPresenter != null ? mPresenter.getLastApDirectConnectingExist() : false;
    }

    public void disconnectBleApDeviceConnection(String deviceId, String model, ApHelper.APDirectListener listener) {
        Bundle param = new Bundle();
        param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, model);
        param.putString(ApHelper.KEY_PARAM_DEVICE_ID, deviceId);
        disconnectBleApDeviceConnection(param, listener);
    }

    public int convertApDeviceType(String model) {
        if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
            return ConstantValue.AP_DEVICE_TYPE_BLE_LP;
        } else if (NooieDeviceHelper.mergeIpcType(model) == IpcType.MC120) {
            return ConstantValue.AP_DEVICE_TYPE_IPC;
        } else {
            return ConstantValue.AP_DEVICE_TYPE_NORMAL;
        }
    }

    public ApDeviceInfo getCurrentApDeviceInfo() {
        if (mPresenter != null) {
            return mPresenter.getCurrentApDeviceInfo();
        }
        return null;
    }

    public void startBleApDeviceConnectionFrontKeepingTask(Bundle param) {
        if (mPresenter != null) {
            mPresenter.startBleApConnectionFrontKeepingTask(param);
        }
    }

    public void stopBleApDeviceConnectionFrontKeepingTask() {
        if (mPresenter != null) {
            mPresenter.stopBleApConnectionFrontKeepingTask();
        }
    }

    public void setBleApDeviceConnectionFrontKeepingTask(BleApConnectionFrontKeepingListener listener) {
        if (mPresenter != null) {
            mPresenter.setBleApDeviceConnectionFrontKeepingListener(listener);
        }
    }

    public void checkBleApDeviceConnectionBackgroundKeepingTask(boolean isBackground, BleApConnectionBackgroundKeepingListener listener) {
        NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1001");
        if (!checkBleApDeviceConnectingExist()) {
            NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1002 isBackground" + isBackground + " getLastApDirectConnectingExist=" + getLastApDirectConnectingExist());
            if (!isBackground && getLastApDirectConnectingExist() && listener != null) {
                NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1003");
                setLastApDirectConnectingExist(false);
                listener.onResult(BLE_AP_CONNECTION_KEEPING_BACKGROUND_STATE_DISCONNECTED, null);
            }
            return;
        }
        setLastApDirectConnectingExist(false);
        String model = getCurrentApDeviceInfo() != null && getCurrentApDeviceInfo().getBindDevice() != null ? getCurrentApDeviceInfo().getBindDevice().getType() : "";
        NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1004");
        if (!(NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320)) {
            NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1005");
            return;
        }
        NooieLog.d("-->> debug ApHelper checkBleApDeviceConnectionBackgroundKeepingTask: 1006");
        if (mPresenter != null) {
            mPresenter.checkBleApDeviceConnectionBackgroundKeepingTask(isBackground, listener);
        }
    }

    public void switchApConnectMode(int connectionMode, String deviceSsid, APDirectListener listener) {
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1000 connectionMode=" + connectionMode);
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            for (DeviceConnInfo deviceConnInfo : CollectionUtil.safeFor(DeviceConnectionCache.getInstance().getAllDeviceConnInfo())) {
                if (deviceConnInfo != null && !TextUtils.isEmpty(deviceConnInfo.getUuid())) {
                    DeviceCmdService.getInstance(NooieApplication.mCtx).removeConnDevice(deviceConnInfo.getUuid());
                }
            }
            DeviceConnectionCache.getInstance().clearCache();
            List<String> deviceIds = new ArrayList<>();
            deviceIds.add(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            boolean result = DeviceCmdApi.getInstance().apNewConn(deviceIds);
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1001 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_AP_DIRECT);
            startSendHeartBeat(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, null);
            mSetApDeviceInfoRetryCount = 1;
            setApDeviceInfo(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> debug ApHelper switchApConnectMode: 1002 code=" + code);
                    loadApDevice(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, deviceSsid, new LoadApDeviceListener() {
                        @Override
                        public void onLoadDevice(int state, ApDeviceInfo device) {
                            if (state == SDKConstant.SUCCESS && device != null && device.getDevInfo() != null && !TextUtils.isEmpty(device.getDevInfo().uuid)) {
                                updateCurrentDvDeviceId(device.getDevInfo().uuid);
                            }
                            if (listener != null) {
                                listener.onSwitchConnectionMode((result && state == SDKConstant.SUCCESS), connectionMode, deviceSsid);
                            }
                        }
                    });
                }
            });
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1003");
        } else {
            boolean result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1004 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_NONE);
            Bundle data = new Bundle();
            data.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
            NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED, data);
            if (listener != null) {
                listener.onSwitchConnectionMode(result, connectionMode, deviceSsid);
            }
            stopSendHeartBeat();
        }
    }

    public void setApDeviceInfo(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1000 deviceId=" + deviceId);
        APNetCfg apNetCfg = new APNetCfg();
        apNetCfg.region = getCurrentRegion();
        apNetCfg.zone = CountryUtil.getCurrentTimezone();
        apNetCfg.encrypt = ApHelper.ENCRYPT_OPEN;
        NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1001");
        DeviceCmdApi.getInstance().setUserInfo(deviceId, apNetCfg, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1002");
                if (listener == null) {
                    NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1003");
                    return;
                }
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1004");
                    long deviceTime = System.currentTimeMillis() / 1000L;
                    DeviceCmdService.getInstance(NooieApplication.mCtx).setUTCTimeStamp(deviceId, deviceTime, new OnActionResultListener() {
                        @Override
                        public void onResult(int codeOfSetTime) {
                            NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1005 code=" + codeOfSetTime);
                            if (listener == null) {
                                return;
                            }
                            if (codeOfSetTime == Constant.OK) {
                                listener.onResult(codeOfSetTime);
                            } else if (mSetApDeviceInfoRetryCount <= AP_CMD_RETRY_MAX_COUNT) {
                                mSetApDeviceInfoRetryCount++;
                                setApDeviceInfo(deviceId, listener);
                            } else {
                                listener.onResult(codeOfSetTime);
                            }
                            NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1006");
                        }
                    });
                } else {
                    NooieLog.d("-->> debug ApHelper setApDeviceInfo: 1007");
                    listener.onResult(code);
                }
            }
        });
    }

    public void startSendHeartBeat(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1000");
        stopSendHeartBeat();
        updateApDirectConnectionErrorCount(true);
        mSendHeartBeatTask = Observable.interval(0, SEND_HEART_BEAT, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<Long, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Long value) {
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1001");
                        if (checkIsSendingApHeartBeat()) {
                            NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1002");
                            DeviceCmdApi.getInstance().apXHeartBeat(deviceId, null);
                            return Observable.just(RESULT_SEND_HEART_BEAT_SENDING);
                        }
                        if (checkIsSendingApHeartBeatTimeOut()) {
                            NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1003");
                            setLastSendingApHeartBeat(0);
                            return Observable.just(RESULT_SEND_HEART_BEAT_TIME_OUT);
                        }
                        mSendApHeartBeatCount = 1;
                        setLastSendingApHeartBeat(System.currentTimeMillis());
                        NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1004");
                        sendApHeartBeat(deviceId, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1005 code=" + code);
                                updateApDirectConnectionErrorCount(code == Constant.OK);
                                notifyApHeartBeatResponse(code);
                                if (listener != null) {
                                    listener.onResult(code);
                                }
                                NooieLog.d("-->> debug ApHelper startSendHeartBeat: 1006");
                            }
                        });
                        return Observable.just(RESULT_SEND_HEART_BEAT_FINISH);
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
                        if (result == RESULT_SEND_HEART_BEAT_TIME_OUT) {
                            updateApDirectConnectionErrorCount(false);
                            notifyApHeartBeatResponse(Constant.ERROR);
                            if (listener != null) {
                                listener.onResult(Constant.ERROR);
                            }
                        }
                    }
                });
    }

    public void stopSendHeartBeat() {
        NooieLog.d("-->> debug ApHelper stopSendHeartBeat: 1000");
        if (mSendHeartBeatTask != null && !mSendHeartBeatTask.isUnsubscribed()) {
            mSendHeartBeatTask.unsubscribe();
            mSendHeartBeatTask = null;
        }
    }

    public void setApDirectConnectionCheckState(int state) {
        mApDirectConnectionCheckState = state;
    }

    public int getApDirectConnectionCheckState() {
        return mApDirectConnectionCheckState;
    }

    public boolean apDirectConnectionCheckable() {
        return mApDirectConnectionCheckState == AP_DIRECT_CONNECTION_CHECK_NORMAL;
    }

    public boolean apDirectConnectionCheckFinish() {
        return mApDirectConnectionCheckState == AP_DIRECT_CONNECTION_CHECK_FINISH;
    }

    public void startCheckApDirectConnection(String deviceId, CheckApDirectConnectionListener listener) {
        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1000 deviceId=" + deviceId);
        stopCheckApDirectConnection();
        mCheckApDirectConnectionTask = Observable.just("")
                .flatMap(new Func1<String, Observable<RequestSsidResult>>() {
                    @Override
                    public Observable<RequestSsidResult> call(String s) {
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1001");
                        RequestSsidResult ssidResult = new RequestSsidResult();
                        //boolean wifiAvailable = NetworkUtil.isWifiConnected(NooieApplication.mCtx) && !NetworkUtil.pingNetworkAvailable(null, 1);
                        boolean wifiAvailable = NetworkUtil.isWifiConnected(NooieApplication.mCtx);
                        if (wifiAvailable) {
                            String ssid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                            ssidResult.setSsid(ssid);
                            NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1002 ssid=" + ssid);
                            wifiAvailable = TextUtils.isEmpty(ssid) ? !NetworkUtil.pingNetworkAvailable(null, 1) : true;
                            NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1003 wifiAvailable=" + wifiAvailable);
                        }
                        ssidResult.setType(wifiAvailable ? RequestSsidResult.TYPE_REQUEST_SSID_WIFI_VALID : RequestSsidResult.TYPE_REQUEST_SSID_WIFI_INVALID);
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1004 wifiAvailable=" + wifiAvailable);
                        /*
                        if (wifiAvailable) {
                            String ssid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                            ssidResult.setSsid(ssid);
                            NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1003 ssid=" + ssid);
                            return Observable.just(ssidResult);
                        }
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1004");
                         */
                        return Observable.just(ssidResult);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RequestSsidResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1005");
                        if (listener != null) {
                            listener.onCheckResult(false, CHECK_AP_DIRECT_CONNECTION_RESULT_ERROR, new String(), APPairStatus.AP_PAIR_NO_RECV_WIFI, new String());
                        }
                    }

                    @Override
                    public void onNext(RequestSsidResult result) {
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1006");
                        if (result == null || result.getType() != RequestSsidResult.TYPE_REQUEST_SSID_WIFI_VALID) {
                            NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1007");
                            if (listener != null) {
                                listener.onCheckResult(false, CHECK_AP_DIRECT_CONNECTION_RESULT_ERROR, new String(), APPairStatus.AP_PAIR_NO_RECV_WIFI, new String());
                            }
                            return;
                        }
                        if (TextUtils.isEmpty(result.getSsid())) {
                            NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1008");
                            if (listener != null) {
                                listener.onCheckResult(false, CHECK_AP_DIRECT_CONNECTION_RESULT_GET_SSID_FAIL, new String(), APPairStatus.AP_PAIR_NO_RECV_WIFI, new String());
                            }
                            return;
                        }
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1009");
                        boolean isApDeviceConnecting = NooieDeviceHelper.checkApFutureCode(result.getSsid());
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1010 isApDeviceConnecting=" + isApDeviceConnecting);
                        if (!isApDeviceConnecting) {
                            if (listener != null) {
                                listener.onCheckResult(false, CHECK_AP_DIRECT_CONNECTION_RESULT_SSID_ILLEGAL, new String(), APPairStatus.AP_PAIR_NO_RECV_WIFI, new String());
                            }
                            return;
                        }
                        NooieLog.d("-->> debug ApHelper startCheckApDirectConnection: 1011");
                        //getDeviceApState(result.getSsid(), listener);
                        if (listener != null) {
                            listener.onCheckResult(true, CHECK_AP_DIRECT_CONNECTION_RESULT_GET_AP_STATUS, result.getSsid(), APPairStatus.AP_PAIR_NO_RECV_WIFI, deviceId);
                        }
                    }
                });
    }

    public void stopCheckApDirectConnection() {
        NooieLog.d("-->> debug ApHelper stopCheckApDirectConnection: 1000");
        mStopCheckApDirectHeartBeat = true;
        if (mCheckApDirectConnectionTask != null && !mCheckApDirectConnectionTask.isUnsubscribed()) {
            mCheckApDirectConnectionTask.unsubscribe();
            mCheckApDirectConnectionTask = null;
        }
    }

    public int getCurrentConnectionMode() {
        return DeviceCmdApi.getInstance().checkIsApDirectCmdType() ? ConstantValue.CONNECTION_MODE_AP_DIRECT : ConstantValue.CONNECTION_MODE_QC;
    }

    public boolean checkIsApDirectConnectionMode() {
        return getCurrentConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT;
    }

    public boolean checkBleApDeviceConnectingExist() {
        return mPresenter != null ? mPresenter.checkBleApDeviceConnectingExist() : false;
    }

    public void updateApDirectConnectionErrorCount(boolean isReset) {
        if (isReset) {
            mApDirectConnectionErrorCount = 0;
            return;
        }
        mApDirectConnectionErrorCount++;
    }

    public boolean checkApDirectConnectionIsError() {
        return mApDirectConnectionErrorCount > AP_DIRECT_CONNECTION_ERROR_MAX_COUNT;
    }

    public void checkApDirectConnectionByHeartBeat(String deviceSsid, String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1000 deviceSsid=" + deviceSsid);
        if (!checkIsApDirectConnectionMode()) {
            NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1001");
            if (listener != null) {
                listener.onResult(Constant.ERROR);
            }
            return;
        }
        NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1002");
        DeviceCmdApi.getInstance().apXHeartBeat(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1003 code=" + code);
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1004");
                    if (listener != null) {
                        listener.onResult(code);
                    }
                } else if (mCheckApDirectHeartBeatCount <= AP_CMD_RETRY_MAX_COUNT) {
                    NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1005");
                    mCheckApDirectHeartBeatCount++;
                    checkApDirectConnectionByHeartBeat(deviceSsid, deviceId, listener);
                } else {
                    NooieLog.d("-->> debug ApHelper checkApDirectConnectionByHeartBeat: 1006");
                    if (listener != null) {
                        listener.onResult(code);
                    }
                }
            }
        });
    }

    public void setCheckApDirectHeartBeatCount(int count) {
        mCheckApDirectHeartBeatCount = count;
    }

    private void sendApHeartBeat(String deviceId, OnActionResultListener listener) {
        NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1000 deviceId=" + deviceId + " mCheckIsSendingApHeartBeat=" + checkIsSendingApHeartBeat() + " mSendApHeartBeatCount=" + mSendApHeartBeatCount);
        DeviceCmdApi.getInstance().apXHeartBeat(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1001 code=" + code);
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1002");
                    setLastSendingApHeartBeat(0);
                    if (listener != null) {
                        listener.onResult(code);
                    }
                } else if (mSendApHeartBeatCount <= AP_CMD_RETRY_MAX_COUNT) {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1010");
                    mSendApHeartBeatCount++;
                    sendApHeartBeat(deviceId, listener);
                } else {
                    NooieLog.d("-->> debug ApHelper sendApHeartBeat: 1011");
                    setLastSendingApHeartBeat(0);
                    if (listener != null) {
                        listener.onResult(code);
                    }
                }
            }
        });
    }

    private boolean checkIsSendingApHeartBeat() {
        return System.currentTimeMillis() - mLastSendingApHeartBeat < SENDING_AP_HEART_BEAT_LIMIT_TIME;
    }

    private boolean checkIsSendingApHeartBeatTimeOut() {
        return mLastSendingApHeartBeat > 0 && !checkIsSendingApHeartBeat();
    }

    private void setLastSendingApHeartBeat(long time) {
        mLastSendingApHeartBeat = time;
    }

    public void addListener(ApHelperListener listener) {
        if (listener == null) {
            return;
        }
        if (mApHelperListeners == null) {
            mApHelperListeners = new ArrayList<>();
        }
        mApHelperListeners.add(listener);
    }

    public void removeListener(ApHelperListener listener) {
        if (listener == null || CollectionUtil.isEmpty(mApHelperListeners)) {
            return;
        }
        if (mApHelperListeners.contains(listener)) {
            mApHelperListeners.remove(listener);
        }
    }

    public void clearListener() {
        if (CollectionUtil.isEmpty(mApHelperListeners)) {
            return;
        }
        mApHelperListeners.clear();
    }

    private void notifyApHeartBeatResponse(int code) {
        if (CollectionUtil.isEmpty(mApHelperListeners)) {
            return;
        }
        for (ApHelperListener listener : CollectionUtil.safeFor(mApHelperListeners)) {
            if (listener != null) {
                listener.onApHeartBeatResponse(code);
            }
        }
    }

    public void notifyNetworkChange() {
        if (CollectionUtil.isEmpty(mApHelperListeners)) {
            return;
        }
        for (ApHelperListener listener : CollectionUtil.safeFor(mApHelperListeners)) {
            if (listener != null) {
                listener.onNetworkChange();
            }
        }
    }

    public void checkNetworkWhenChanged(Observer<NetworkChangeResult> observer) {
        NooieLog.d("-->> debug ApHelper checkNetworkWhenChanged: 1000");
        stopCheckNetworkWhenChangedTask();
        mCheckNetworkWhenChangedTask = Observable.just(1)
                .flatMap(new Func1<Integer, Observable<NetworkChangeResult>>() {
                    @Override
                    public Observable<NetworkChangeResult> call(Integer integer) {
                        NooieLog.d("-->> debug ApHelper checkNetworkWhenChanged: 1001");
                        NetworkChangeResult result = new NetworkChangeResult();
                        boolean isWifiConnected = NetworkUtil.isWifiConnected(getContext());
                        result.setIsConnected(isWifiConnected);
                        NooieLog.d("-->> debug ApHelper checkNetworkWhenChanged: 1002 isWifiConnected=" + isWifiConnected);
                        if (!isWifiConnected) {
                            return Observable.just(result);
                        }
                        String ssid = NetworkUtil.getSSIDAuto(getContext());
                        result.setSsid(ssid);
                        NooieLog.d("-->> debug ApHelper checkNetworkWhenChanged: 1003 ssid=" + ssid);
                        return Observable.just(result);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer != null ? observer : new Observer<NetworkChangeResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(NetworkChangeResult result) {
                    }
                });
    }

    public void stopCheckNetworkWhenChangedTask() {
        NooieLog.d("-->> debug ApHelper stopCheckNetworkWhenChangedTask: 1000");
        if (mCheckNetworkWhenChangedTask != null && !mCheckNetworkWhenChangedTask.isUnsubscribed()) {
            mCheckNetworkWhenChangedTask.unsubscribe();
            mCheckNetworkWhenChangedTask = null;
        }
    }

    public BindDevice getDevice(String deviceId, String model) {
        BindDevice device = new BindDevice();
        device.setUuid(!TextUtils.isEmpty(deviceId) ? deviceId : ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
        device.setType(model);
        device.setModel(model);
        device.setBind_type(ApiConstant.BIND_TYPE_OWNER);
        device.setOnline(ApiConstant.ONLINE_STATUS_ON);
        device.setOpen_status(ApiConstant.OPEN_STATUS_ON);
        device.setName(model);
        device.setPuuid(ConstantValue.NORMAL_DEVICE_PUUID);
        return device;
    }

    public ApDeviceInfo getApDeviceCache(String deviceSsid) {
        return ApDeviceInfoCache.getInstance().getCacheById(deviceSsid);
    }

    public void updateApDeviceCache(String deviceSsid, Bundle data) {
        ApDeviceInfoCache.getInstance().updateApDeviceCache(deviceSsid, data);
    }

    public void updateOpenStatusInApDeviceCache(String deviceSsid, int openStatus) {
        Bundle data = new Bundle();
        data.putInt(ApDeviceInfoCache.AP_DEVICE_KEY_OPEN_STATUS, openStatus);
        ApDeviceInfoCache.getInstance().updateApDeviceCache(deviceSsid, data);
    }

    public void updateCurrentApDeviceInfoOfOpenStatus(String deviceId, int openStatus) {
        Bundle data = new Bundle();
        data.putString(KEY_PARAM_DEVICE_ID, deviceId);
        data.putInt(INTENT_KEY_OPEN_STATUS, openStatus);
        updateCurrentApDeviceInfo(data);
    }

    public void updateCurrentApDeviceInfo(Bundle data) {
        if (mPresenter != null) {
            mPresenter.updateCurrentApDeviceInfo(data);
        }
    }

    public void addApDeviceCacheListener(OnUpdateApDeviceCacheListener listener) {
        ApDeviceInfoCache.getInstance().addApDeviceCacheListener(listener);
    }

    public void removeApDeviceCacheListener(OnUpdateApDeviceCacheListener listener) {
        ApDeviceInfoCache.getInstance().removeApDeviceCacheListener(listener);
    }

    public void clearApDeviceCacheListener() {
        ApDeviceInfoCache.getInstance().clearApDeviceCacheListener();
    }

    public void loadApDevice(String deviceId, String deviceSsid, LoadApDeviceListener listener) {
        NooieLog.d("-->> debug ApHelper loadApDevice: 1000 deviceSsid=" + deviceSsid);
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        NooieLog.d("-->> debug ApHelper loadApDevice: 1001");
                        String ssid = TextUtils.isEmpty(deviceSsid) ? NetworkUtil.getSSIDAuto(getContext()) : deviceSsid;
                        NooieLog.d("-->> debug ApHelper loadApDevice: 1002 ssid=" + ssid);
                        ssid = TextUtils.isEmpty(ssid) ? deviceId : ssid;
                        return Observable.just(ssid);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug ApHelper loadApDevice: 1003");
                        if (listener != null) {
                            listener.onLoadDevice(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(String deviceSsid) {
                        NooieLog.d("-->> debug ApHelper loadApDevice: 1004 deviceSsid=" + deviceSsid);
                        if (TextUtils.isEmpty(deviceSsid) || !NooieDeviceHelper.checkApFutureCode(deviceSsid)) {
                            if (listener != null) {
                                listener.onLoadDevice(SDKConstant.ERROR, null);
                            }
                            return;
                        }
                        getApDeviceConfigure(deviceSsid, deviceId, listener);
                    }
                });
    }

    public void updateBleApDevice(boolean isSync, String user, String deviceId, Bundle data, Observer<Boolean> observer) {
        if (mPresenter != null) {
            mPresenter.updateBleApDevice(isSync, user, deviceId, data, observer);
        }
    }

    public void removeBleApDeviceInfoCache(boolean isSync, String user, String deviceId, Observer<Boolean> observer) {
        if (mPresenter != null) {
            mPresenter.removeBleApDeviceInfoCache(isSync, user, deviceId, observer);
        }
    }

    public void setupApDeviceTime(String deviceId, String model, long timeStamp, OnActionResultListener listener) {
        if (mPresenter != null) {
            mPresenter.setupApDeviceTime(deviceId, model, timeStamp, listener);
        }
    }

    private void getApDeviceConfigure(String deviceSsid, String deviceId, LoadApDeviceListener listener) {
        NooieLog.d("-->> debug ApHelper getApDeviceConfigure: 1000 deviceSsid=" + deviceSsid);
        if (TextUtils.isEmpty(deviceSsid)) {
            if (listener != null) {
                listener.onLoadDevice(SDKConstant.ERROR, null);
            }
            return;
        }
        DeviceCmdApi.getInstance().getDeviceSetting(deviceId, new OnGetDeviceSetting() {
            @Override
            public void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting) {
                NooieLog.d("-->> debug ApHelper getApDeviceConfigure: 1001 code=" + code);
                BindDevice device = getDevice(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, IpcType.MC120.getType());
                updateApDeviceCache(deviceSsid, device);
                if (code == Constant.OK) {
                    NooieLog.d("-->> debug ApHelper getApDeviceConfigure: 1002");
                    updateApDeviceCache(deviceSsid, complexSetting.getDevAllSettings());
                }
                NooieLog.d("-->> debug ApHelper getApDeviceConfigure: 1003");
                DeviceCmdApi.getInstance().getDevInfo(deviceId, new OnGetDevInfoListener() {
                    @Override
                    public void onDevInfo(int code, DevInfo devInfo) {
                        NooieLog.d("-->> debug ApHelper getApDeviceConfigure: 1004 code=" + code);
                        if (code == Constant.OK) {
                            updateApDeviceCache(deviceSsid, devInfo);
                        }
                        if (listener != null) {
                            listener.onLoadDevice(SDKConstant.SUCCESS, ApDeviceInfoCache.getInstance().getCacheById(deviceSsid));
                        }
                    }
                });
            }
        });
    }

    private void updateApDeviceCache(String deviceSsid, BindDevice device) {
        ApDeviceInfoCache.getInstance().updateApDeviceCache(deviceSsid, device);
    }

    private void updateApDeviceCache(String deviceSsid, DevAllSettingsV2 devAllSettingsV2) {
        ApDeviceInfoCache.getInstance().updateApDeviceCache(deviceSsid, devAllSettingsV2);
    }

    private void updateApDeviceCache(String deviceSsid, DevInfo devInfo) {
        ApDeviceInfoCache.getInstance().updateApDeviceCache(deviceSsid, devInfo);
    }

    private void updateCurrentDvDeviceId(String deviceId) {
        mCurrentDvDeviceId = deviceId;
    }

    private void getDeviceApState(String ssid, CheckApDirectConnectionListener listener) {
        NooieLog.d("-->> debug ApHelper getDeviceApState: 1000");
        DeviceCmdApi.getInstance().getAPPairStatus(new OnAPPairStatusResultListener() {
            @Override
            public void onResult(int code, APPairStatus status, String uuid) {
                NooieLog.d("-->> debug ApHelper getDeviceApState: 1001 code=" + code + " status=" + status + " deviceId=" + uuid);
                if (listener != null) {
                    listener.onCheckResult((code == Constant.OK), CHECK_AP_DIRECT_CONNECTION_RESULT_GET_AP_STATUS, ssid, status, uuid);
                }
            }
        });
    }

    private void switchDeviceCmdType(int connectionMode) {
        NooieLog.d("-->> debug ApHelper switchDeviceCmdType: 1000");
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            List<String> deviceIds = new ArrayList<>();
            deviceIds.add(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            boolean result = DeviceCmdApi.getInstance().apNewConn(deviceIds);
            NooieLog.d("-->> debug ApHelper switchDeviceCmdType: 1001 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_AP_DIRECT);
        } else {
            boolean result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            NooieLog.d("-->> debug ApHelper switchDeviceCmdType: 1002 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_NONE);
        }
    }

    public void registerAppStateListener() {
        if (mAppStateManagerListener == null) {
            mAppStateManagerListener = new CustomAppStateManagerListener();
        }
        AppStateManager.getInstance().addListener(mAppStateManagerListener);
    }

    public void unRegisterAppStateListener() {
        if (mAppStateManagerListener != null) {
            AppStateManager.getInstance().removeListener(mAppStateManagerListener);
        }
    }

    public void setIsEnterApDevicePage(boolean isEnterApDevicePage) {
        mIsEnterApDevicePage = isEnterApDevicePage;
    }

    public boolean getIsEnterApDevicePage() {
        return mIsEnterApDevicePage;
    }

    public String getCurrentRegion() {
        String region = GlobalData.getInstance().getRegion();
        if (TextUtils.isEmpty(region)) {
            region = CountryUtil.getCurrentCountryKey(NooieApplication.mCtx);
            region = region != null && region.equalsIgnoreCase(CountryUtil.DEFAULT_COUNTRY_KEY) ? region : CConstant.REGION_EU;
        }
        return region;
    }

    private Context getContext() {
        return NooieApplication.mCtx;
    }

    public class APPairStatusResultListener implements OnAPPairStatusResultListener {

        private WeakReference<QueryApPairStatusListener> mListenerRef;

        public void setListener(QueryApPairStatusListener listener) {
            mListenerRef = new WeakReference<QueryApPairStatusListener>(listener);
        }

        @Override
        public void onResult(int code, APPairStatus status, String uuid) {
            NooieLog.d("-->> APPairStatusResultListener onResult code=" + code + " deviceId=" + uuid + " status=" + (status != null ? status.getIntValue() : "null"));
            if (mListenerRef != null && mListenerRef.get() != null) {
                mListenerRef.get().onQueryAPPairStatus(code, status);
            }
        }
    }

    private class CustomAppStateManagerListener implements AppStateManager.AppStateManagerListener {

        @Override
        public void onAppBackground() {
            NooieLog.d("-->> debug ApHelper onAppBackground");
            checkBleApDeviceConnectionBackgroundKeepingTask(true, null);
        }

        @Override
        public void onAppForeground() {
            NooieLog.d("-->> debug ApHelper onAppForeground");
            checkBleApDeviceConnectionBackgroundKeepingTask(false, null);
        }
    }

    public interface ApPairListener {
        void onAPPairResult(int state, int code);
    }

    public interface QueryApPairStatusListener {
        void onQueryAPPairStatus(int code, APPairStatus status);
    }

    public interface APDirectListener {
        void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId);
    }

    public interface CheckApDirectConnectionListener {
        void onCheckResult(boolean result, int resultType, String ssid, APPairStatus status, String uuid);
    }

    public interface ApHelperListener {
        void onApHeartBeatResponse(int code);

        void onNetworkChange();
    }

    public interface LoadApDeviceListener {
        void onLoadDevice(int state, ApDeviceInfo device);
    }

    public interface OnUpdateApDeviceCacheListener {
        void onUpdateCache(String key, ApDeviceInfo apDeviceInfo);
    }

    public interface BleApConnectionFrontKeepingListener {

        void onResult(int state, Bundle param);
    }

    public interface BleApConnectionBackgroundKeepingListener {

        void onResult(int state, Bundle param);
    }
}
