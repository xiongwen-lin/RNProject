package com.afar.osaio.smart.device.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.bean.BleApDeviceInfo;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.device.helper.IpcDeviceManageHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/21 5:07 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class IpcDeviceManagePresenter implements IIpcDeviceManagePresenter {

    @Override
    public void checkBleApDeviceConnecting(OnCheckBleApDeviceConnecting callback) {
        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1001");
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1002");
            dealOnCheckBleApDeviceConnecting(false, callback);
            return;
        }
        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1003");
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<ApDeviceInfo>>() {
                    @Override
                    public Observable<ApDeviceInfo> call(Integer integer) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1004");
                        if (!NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {
                            NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1005");
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1006");
                        ApDeviceInfo apDeviceInfo = ApHelper.getInstance().getCurrentApDeviceInfo();
                        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
                            NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1007");
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1008");
                        String hotSpotSsid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                        boolean isHotSpotMatching = false;
                        if (NooieDeviceHelper.mergeIpcType(apDeviceInfo.getBindDevice().getType()) == IpcType.HC320) {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkBluetoothApFutureCode(hotSpotSsid, apDeviceInfo.getDeviceSsid());
                        } else {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkApFutureCode(hotSpotSsid);
                        }
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1009 hotSpotSsid=" + hotSpotSsid + " model=" + apDeviceInfo.getBindDevice().getType() + " isHotSpotMatching=" + isHotSpotMatching);
                        if (isHotSpotMatching) {
                            BleApDeviceInfo bleApDeviceInfo = BleApDeviceInfoCache.getInstance().getCacheById(apDeviceInfo.getDeviceId());
                            boolean isUpdateApDeviceName = bleApDeviceInfo != null && bleApDeviceInfo.getBindDevice() != null && !TextUtils.isEmpty(bleApDeviceInfo.getBindDevice().getName()) &&
                                    apDeviceInfo != null && apDeviceInfo.getBindDevice() != null;
                            if (isUpdateApDeviceName) {
                                apDeviceInfo.getBindDevice().setName(bleApDeviceInfo.getBindDevice().getName());
                            }
                            return Observable.just(apDeviceInfo);
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApDeviceInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1010");
                        dealOnCheckBleApDeviceConnecting(false, callback);
                    }

                    @Override
                    public void onNext(ApDeviceInfo result) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1011");
                        boolean isConnectable = ApHelper.getInstance().checkBleApDeviceConnectingExist() && result != null && result.getBindDevice() != null;
                        NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1003");
                        dealOnCheckBleApDeviceConnecting(isConnectable, callback);
                    }
                });
    }

    @Override
    public void checkApDirectWhenNetworkChange(OnCheckApDirectWhenNetworkChange callback) {
        ApHelper.getInstance().checkNetworkWhenChanged(new Observer<NetworkChangeResult>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onCheckApDirectWhenNetworkChange(IpcDeviceManageHelper.NET_SPOT_NORMAL);
                }
            }

            @Override
            public void onNext(NetworkChangeResult result) {
                NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1002");
                boolean isNeedToStopApDirectConnection = result != null && result.getIsConnected() && !TextUtils.isEmpty(result.getSsid())
                        && !(NooieDeviceHelper.checkApFutureCode(result.getSsid()) || NooieDeviceHelper.checkBluetoothApFutureCode(result.getSsid(), ""));
                if (!isNeedToStopApDirectConnection) {
                    NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1003");
                    if (callback != null) {
                        callback.onCheckApDirectWhenNetworkChange(IpcDeviceManageHelper.NET_SPOT_NORMAL);
                    }
                    return;
                }
                NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1004");
                stopAPDirectConnection("", new OnStopAPDirectConnection() {
                    @Override
                    public void onStopAPDirectConnection(int state) {
                        if (callback != null) {
                            callback.onCheckApDirectWhenNetworkChange(IpcDeviceManageHelper.NET_SPOT_DISCONNECTED);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid, IpcDeviceManagePresenter.OnCheckBeforeConnectBleDevice callback) {
        NooieLog.d("-->> debug SmartDeviceListPresenter checkBeforeConnectBleDevice: 1001");
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBeforeConnectBleDevice: 1001");
                        if (!NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {
                            NooieLog.d("-->> debug SmartDeviceListPresenter checkBeforeConnectBleDevice: 1001");
                            return Observable.just("");
                        }
                        String hotSpotSsid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                        boolean isHotSpotMatching = false;
                        if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkBluetoothApFutureCode(hotSpotSsid, ssid);
                        } else {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkApFutureCode(hotSpotSsid);
                        }
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBeforeConnectBleDevice: 1001 isHotSpotMatching=" + isHotSpotMatching);
                        return Observable.just((isHotSpotMatching ? hotSpotSsid : ""));
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
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (callback != null) {
                            callback.onCheckBeforeConnectBleDevice(SDKConstant.ERROR, false, bleDeviceId, model, ssid);
                        }
                    }

                    @Override
                    public void onNext(String hotSpotSsid) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (callback != null) {
                            boolean result = !TextUtils.isEmpty(hotSpotSsid);
                            callback.onCheckBeforeConnectBleDevice(SDKConstant.SUCCESS, result, bleDeviceId, model, ssid);
                        }
                    }
                });
    }

    @Override
    public void stopAPDirectConnection(String model, OnStopAPDirectConnection callback) {
        NooieLog.d("-->> debug SmartDeviceListPresenter stopAPDirectConnection: apConnectionExist=" + ApHelper.getInstance().checkBleApDeviceConnectingExist());
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            if (callback != null) {
                callback.onStopAPDirectConnection(SDKConstant.SUCCESS);
            }
            return;
        }
        tryToStopBleApConnection(model, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                if (callback != null) {
                    callback.onStopAPDirectConnection(SDKConstant.SUCCESS);
                }
            }
        });
    }

    @Override
    public void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean switchOn) {
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
//                if (code == Constant.OK) {
//                    boolean sleep = !switchOn;
//                    if (sleep == on) {
//                        if (mTaskView != null) {
//                            mTaskView.onUpdateApDeviceOpenStatus(SDKConstant.SUCCESS, deviceSsid, deviceId, switchOn);
//                        }
//                    } else {
//                        DeviceCmdApi.getInstance().setSleep(deviceId, sleep, new OnActionResultListener() {
//                            @Override
//                            public void onResult(int code) {
//                                if (mTaskView != null) {
//                                    mTaskView.onUpdateApDeviceOpenStatus(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, deviceSsid, deviceId, switchOn);
//                                }
//                            }
//                        });
//                    }
//                } else if (mTaskView != null) {
//                    mTaskView.onUpdateApDeviceOpenStatus(SDKConstant.ERROR, deviceSsid, deviceId, switchOn);
//                }
            }
        });
    }

    public interface OnCheckBleApDeviceConnecting {

        void onCheckBleApDeviceConnecting(String state);

    }

    public interface OnCheckApDirectWhenNetworkChange {

        void onCheckApDirectWhenNetworkChange(String state);

    }

    public interface OnCheckBeforeConnectBleDevice {

        void onCheckBeforeConnectBleDevice(int state, boolean result, String bleDeviceId, String model, String ssid);

    }

    public interface OnStopAPDirectConnection {

        void onStopAPDirectConnection(int state);

    }

    private void tryToStopBleApConnection(String model, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug SmartDeviceListPresenter tryToStopBleApConnection: model=" + model);
        try {
            ApHelper.getInstance().tryResetApConnectMode(model, listener);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void dealOnCheckBleApDeviceConnecting(boolean isConnectable, OnCheckBleApDeviceConnecting callback) {
        if (isConnectable) {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1004");
            if (callback != null) {
                callback.onCheckBleApDeviceConnecting(IpcDeviceManageHelper.NET_SPOT_CONNECTED);
            }
        } else {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1005");
            String model = "";
            if (ApHelper.getInstance().getCurrentApDeviceInfo() != null && ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice() != null) {
                model = ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice().getType();
            }
            NooieLog.d("-->> debug SmartDeviceListFragment checkBleApDeviceConnecting: 1002 model=" + model);
            stopAPDirectConnection(model, new OnStopAPDirectConnection() {
                @Override
                public void onStopAPDirectConnection(int state) {
                    if (callback != null) {
                        callback.onCheckBleApDeviceConnecting(IpcDeviceManageHelper.NET_SPOT_DISCONNECTED);
                    }
                }
            });
        }
    }

}
