package com.afar.osaio.smart.home.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.bean.BleApDeviceInfo;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.contract.SmartIpcDeviceContract;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.BindDeviceResult;
import com.nooie.sdk.api.network.base.bean.entity.DeviceStatusResult;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.nooie.sdk.db.dao.BleApDeviceService;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.DeviceEntity;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.device.DeviceApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

public class SmartIpcDevicePresenter implements SmartIpcDeviceContract.Presenter {

    private static final int PAGE_MAX_DEVICE_NUM = 100;

    private SmartIpcDeviceContract.View mTaskView;

    private int mNextPage = 1;
    private Subscription mRefreshTask = null;
    private Subscription mGetGatewayDeviceTask;

    public SmartIpcDevicePresenter(SmartIpcDeviceContract.View view) {
        mTaskView = view;
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView = null;
        }
    }

    @Override
    public void refreshIpcDevices(String account, String uid) {
        mNextPage = 1;
        if (mNextPage == 1) {
            DeviceInfoCache.getInstance().clearCacheMap();
        }
        stopRefreshIpcDevicesTask();
        //stopLoadMoreTask();
        mRefreshTask = Observable.just(account)
                .flatMap(new Func1<String, Observable<List<BleApDeviceEntity>>>() {
                    @Override
                    public Observable<List<BleApDeviceEntity>> call(String s) {
                        return getBleApDevice(account);
                    }
                })
                .flatMap(new Func1<List<BleApDeviceEntity>, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(List<BleApDeviceEntity> bleApDeviceEntities) {
                        List<DeviceEntity> deviceEntities = DeviceCacheService.getInstance().getDevices(account);
                        List<ListDeviceItem> deviceItems = NooieDeviceHelper.convertDeviceFromCache(deviceEntities);
                        DeviceListCache.getInstance().addDevices(deviceItems);
                        return getDevicesObservable(account, mNextPage, PAGE_MAX_DEVICE_NUM);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ListDeviceItem>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onLoadDeviceEnd(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<ListDeviceItem> listDeviceItems) {
                        if (mTaskView != null) {
                            mTaskView.onLoadDeviceEnd(SDKConstant.SUCCESS, listDeviceItems);
                        }
                        preConnectDevice(getAllIpcDeviceList(), uid);
                        updateDeviceInDb(account, listDeviceItems);
                        getGatewayDevices(account, uid);
                    }
                });
    }

    @Override
    public void stopRefreshIpcDevicesTask() {
        if (mRefreshTask != null && !mRefreshTask.isUnsubscribed()) {
            mRefreshTask.unsubscribe();
        }
    }

    @Override
    public List<SmartCameraDevice> getIpcDevices() {
        List<SmartCameraDevice> result = new ArrayList<>();
        result.addAll(CollectionUtil.safeFor(SmartDeviceHelper.sortSmartCameraDevice(SmartDeviceHelper.convertSmartCameraDeviceList(getAllIpcDeviceList()))));
        result.addAll(CollectionUtil.safeFor(SmartDeviceHelper.convertSmartCameraDeviceListOfBleAp(getAllBleApDeviceList())));
        return result;
    }

    @Override
    public void checkBleApDeviceConnecting() {
        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1001");
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1002");
            if (mTaskView != null) {
                mTaskView.onCheckBleApDeviceConnecting(SDKConstant.ERROR, null);
            }
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
                        if (mTaskView != null) {
                            mTaskView.onCheckBleApDeviceConnecting(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(ApDeviceInfo result) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1011");
                        if (mTaskView != null) {
                            mTaskView.onCheckBleApDeviceConnecting(SDKConstant.SUCCESS, result);
                        }
                    }
                });
    }

    @Override
    public void checkApDirectWhenNetworkChange() {
        ApHelper.getInstance().checkNetworkWhenChanged(new Observer<NetworkChangeResult>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (mTaskView != null) {
                    mTaskView.onCheckApDirectWhenNetworkChange(SDKConstant.ERROR, null);
                }
            }

            @Override
            public void onNext(NetworkChangeResult result) {
                if (mTaskView != null) {
                    mTaskView.onCheckApDirectWhenNetworkChange(SDKConstant.SUCCESS, result);
                }
            }
        });
    }

    @Override
    public void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid) {
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
                        if (mTaskView != null) {
                            mTaskView.onCheckBeforeConnectBleDevice(SDKConstant.ERROR, false, bleDeviceId, model, ssid);
                        }
                    }

                    @Override
                    public void onNext(String hotSpotSsid) {
                        NooieLog.d("-->> debug SmartDeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (mTaskView != null) {
                            boolean result = !TextUtils.isEmpty(hotSpotSsid);
                            mTaskView.onCheckBeforeConnectBleDevice(SDKConstant.SUCCESS, result, bleDeviceId, model, ssid);
                        }
                    }
                });
    }

    @Override
    public void stopAPDirectConnection(String model) {
        NooieLog.d("-->> debug SmartDeviceListPresenter stopAPDirectConnection: apConnectionExist=" + ApHelper.getInstance().checkBleApDeviceConnectingExist());
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            if (mTaskView != null) {
                mTaskView.onStopAPDirectConnection(SDKConstant.SUCCESS);
            }
            return;
        }
        tryToStopBleApConnection(model, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                if (mTaskView != null) {
                    mTaskView.onStopAPDirectConnection(SDKConstant.SUCCESS);
                }
            }
        });
    }

    @Override
    public void updateDeviceOpenStatus(String deviceId, boolean on) {
        DeviceService.getService().getDeviceStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onUpdateDeviceOpenStatusResult(ConstantValue.ERROR, deviceId, false);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        boolean isNeedSetSleep = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && (response.getData().getOpen_status() == ApiConstant.ONLINE_STATUS_ON ? true : false) != on;
                        if (isNeedSetSleep) {
                            DeviceCmdApi.getInstance().setSleep(deviceId, !on, new OnActionResultListener() {
                                @Override
                                public void onResult(int code) {
                                    if (code == Constant.OK && mTaskView != null) {
                                        mTaskView.onUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, on);
                                    } else if (mTaskView != null) {
                                        mTaskView.onUpdateDeviceOpenStatusResult(ConstantValue.ERROR, deviceId, false);
                                    }
                                }
                            });
                        } else if (mTaskView != null) {
                            mTaskView.onUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, on);
                        }
                    }
                });
    }

    @Override
    public void getDeviceOpenStatus(final String deviceId) {
        DeviceService.getService().getDeviceStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetDeviceOpenStatusResult("", "", false);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && mTaskView != null) {
                            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, response.getData().getOpen_status());
                            mTaskView.onGetDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, (response.getData().getOpen_status() == ApiConstant.OPEN_STATUS_ON ? true : false));
                        } else if (mTaskView != null) {
                            mTaskView.onGetDeviceOpenStatusResult("", "", false);
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
                if (code == Constant.OK) {
                    boolean sleep = !switchOn;
                    if (sleep == on) {
                        if (mTaskView != null) {
                            mTaskView.onUpdateApDeviceOpenStatus(SDKConstant.SUCCESS, deviceSsid, deviceId, switchOn);
                        }
                    } else {
                        DeviceCmdApi.getInstance().setSleep(deviceId, sleep, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTaskView != null) {
                                    mTaskView.onUpdateApDeviceOpenStatus(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, deviceSsid, deviceId, switchOn);
                                }
                            }
                        });
                    }
                } else if (mTaskView != null) {
                    mTaskView.onUpdateApDeviceOpenStatus(SDKConstant.ERROR, deviceSsid, deviceId, switchOn);
                }
            }
        });
    }

    @Override
    public void removeIpcDevice(String account, String deviceId) {
        DeviceService.getService().deleteDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code)) {
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
                            DeviceConnectionCache.getInstance().removeConnection(deviceId);
                        }
                        return Observable.just(response);
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
                            mTaskView.onDeleteIpcDeviceResult(ConstantValue.ERROR, deviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteIpcDeviceResult(response != null && response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR, deviceId);
                        }
                    }
                });
    }

    private Observable<List<ListDeviceItem>> getDevicesObservable(String account, int page, int pageSize) {
        return DeviceService.getService().getBindDevices(page, pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<BaseResponse<BindDeviceResult>, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(BaseResponse<BindDeviceResult> response) {
                        List<ListDeviceItem> deviceItems = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getPage_info() != null) {

                            int currentPage = response.getData().getPage_info().getCurrent_page() != 0 ? response.getData().getPage_info().getCurrent_page() : 0;
                            int totalPage = response.getData().getPage_info().getTotal_page() != 0 ? response.getData().getPage_info().getTotal_page() : 0;

                            if (currentPage == 1) {
                                mNextPage = 1;
                                DeviceListCache.getInstance().clearCacheDeviceIds();
                            }

                            if (currentPage == 0 || totalPage == 0 || mNextPage > totalPage) {
                                return Observable.just(deviceItems);
                            }

                            mNextPage++;

                            DeviceListCache.getInstance().addCacheDeviceIds(NooieDeviceHelper.getNooieDeviceIds(response.getData().getData()));
                            deviceItems.addAll(NooieDeviceHelper.convertNooieDevice(response.getData().getData()));
                            DeviceListCache.getInstance().addDevices(deviceItems);
                            DeviceCmdApi.getInstance().updateDeviceCmdParams(account, response.getData().getData());
                            if (mTaskView != null) {
                                mTaskView.onLoadDeviceSuccess(deviceItems);
                            }
                        } else if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            DeviceListCache.getInstance().clearCacheDeviceIds();
                        }
                        return Observable.just(deviceItems);
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<List<ListDeviceItem>, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(List<ListDeviceItem> deviceItems) {
                        DeviceListCache.getInstance().compareDevcieListCache(account);
                        return deviceItems.size() > 0 ? getAllDeviceConfig(account, deviceItems) : Observable.just(deviceItems);
                    }
                })
                .flatMap(new Func1<List<ListDeviceItem>, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(List<ListDeviceItem> deviceItems) {
                        DeviceListCache.getInstance().addDevices(deviceItems);
                        return Observable.just(deviceItems);
                    }
                })
                .onErrorReturn(new Func1<Throwable, List<ListDeviceItem>>() {
                    @Override
                    public List<ListDeviceItem> call(Throwable throwable) {
                        return null;
                    }
                });
    }

    private Observable<List<ListDeviceItem>> getAllDeviceConfig(String account, List<ListDeviceItem> devices) {
        //long testStartTime = System.currentTimeMillis();
        List<ListDeviceItem> ownerDeviceItems = new ArrayList<>();
        List<Observable<ListDeviceItem>> observables = new ArrayList<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            if (device == null) {
                continue;
            }
            int bindType = device.getBindDevice() != null ? device.getBindDevice().getBind_type() : device.getBindType();
            if (bindType == ApiConstant.BIND_TYPE_OWNER) {
                ownerDeviceItems.add(device);
            } else {
                Observable<BaseResponse<PackInfoResult>> checkDeviceCloudObservable = PackService.getService().getPackInfo(device.getDeviceId(), bindType)
                        .onErrorReturn(new Func1<Throwable, BaseResponse<PackInfoResult>>() {
                            @Override
                            public BaseResponse<PackInfoResult> call(Throwable throwable) {
                                return null;
                            }
                        });

                Observable<ListDeviceItem> loadDeviceConfigObservable = Observable.zip(Observable.just(device), checkDeviceCloudObservable, new Func2<ListDeviceItem, BaseResponse<PackInfoResult>, ListDeviceItem>() {
                    @Override
                    public ListDeviceItem call(ListDeviceItem listDeviceItem, BaseResponse<PackInfoResult> cloudInfoBaseResponse) {
                        if (listDeviceItem != null && cloudInfoBaseResponse != null && cloudInfoBaseResponse.getCode() == StateCode.SUCCESS.code && cloudInfoBaseResponse.getData() != null) {
                            float deviceTimezone = listDeviceItem.getBindDevice() != null ? listDeviceItem.getBindDevice().getZone() : CountryUtil.getCurrentTimeZone();
                            boolean isOpenCloud = NooieCloudHelper.isOpenCloud(cloudInfoBaseResponse.getData().getEnd_time(), deviceTimezone);
                            listDeviceItem.setOpenCloud(isOpenCloud);
                            listDeviceItem.updateOpenCloudDe(listDeviceItem.isOpenCloud());
                            DeviceApi.getInstance().updateConfigureDeviceInfo(false, account, listDeviceItem.getBindDevice(), cloudInfoBaseResponse.getData());
                        } else if (listDeviceItem != null) {
                            listDeviceItem.setOpenCloud(false);
                            listDeviceItem.updateOpenCloudDe(listDeviceItem.isOpenCloud());
                            DeviceApi.getInstance().updateConfigureDeviceInfo(false, account, listDeviceItem.getBindDevice(), null);
                        }
                        return listDeviceItem;
                    }
                });

                observables.add(loadDeviceConfigObservable);
            }
        }

        Observable<List<ListDeviceItem>> getShareDeviceItemsObservable = CollectionUtil.isNotEmpty(observables) ? Observable.zip(observables, new FuncN<List<ListDeviceItem>>() {
            @Override
            public List<ListDeviceItem> call(Object... args) {
                List<ListDeviceItem> result = new ArrayList<>();
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        result.add((ListDeviceItem) args[i]);
                    }
                }
                return result;
            }
        }) : Observable.just(new ArrayList<>());

        Observable<BaseResponse<List<PackInfoResult>>> getAllDevicePackInfoObservable = PackService.getService().getAllPackInfo()
                .onErrorReturn(new Func1<Throwable, BaseResponse<List<PackInfoResult>>>() {
                    @Override
                    public BaseResponse<List<PackInfoResult>> call(Throwable throwable) {
                        return null;
                    }
                });

        Observable<List<ListDeviceItem>> getOwnerDeviceItemsObservable = CollectionUtil.isNotEmpty(ownerDeviceItems) ? Observable.zip(Observable.just(ownerDeviceItems), getAllDevicePackInfoObservable, new Func2<List<ListDeviceItem>, BaseResponse<List<PackInfoResult>>, List<ListDeviceItem>>() {
            @Override
            public List<ListDeviceItem> call(List<ListDeviceItem> ownerDeviceItems, BaseResponse<List<PackInfoResult>> response) {
                Map<String, PackInfoResult> packInfoResultMap = new HashMap<>();
                if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                    for (PackInfoResult packInfoResult : CollectionUtil.safeFor(response.getData())) {
                        if (packInfoResult != null && !TextUtils.isEmpty(packInfoResult.getUuid())) {
                            packInfoResultMap.put(packInfoResult.getUuid(), packInfoResult);
                        }
                    }
                }
                int deviceItemSize = CollectionUtil.size(ownerDeviceItems);
                ListDeviceItem deviceItem = null;
                PackInfoResult packInfoResult = null;
                for (int i = 0; i < deviceItemSize; i++) {
                    deviceItem = ownerDeviceItems.get(i);
                    if (deviceItem != null && packInfoResultMap.containsKey(deviceItem.getDeviceId()) && packInfoResultMap.get(deviceItem.getDeviceId()) != null) {
                        packInfoResult = packInfoResultMap.get(deviceItem.getDeviceId());
                        float deviceTimezone = packInfoResult != null ? packInfoResult.getZone() : CountryUtil.getCurrentTimeZone();
                        boolean isOpenCloud = NooieCloudHelper.isOpenCloud(packInfoResult.getEnd_time(), deviceTimezone);
                        ownerDeviceItems.get(i).setOpenCloud(isOpenCloud);
                        ownerDeviceItems.get(i).updateOpenCloudDe(isOpenCloud);
                        DeviceApi.getInstance().updateConfigureDeviceInfo(false, account, deviceItem.getBindDevice(), packInfoResult);
                    } else if (deviceItem != null) {
                        ownerDeviceItems.get(i).setOpenCloud(false);
                        ownerDeviceItems.get(i).updateOpenCloudDe(false);
                        DeviceApi.getInstance().updateConfigureDeviceInfo(false, account, deviceItem.getBindDevice(), null);
                    }
                }
                return ownerDeviceItems;
            }
        }) : Observable.just(new ArrayList<>());

        return Observable.zip(getShareDeviceItemsObservable, getOwnerDeviceItemsObservable, new Func2<List<ListDeviceItem>, List<ListDeviceItem>, List<ListDeviceItem>>() {
            @Override
            public List<ListDeviceItem> call(List<ListDeviceItem> sharedListDeviceItems, List<ListDeviceItem> ownerListDeviceItems) {
                List<ListDeviceItem> deviceItems = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(sharedListDeviceItems)) {
                    deviceItems.addAll(sharedListDeviceItems);
                }
                if (CollectionUtil.isNotEmpty(ownerListDeviceItems)) {
                    deviceItems.addAll(ownerListDeviceItems);
                }
                //NooieLog.d("-->> debug SmartDeviceListPresenter test cloud getAllDeviceConfig use time: " + (System.currentTimeMillis() - testStartTime));
                DeviceConfigureService.getInstance().log();
                return deviceItems;
            }
        });
    }

    private Observable<List<BleApDeviceEntity>> getBleApDevice(String account) {
        return Observable.just("")
                .flatMap(new Func1<String, Observable<List<BleApDeviceEntity>>>() {
                    @Override
                    public Observable<List<BleApDeviceEntity>> call(String s) {
                        List<BleApDeviceEntity> bleDeviceEntities = BleApDeviceService.getInstance().getDevices();
                        BleApDeviceInfoCache.getInstance().updateApDevicesCache(bleDeviceEntities);
                        return Observable.just(bleDeviceEntities);
                    }
                });
    }

    private void updateDeviceInDb(String user, List<ListDeviceItem> deviceItems) {
        if (TextUtils.isEmpty(user) || CollectionUtil.isEmpty(deviceItems)) {
            return;
        }

        for (ListDeviceItem deviceItem : CollectionUtil.safeFor(deviceItems)) {
            if (deviceItem != null && deviceItem.getBindDevice() != null) {
                DeviceApi.getInstance().updateDevice(false, user, deviceItem.getBindDevice().getUuid(), ListDeviceItem.DEVICE_PLATFORM_NOOIE, deviceItem.getBindDevice());
                if (deviceItem.isDataEffectByKey(ListDeviceItem.DE_KEY_OPEN_CLOUD)) {
                    DeviceApi.getInstance().updateDeviceStorage(false, user, deviceItem.getBindDevice().getUuid(), ListDeviceItem.DEVICE_PLATFORM_NOOIE, deviceItem.isOpenCloud(), deviceItem.isMountedSDCard());
                }
            }
        }
        DeviceCacheService.getInstance().log(user);
    }

    private void getGatewayDevices(String account, String uid) {
        stopGetGatewayDeviceTask();
        mGetGatewayDeviceTask = DeviceService.getService().getGatewayDevices()
                .flatMap(new Func1<BaseResponse<List<GatewayDevice>>, Observable<List<GatewayDevice>>>() {
                    @Override
                    public Observable<List<GatewayDevice>> call(BaseResponse<List<GatewayDevice>> response) {
                        List<GatewayDevice> gatewayDevices = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            if (CollectionUtil.isNotEmpty(response.getData())) {
                                for (int i = 0; i < response.getData().size(); i++) {
                                    if (response.getData().get(i) != null && CollectionUtil.isNotEmpty(response.getData().get(i).getChild())) {
                                        for (int j = 0; j < response.getData().get(i).getChild().size(); j++) {
                                            if (response.getData().get(i).getChild().get(j) != null) {
                                                response.getData().get(i).getChild().get(j).setPuuid(response.getData().get(i).getUuid());
                                            }
                                        }
                                    }
                                }
                                gatewayDevices.addAll(response.getData());
                            }
                        }
                        return Observable.just(gatewayDevices);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GatewayDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<GatewayDevice> gatewayDevices) {
                        DeviceCmdApi.getInstance().updateGatewayDeviceCmdParams(account, gatewayDevices);
                        preConnectGatewayDevices(gatewayDevices, account, uid);
                    }
                });
    }

    private void stopGetGatewayDeviceTask() {
        if (mGetGatewayDeviceTask != null && !mGetGatewayDeviceTask.isUnsubscribed()) {
            mGetGatewayDeviceTask.unsubscribe();
            mGetGatewayDeviceTask = null;
        }
    }

    private void preConnectDevice(List<ListDeviceItem> deviceList, String uid) {
        List<BindDevice> bindDevices = new ArrayList<>();
        List<BindDevice> offLineDevices = new ArrayList<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(deviceList)) {
            if (device != null && device.getBindDevice() != null && device.getBindDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                bindDevices.add(device.getBindDevice());
            } else if (device != null && device.getBindDevice() != null) {
                offLineDevices.add(device.getBindDevice());
            }
        }
        NooieDeviceHelper.removeOffLineDeviceConn(offLineDevices);
        NooieDeviceHelper.tryConnectionToDevice(uid, bindDevices, false);
    }

    private void preConnectGatewayDevices(List<GatewayDevice> gatewayDevices, String account, String uid) {
        if (CollectionUtil.isEmpty(gatewayDevices) || TextUtils.isEmpty(uid)) {
            return;
        }

        NooieDeviceHelper.removeOffLineGatewayDeviceConn(gatewayDevices);
        NooieDeviceHelper.tryConnectionToGatewayDevice(uid, gatewayDevices, false);
    }

    private List<ListDeviceItem> getAllIpcDeviceList() {
        DeviceInfoCache.getInstance().replaceCaches();
        List<ListDeviceItem> devices = NooieDeviceHelper.convertListDeviceItem(DeviceInfoCache.getInstance().getAllDeviceInfo());
        return devices;
    }

    private List<BleApDeviceEntity> getAllBleApDeviceList() {
        List<BleApDeviceEntity> deviceList = new ArrayList<>();
        deviceList.addAll(CollectionUtil.safeFor(BleApDeviceInfoCache.getInstance().getAllBleApDevice()));
        return CollectionUtil.isEmpty(deviceList) ? deviceList : NooieDeviceHelper.sortBleApDevices(deviceList);
    }

    private void tryToStopBleApConnection(String model, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug SmartDeviceListPresenter tryToStopBleApConnection: model=" + model);
        try {
            ApHelper.getInstance().tryResetApConnectMode(model, listener);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

}
