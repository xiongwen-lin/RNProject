package com.afar.osaio.smart.scan.helper.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.smart.scan.helper.contract.ApHelperContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.nooie.sdk.db.dao.BleApDeviceService;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.APNetCfg;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.DeviceConnInfo;
import com.nooie.sdk.device.bean.hub.CameraInfo;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ApHelperPresenter implements ApHelperContract.Presenter {

    private static final int BLE_AP_CONNECTION_KEEPING_TIME_LENGTH_FG = 4 * 60 * 1000;
    private static final int BLE_AP_DEVICE_CONNECTION_BACKGROUND_KEEPING_TIME_LEN = 60 * 1000;//3 * 60 * 1000;

    private ApHelperContract.View mTaskView;
    private int mSetApDeviceInfoRetryCount = 1;
    private int mCheckApDeviceConnectRetryCount = 1;
    private int mCheckBleApDeviceConnectRetryCount = 1;
    private ApDeviceInfo mApDeviceInfo;
    private Subscription mStartBleApConnectionFrontKeepingTask = null;

    private ApHelper.BleApConnectionFrontKeepingListener mBleApConnectionFrontKeepingListener = null;
    private Subscription mCheckBleApDeviceConnectionBackgroundKeepingTask = null;
    private long mCheckBleApDeviceConnectBackgroundTime = 0;
    private boolean mLastApDirectConnectingExist = false;

    public ApHelperPresenter(ApHelperContract.View view) {
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
    public void updateBleApDevice(boolean isSync, String user, String deviceId, Bundle data, Observer<Boolean> observer) {
        if (isSync) {
            BleApDeviceService.getInstance().updateDevice(user, deviceId, data);
            return;
        }
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        BleApDeviceService.getInstance().updateDevice(user, deviceId, data);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer != null ? observer : new Observer<Boolean>() {
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

    private void updateBleApDevice(boolean isSync, String user, Bundle data, DevInfo devInfo, Observer<Boolean> observer) {
        if (devInfo == null) {
            return;
        }
        if (data == null) {
            data = new Bundle();
        }
        data.putString(BleApDeviceService.KEY_MODEL, devInfo.model);
        updateBleApDevice(isSync, user, devInfo.uuid, data, observer);
    }

    private void updateBleApDevice(boolean isSync, String user, Bundle data, CameraInfo devInfo, Observer<Boolean> observer) {
        if (devInfo == null) {
            return;
        }
        if (data == null) {
            data = new Bundle();
        }
        if (devInfo != null && !TextUtils.isEmpty(devInfo.softVer)) {
            data.putString(BleApDeviceService.KEY_VERSION, devInfo.softVer);
        }
        updateBleApDevice(isSync, user, devInfo.uuid, data, observer);
    }

    private void updateBleApDeviceInfoCache(boolean isSync, String user, String deviceId, Observer<Boolean> observer) {
        if (isSync) {
            BleApDeviceEntity bleApDeviceEntity = BleApDeviceService.getInstance().getDevice(deviceId);
            BleApDeviceInfoCache.getInstance().updateApDeviceCache(deviceId, bleApDeviceEntity);
            return;
        }
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        BleApDeviceEntity bleApDeviceEntity = BleApDeviceService.getInstance().getDevice(deviceId);
                        BleApDeviceInfoCache.getInstance().updateApDeviceCache(deviceId, bleApDeviceEntity);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer != null ? observer : new Observer<Boolean>() {
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
    public void removeBleApDeviceInfoCache(boolean isSync, String user, String deviceId, Observer<Boolean> observer) {
        if (isSync) {
            BleApDeviceService.getInstance().deleteDevice(deviceId);
            BleApDeviceInfoCache.getInstance().removeCacheById(deviceId);
            return;
        }
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        BleApDeviceService.getInstance().deleteDevice(deviceId);
                        BleApDeviceInfoCache.getInstance().removeCacheById(deviceId);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer != null ? observer : new Observer<Boolean>() {
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
    public void switchApDirectConnectMode(Bundle data, ApHelper.APDirectListener listener) {
        if (getDeviceType(data) == ConstantValue.AP_DEVICE_TYPE_BLE_LP) {
            switchBluetoothApConnectMode(getConnectionMode(data), getSsid(data), getDefaultPw(data), getDeviceId(data), getServer(data), getPort(data), ConstantValue.AP_DEVICE_TYPE_BLE_LP, getDeviceModel(data), getBleDevice(data), listener);
        } else if (getDeviceType(data) == ConstantValue.AP_DEVICE_TYPE_IPC) {
            switchApConnectMode(getConnectionMode(data), getSsid(data), ConstantValue.AP_DEVICE_TYPE_IPC, listener);
        } else if (listener != null) {
            listener.onSwitchConnectionMode(false, ConstantValue.CONNECTION_MODE_QC, null);
        }
    }

    @Override
    public void resetApDirectConnectMode(Bundle data, ApHelper.APDirectListener listener) {
        if (getDeviceType(data) == ConstantValue.AP_DEVICE_TYPE_BLE_LP) {
            stopBluetoothApDirectConnect(getConnectionMode(data), listener);
        } else if (getDeviceType(data) == ConstantValue.AP_DEVICE_TYPE_IPC) {
            stopApDirectConnect(getConnectionMode(data), listener);
        } else {
            stopNormalApDirectConnect(getConnectionMode(data), listener);
        }
    }

    @Override
    public void resetApDirectConnectMode(String model, ApHelper.APDirectListener listener) {
        Bundle param = new Bundle();
        param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ApHelper.getInstance().convertApDeviceType(model));
        param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
        resetApDirectConnectMode(param, listener);
    }

    @Override
    public void setupApDeviceTime(String deviceId, String model, long timeStamp, OnActionResultListener listener) {
        try {
            DeviceCmdService.getInstance(NooieApplication.mCtx).setUTCTimeStamp(DeviceCmdApi.getInstance().getApDeviceId(deviceId, model), timeStamp, listener);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
            if (listener != null) {
                listener.onResult(Constant.ERROR);
            }
        }
    }

    @Override
    public ApDeviceInfo getCurrentApDeviceInfo() {
        return mApDeviceInfo;
    }

    @Override
    public void updateCurrentApDeviceInfo(Bundle data) {
        if (data == null || mApDeviceInfo == null || mApDeviceInfo.getBindDevice() == null) {
            return;
        }
        String deviceId = getDeviceId(data);
        if (TextUtils.isEmpty(deviceId) || !deviceId.equalsIgnoreCase(mApDeviceInfo.getBindDevice().getUuid())) {
            return;
        }
        if (data.containsKey(ApHelper.INTENT_KEY_OPEN_STATUS)) {
            mApDeviceInfo.getBindDevice().setOpen_status(getOpenStatus(data));
        }
    }

    @Override
    public void startBleApConnectionFrontKeepingTask(Bundle param) {
        if (!NooieDeviceHelper.isBleApLpDevice(getDeviceModel(param), getConnectionMode(param)) || !checkBleApDeviceConnectingExist()) {
            stopBleApConnectionFrontKeepingTask();
            return;
        }
        if (!getIsRestartTask(param) && mStartBleApConnectionFrontKeepingTask != null && !mStartBleApConnectionFrontKeepingTask.isUnsubscribed()) {
            return;
        }
        stopBleApConnectionFrontKeepingTask();
        mStartBleApConnectionFrontKeepingTask = Observable.just(1)
                .delay(BLE_AP_CONNECTION_KEEPING_TIME_LENGTH_FG, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mBleApConnectionFrontKeepingListener != null) {
                            mBleApConnectionFrontKeepingListener.onResult(ApHelper.BLE_AP_CONNECTION_KEEPING_FRONT_STATE_ERROR, param);
                        }
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (mBleApConnectionFrontKeepingListener != null) {
                            mBleApConnectionFrontKeepingListener.onResult(ApHelper.BLE_AP_CONNECTION_KEEPING_FRONT_STATE_TIME_OUT, param);
                        }
                    }
                });
    }

    @Override
    public void stopBleApConnectionFrontKeepingTask() {
        if (mStartBleApConnectionFrontKeepingTask != null && !mStartBleApConnectionFrontKeepingTask.isUnsubscribed()) {
            mStartBleApConnectionFrontKeepingTask.unsubscribe();
            mStartBleApConnectionFrontKeepingTask = null;
        }
    }

    @Override
    public void setBleApDeviceConnectionFrontKeepingListener(ApHelper.BleApConnectionFrontKeepingListener listener) {
        mBleApConnectionFrontKeepingListener = listener;
    }

    @Override
    public boolean checkBleApDeviceConnectingExist() {
        return DeviceCmdApi.getInstance().checkIsApDirectCmdType() && getCurrentApDeviceInfo() != null;
    }

    @Override
    public void checkBleApDeviceConnectionBackgroundKeepingTask(boolean isBackground, ApHelper.BleApConnectionBackgroundKeepingListener listener) {
        NooieLog.d("-->> debug ApHelperPresenter checkBleApDeviceConnectionBackgroundKeepingTask: 1001 isBackground=" + isBackground);
        if (!isBackground) {
            boolean isCancelTask = System.currentTimeMillis() - mCheckBleApDeviceConnectBackgroundTime < BLE_AP_DEVICE_CONNECTION_BACKGROUND_KEEPING_TIME_LEN;
            NooieLog.d("-->> debug ApHelperPresenter checkBleApDeviceConnectionBackgroundKeepingTask: 1002 isBackground=" + isBackground + " isCancelTask" + isCancelTask);
            if (isCancelTask) {
                mCheckBleApDeviceConnectBackgroundTime = 0;
                stopBleApDeviceConnectionBackgroundKeepingTask();
            }
            return;
        }
        NooieLog.d("-->> debug ApHelperPresenter checkBleApDeviceConnectionBackgroundKeepingTask: 1003 isBackground=" + isBackground);
        stopBleApDeviceConnectionBackgroundKeepingTask();
        mCheckBleApDeviceConnectBackgroundTime = System.currentTimeMillis();
        mCheckBleApDeviceConnectionBackgroundKeepingTask = Observable.just(1)
                .delay(BLE_AP_DEVICE_CONNECTION_BACKGROUND_KEEPING_TIME_LEN, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Integer result) {
                        NooieLog.d("-->> debug ApHelperPresenter checkBleApDeviceConnectionBackgroundKeepingTask: 1004 isBackground=" + isBackground);
                        NooieLog.d("-->> debug turn off hot spot 挂后台超过一分钟，尝试断开直连");
                        mLastApDirectConnectingExist = checkBleApDeviceConnectingExist();
                        tryToRemoveBleApDirectConnection();
                    }
                });
    }

    @Override
    public void stopBleApDeviceConnectionBackgroundKeepingTask() {
        if (mCheckBleApDeviceConnectionBackgroundKeepingTask != null && !mCheckBleApDeviceConnectionBackgroundKeepingTask.isUnsubscribed()) {
            NooieLog.d("-->> debug ApHelperPresenter stopBleApDeviceConnectionBackgroundKeepingTask: 1001");
            mCheckBleApDeviceConnectionBackgroundKeepingTask.unsubscribe();
            mCheckBleApDeviceConnectionBackgroundKeepingTask = null;
        }
    }

    @Override
    public void removeBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener) {
        if (!checkBleApDeviceConnectingExist() || param == null) {
            if (listener != null) {
                listener.onSwitchConnectionMode(false, ConstantValue.CONNECTION_MODE_QC, null);
            }
            return;
        }
        DeviceCmdApi.getInstance().unbindDevice(getDeviceId(param), getUid(param), new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (param != null) {
                    param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ApHelper.getInstance().convertApDeviceType(getDeviceModel(param)));
                    param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
                }
                if (!getIsRemoveCache(param)) {
                    resetApDirectConnectMode(getDeviceModel(param), listener);
                    return;
                }
                removeBleApDeviceInfoCache(false, getAccount(param), getDeviceId(param), new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        resetApDirectConnectMode(getDeviceModel(param), listener);
                    }

                    @Override
                    public void onNext(Boolean result) {
                        resetApDirectConnectMode(getDeviceModel(param), listener);
                    }
                });
            }
        });
    }

    @Override
    public void disconnectBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug ApHelperPresenter disconnectBleApDeviceConnection: 1001");
        if (!checkBleApDeviceConnectingExist() || param == null) {
            if (listener != null) {
                listener.onSwitchConnectionMode(false, ConstantValue.CONNECTION_MODE_QC, null);
            }
            return;
        }
        NooieLog.d("-->> debug turn off hot spot 发送断开热点命令");
        NooieLog.d("-->> debug ApHelperPresenter disconnectBleApDeviceConnection: 1001 deviceId=" + getDeviceId(param));
        DeviceCmdApi.getInstance().setWiFiStatus(getDeviceId(param), false, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelperPresenter disconnectBleApDeviceConnection: 1001 code=" + code);
                resetApDirectConnectMode(getDeviceModel(param), listener);
            }
        });
    }

    public void setLastApDirectConnectingExist(boolean exist) {
        mLastApDirectConnectingExist = exist;
    }

    public boolean getLastApDirectConnectingExist() {
        return mLastApDirectConnectingExist;
    }

    private void tryToRemoveBleApDirectConnection() {
        NooieLog.d("-->> debug ApHelperPresenter tryToRemoveBleApDirectConnection: 1001");
        if (!checkBleApDeviceConnectingExist()) {
            return;
        }
        NooieLog.d("-->> debug ApHelperPresenter tryToRemoveBleApDirectConnection: 1002");
        ApDeviceInfo apDeviceInfo = getCurrentApDeviceInfo();
        if (apDeviceInfo == null || apDeviceInfo.getBindDevice() == null) {
            NooieLog.d("-->> debug ApHelperPresenter tryToRemoveBleApDirectConnection: 1003");
            Bundle param = new Bundle();
            param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ConstantValue.AP_DEVICE_TYPE_BLE_LP);
            param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            resetApDirectConnectMode(param, new ApHelper.APDirectListener() {
                @Override
                public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                }
            });
            return;
        }
        NooieLog.d("-->> debug ApHelperPresenter tryToRemoveBleApDirectConnection: 1004");
        String deviceId = apDeviceInfo.getBindDevice().getUuid();
        String model = apDeviceInfo.getBindDevice().getModel();
        DeviceCmdApi.getInstance().apXHeartBeat(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> debug ApHelperPresenter tryToRemoveBleApDirectConnection: 1005 code=" + code);
                if (code == Constant.OK) {
                    Bundle param = new Bundle();
                    param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, model);
                    param.putString(ApHelper.KEY_PARAM_DEVICE_ID, deviceId);
                    NooieLog.d("-->> debug turn off hot spot 挂后台超过一分钟，尝试断开直连；设备链接正常，开始发送关闭热点命令");
                    disconnectBleApDeviceConnection(param, new ApHelper.APDirectListener() {
                        @Override
                        public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                        }
                    });
                } else {
                    Bundle param = new Bundle();
                    param.putString(ApHelper.KEY_PARAM_DEVICE_ID, deviceId);
                    param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, model);
                    param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ConstantValue.AP_DEVICE_TYPE_BLE_LP);
                    param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
                    resetApDirectConnectMode(param, new ApHelper.APDirectListener() {
                        @Override
                        public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                        }
                    });
                }
            }
        });
    }

    private void switchBluetoothApConnectMode(int connectionMode, String deviceSsid, String defaultPw, String deviceId, String server, int port, int deviceType, String model, String bleDeviceAddress, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1000 connectionMode=" + connectionMode);
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            DeviceConnectionHelper.getInstance().removeConnectionsForAp();
            DeviceConnectionCache.getInstance().clearCache();
            boolean result = DeviceCmdApi.getInstance().apNewConnect(convertApDeviceConnectInfo(deviceId, server, port));
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1001 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_AP_DIRECT);
            DeviceCmdApi.getInstance().setApDeviceId(deviceId);
            //startSendHeartBeat(deviceId, null);
            mCheckBleApDeviceConnectRetryCount = 1;
            checkBluetoothApDeviceConnect(deviceId, model, new OnGetDeviceSetting() {
                @Override
                public void onGetDeviceSetting(int state, DeviceComplexSetting deviceComplexSetting) {
                    CameraInfo cameraInfo = deviceComplexSetting != null ? deviceComplexSetting.getCameraInfo() : null;
                    Bundle data = new Bundle();
                    data.putString(BleApDeviceService.KEY_SSID, deviceSsid);
                    data.putInt(BleApDeviceService.KEY_DEVICE_TYPE, deviceType);
                    data.putString(BleApDeviceService.KEY_MODEL, model);
                    data.putString(BleApDeviceService.KEY_BLE_DEVICE_ID, bleDeviceAddress);
                    dealForCheckBluetoothApDeviceConnect(state, connectionMode, "", cameraInfo, data, listener);
                }
            });
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1003");
        } else {
            stopBluetoothApDirectConnect(connectionMode, listener);
        }
    }

    private void stopBluetoothApDirectConnect(int connectionMode, ApHelper.APDirectListener listener) {
        boolean result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.DEFAULT_UUID_AP_P2P);
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1004 result=" + result);
        DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_NONE);
        DeviceCmdApi.getInstance().setApDeviceId(null);
        mApDeviceInfo = null;
        Bundle data = new Bundle();
        data.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED, data);
        if (listener != null) {
            listener.onSwitchConnectionMode(result, connectionMode, null);
        }
        stopSendHeartBeat();
    }

    private List<DeviceConnInfo> convertApDeviceConnectInfo(String deviceId, String server, int port) {
        List<DeviceConnInfo> deviceConnInfos = new ArrayList<>();
        DeviceConnInfo deviceConnInfo = new DeviceConnInfo();
        deviceConnInfo.setUuid(deviceId);
        deviceConnInfo.setApConnType(Constant.AP_CONN_TYPE_P2P_AP);
        deviceConnInfo.setHbServer(server);
        deviceConnInfo.setHbPort(port);
        //set null
        deviceConnInfo.setSecret("");
        deviceConnInfo.setUserName("");
        deviceConnInfos.add(deviceConnInfo);
        return deviceConnInfos;
    }

    private void checkBluetoothApDeviceConnect(String deviceId, String model, OnGetDeviceSetting listener) {
        DeviceCmdApi.getInstance().getDeviceSetting(deviceId, model, new OnGetDeviceSetting() {
            @Override
            public void onGetDeviceSetting(int code, DeviceComplexSetting deviceComplexSetting) {
                if (listener == null) {
                    return;
                }
                if (code == Constant.OK) {
                    listener.onGetDeviceSetting(SDKConstant.SUCCESS, deviceComplexSetting);
                } else if (mCheckBleApDeviceConnectRetryCount <= ApHelper.AP_CMD_RETRY_MAX_COUNT) {
                    mCheckBleApDeviceConnectRetryCount++;
                    checkBluetoothApDeviceConnect(deviceId, model, listener);
                } else {
                    listener.onGetDeviceSetting(SDKConstant.ERROR, deviceComplexSetting);
                }
            }
        });
    }

    private void dealForCheckBluetoothApDeviceConnect(int state, int connectionMode, String user, CameraInfo devInfo, Bundle data, ApHelper.APDirectListener listener) {
        if (state == SDKConstant.SUCCESS && devInfo != null && !TextUtils.isEmpty(devInfo.uuid)) {
            updateBleApDevice(false, user, data, devInfo, new Observer<Boolean>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onNext(Boolean result) {
                    updateBleApDeviceInfoCache(false, user, devInfo.uuid, null);
                }
            });
            updateBleApDeviceInfoCache(false, user, devInfo.uuid, null);
            saveCurrentApDeviceInfo(devInfo.uuid, devInfo, data);
            if (listener != null) {
                listener.onSwitchConnectionMode(true, connectionMode, devInfo.uuid);
            }
        } else {
            stopBluetoothApDirectConnect(ConstantValue.CONNECTION_MODE_QC, null);
            if (listener != null) {
                listener.onSwitchConnectionMode(false, connectionMode, null);
            }
        }
    }

    private void switchApConnectMode(int connectionMode, String deviceSsid, int deviceType, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1000 connectionMode=" + connectionMode);
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            DeviceConnectionHelper.getInstance().removeConnectionsForAp();
            DeviceConnectionCache.getInstance().clearCache();
            List<String> deviceIds = new ArrayList<>();
            deviceIds.add(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            boolean result = DeviceCmdApi.getInstance().apNewConn(deviceIds);
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1001 result=" + result);
            DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_AP_DIRECT);
            DeviceCmdApi.getInstance().setApDeviceId(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
            startSendHeartBeat(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, null);
            mSetApDeviceInfoRetryCount = 1;
            setApDeviceInfo(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> debug ApHelper switchApConnectMode: 1002 code=" + code);
                    if (code != Constant.OK) {
                        dealForCheckApDeviceConnect(code, null, connectionMode, "", null, listener);
                        return;
                    }
                    mCheckApDeviceConnectRetryCount = 1;
                    checkApDeviceConnect(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, new OnGetDevInfoListener() {
                        @Override
                        public void onDevInfo(int code, DevInfo devInfo) {
                            Bundle data = new Bundle();
                            data.putString(BleApDeviceService.KEY_SSID, deviceSsid);
                            data.putInt(BleApDeviceService.KEY_DEVICE_TYPE, deviceType);
                            dealForCheckApDeviceConnect(code, devInfo, connectionMode, "", data, listener);
                        }
                    });
                }
            });
            NooieLog.d("-->> debug ApHelper switchApConnectMode: 1003");
        } else {
            stopApDirectConnect(connectionMode, listener);
        }
    }

    private void stopApDirectConnect(int connectionMode, ApHelper.APDirectListener listener) {
        boolean result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
        NooieLog.d("-->> debug ApHelper switchApConnectMode: 1004 result=" + result);
        DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_NONE);
        DeviceCmdApi.getInstance().setApDeviceId(null);
        mApDeviceInfo = null;
        Bundle data = new Bundle();
        data.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED, data);
        if (listener != null) {
            listener.onSwitchConnectionMode(result, connectionMode, null);
        }
        stopSendHeartBeat();
    }

    private void setApDeviceInfo(String deviceId, OnActionResultListener listener) {
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
                            } else if (mSetApDeviceInfoRetryCount <= ApHelper.AP_CMD_RETRY_MAX_COUNT) {
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

    private void startSendHeartBeat(String deviceId, OnActionResultListener listener) {
        if (mTaskView !=null) {
            mTaskView.startSendHeartBeat(deviceId, listener);
        }
    }

    private void stopSendHeartBeat() {
        if (mTaskView != null) {
            mTaskView.stopSendHeartBeat();
        }
    }

    private void checkApDeviceConnect(String deviceId, OnGetDevInfoListener listener) {
        DeviceCmdApi.getInstance().getDevInfo(deviceId, new OnGetDevInfoListener() {
            @Override
            public void onDevInfo(int code, DevInfo devInfo) {
                if (listener == null) {
                    return;
                }
                if (code == Constant.OK) {
                    listener.onDevInfo(code, devInfo);
                } else if (mCheckApDeviceConnectRetryCount <= ApHelper.AP_CMD_RETRY_MAX_COUNT) {
                    mCheckApDeviceConnectRetryCount++;
                    checkApDeviceConnect(deviceId, listener);
                } else {
                    listener.onDevInfo(code, devInfo);
                }
            }
        });
    }

    private void dealForCheckApDeviceConnect(int code, DevInfo devInfo, int connectionMode, String user, Bundle data, ApHelper.APDirectListener listener) {
        if (code == Constant.OK && devInfo != null && !TextUtils.isEmpty(devInfo.uuid) && !TextUtils.isEmpty(devInfo.model)) {
            List<String> deviceIds = new ArrayList<>();
            deviceIds.add(devInfo.uuid);
            boolean result = DeviceCmdApi.getInstance().apNewConn(deviceIds);
            updateBleApDevice(false, user, data, devInfo, new Observer<Boolean>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onNext(Boolean result) {
                    updateBleApDeviceInfoCache(false, user, devInfo.uuid, null);
                }
            });
            updateBleApDeviceInfoCache(false, user, devInfo.uuid, null);
            saveCurrentApDeviceInfo(devInfo.uuid, devInfo, data);
            if (listener != null) {
                listener.onSwitchConnectionMode(true, connectionMode, devInfo.uuid);
            }
        } else {
            stopApDirectConnect(ConstantValue.CONNECTION_MODE_QC, null);
            if (listener != null) {
                listener.onSwitchConnectionMode(false, connectionMode, null);
            }
        }
    }

    private void stopNormalApDirectConnect(int connectionMode, ApHelper.APDirectListener listener) {
        boolean result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
        result = DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.DEFAULT_UUID_AP_P2P);
        NooieLog.d("-->> debug ApHelper stopNormalApDirectConnect: 1004 result=" + result);
        DeviceCmdApi.getInstance().setMainCmdType(DeviceCmdApi.CMD_TYPE_NONE);
        DeviceCmdApi.getInstance().setApDeviceId(null);
        mApDeviceInfo = null;
        Bundle data = new Bundle();
        data.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED, data);
        if (listener != null) {
            listener.onSwitchConnectionMode(result, connectionMode, null);
        }
        stopSendHeartBeat();
    }

    private String getCurrentRegion() {
        String region = GlobalData.getInstance().getRegion();
        if (TextUtils.isEmpty(region)) {
            region = CountryUtil.getCurrentCountryKey(NooieApplication.mCtx);
            region = region != null && region.equalsIgnoreCase(CountryUtil.DEFAULT_COUNTRY_KEY) ? region : CConstant.REGION_EU;
        }
        return region;
    }

    private void saveCurrentApDeviceInfo(String deviceId, CameraInfo info, Bundle data) {
        String model = data != null ? data.getString(BleApDeviceService.KEY_MODEL) : "";
        String deviceSsid = data != null ? data.getString(BleApDeviceService.KEY_SSID) : "";
        String bleDeviceId = data != null ? data.getString(BleApDeviceService.KEY_BLE_DEVICE_ID) : "";
        ApDeviceInfo apDeviceInfo = new ApDeviceInfo();
        BindDevice bindDevice = ApHelper.getInstance().getDevice(deviceId, model);
        bindDevice.setVersion(info.softVer);
        apDeviceInfo.setBindDevice(bindDevice);
        apDeviceInfo.setCameraInfo(info);
        apDeviceInfo.setDeviceId(deviceId);
        apDeviceInfo.setDeviceSsid(deviceSsid);
        apDeviceInfo.setBleDeviceId(bleDeviceId);
        mApDeviceInfo = apDeviceInfo;
    }

    private void saveCurrentApDeviceInfo(String deviceId, DevInfo info, Bundle data) {
        String model = info != null ? info.model : null;
        String deviceSsid = data != null ? data.getString(BleApDeviceService.KEY_SSID) : "";
        ApDeviceInfo apDeviceInfo = new ApDeviceInfo();
        BindDevice bindDevice = ApHelper.getInstance().getDevice(deviceId, model);
        apDeviceInfo.setBindDevice(bindDevice);
        apDeviceInfo.setDevInfo(info);
        apDeviceInfo.setDeviceId(deviceId);
        apDeviceInfo.setDeviceSsid(deviceSsid);
        mApDeviceInfo = apDeviceInfo;
    }

    private int getDeviceType(Bundle data) {
        if (data == null) {
            return 0;
        }
        return data.getInt(ApHelper.KEY_PARAM_DEVICE_TYPE, 0);
    }

    private int getConnectionMode(Bundle data) {
        if (data == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return data.getInt(ApHelper.KEY_PARAM_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private String getSsid(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_SSID);
    }

    private String getDefaultPw(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_DEFAULT_PW);
    }

    private String getDeviceId(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_DEVICE_ID);
    }

    private String getServer(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_SERVER);
    }

    private int getPort(Bundle data) {
        if (data == null) {
            return 80;
        }
        return data.getInt(ApHelper.KEY_PARAM_PORT, 80);
    }

    private String getDeviceModel(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_DEVICE_MODEL);
    }

    private String getBleDevice(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.INTENT_KEY_BLE_DEVICE);
    }

    private int getOpenStatus(Bundle data) {
        if (data == null) {
            return ApiConstant.OPEN_STATUS_ON;
        }
        return data.getInt(ApHelper.INTENT_KEY_OPEN_STATUS, ApiConstant.OPEN_STATUS_ON);
    }

    private boolean getIsRestartTask(Bundle data) {
        if (data == null) {
            return true;
        }
        return data.getBoolean(ApHelper.KEY_PARAM_IS_RESTART_TASK, true);
    }

    private String getUid(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_UID);
    }

    private String getAccount(Bundle data) {
        if (data == null) {
            return new String();
        }
        return data.getString(ApHelper.KEY_PARAM_ACCOUNT);
    }

    private boolean getIsRemoveCache(Bundle data) {
        if (data == null) {
            return false;
        }
        return data.getBoolean(ApHelper.KEY_PARAM_IS_REMOVE_CACHE, false);
    }
}
