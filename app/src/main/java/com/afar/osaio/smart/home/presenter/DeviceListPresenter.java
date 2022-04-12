package com.afar.osaio.smart.home.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.bean.BleApDeviceInfo;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.routerlocal.RouterDao;
import com.afar.osaio.smart.routerlocal.RouterInfo;
import com.afar.osaio.smart.routerlocal.internet.InternetConnectionStatus;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.smartlook.cache.LockDeviceCache;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.nooie.sdk.db.dao.BleApDeviceService;
import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDeviceResult;
import com.nooie.sdk.api.network.base.bean.entity.DeviceStatusResult;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.bean.APPairStatus;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.nooie.sdk.db.entity.DeviceEntity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.contract.DeviceListContract;
import com.afar.osaio.util.ConstantValue;
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

public class DeviceListPresenter implements DeviceListContract.Presenter {

    public static final int PAGE_MAX_DEVICE_NUM = 100;

    private DeviceListContract.View mTasksView;
    private int mNextPage = 1;
    private Subscription mRefreshTask = null;
    private Subscription mLoadMoreTask = null;
    private Subscription mLoadBleApDeviceTask = null;
    private Subscription mLoadRouterDeviceTask = null;

    public DeviceListPresenter(DeviceListContract.View tasksView) {
        this.mTasksView = tasksView;
        this.mTasksView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
        }
    }

    public Observable<List<ListDeviceItem>> getDevicesObservable(final String account, int page, int pageSize) {
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
                            if (mTasksView != null) {
                                mTasksView.onLoadDeviceSuccess(deviceItems);
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

    public Observable<List<ListDeviceItem>> getAllDeviceConfig(String account, List<ListDeviceItem> devices) {
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
                //NooieLog.d("-->> debug DeviceListPresenter test cloud getAllDeviceConfig use time: " + (System.currentTimeMillis() - testStartTime));
                DeviceConfigureService.getInstance().log();
                return deviceItems;
            }
        });
    }

    public Observable<List<ListDeviceItem>> getDevicesConfig(String account, List<ListDeviceItem> devices) {
        //long testStartTime = System.currentTimeMillis();
        List<Observable<ListDeviceItem>> observables = new ArrayList<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            int bindType = device != null && device.getBindDevice() != null ? device.getBindDevice().getBind_type() : (device != null ? device.getBindType() : ApiConstant.BIND_TYPE_OWNER);
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

        return Observable.zip(observables, new FuncN<List<ListDeviceItem>>() {
            @Override
            public List<ListDeviceItem> call(Object... args) {
                List<ListDeviceItem> result = new ArrayList<>();
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        result.add((ListDeviceItem) args[i]);
                    }
                }
                //NooieLog.d("-->> debug DeviceListPresenter test cloud getDevicesConfig use time: " + (System.currentTimeMillis() - testStartTime));
                return result;
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

    @Override
    public void stopRefreshTask() {
        if (mRefreshTask != null && !mRefreshTask.isUnsubscribed()) {
            mRefreshTask.unsubscribe();
        }
    }

    @Override
    public void stopLoadMoreTask() {
        if (mLoadMoreTask != null && !mLoadMoreTask.isUnsubscribed()) {
            mLoadMoreTask.unsubscribe();
        }
    }

    @Override
    public void refreshDevices(final String account, String uid) {
        mNextPage = 1;
        if (mNextPage == 1) {
            DeviceInfoCache.getInstance().clearCacheMap();
        }
        stopRefreshTask();
        stopLoadMoreTask();
        mRefreshTask = Observable.just(account)
                .flatMap(new Func1<String, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(String user) {
                        List<DeviceEntity> deviceEntities = DeviceCacheService.getInstance().getDevices(user);
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
                        if (mTasksView != null) {
                            mTasksView.onLoadDeviceFailed("");
                        }
                    }

                    @Override
                    public void onNext(List<ListDeviceItem> listDeviceItems) {
                        if (mTasksView != null) {
                            mTasksView.onLoadDeviceSuccessEnd();
                        }
                        updateDeviceInDb(account, listDeviceItems);
                        getGatewayDevices(account, uid);
                    }
                });

        loadLockDevices(account);
    }

    @Override
    public void loadMoreDevice(String account) {
        getDevicesObservable(account, mNextPage, PAGE_MAX_DEVICE_NUM)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ListDeviceItem>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<ListDeviceItem> listDeviceItems) {
                        if (mTasksView != null) {
                            mTasksView.onLoadDeviceSuccessEnd();
                        }
                        updateDeviceInDb(account, listDeviceItems);
                    }
                });
    }

    @Override
    public void updateDeviceOpenStatus(final String deviceId, final int status) {
        DeviceService.getService().getDeviceStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyUpdateDeviceOpenStatusResult(ConstantValue.ERROR, deviceId, 0);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        boolean isNeedSetSleep = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getOpen_status() != status;
                        if (isNeedSetSleep) {
                            DeviceCmdApi.getInstance().setSleep(deviceId, ApiConstant.OPEN_STATUS_ON == status ? false : true, new OnActionResultListener() {
                                @Override
                                public void onResult(int code) {
                                    if (code == Constant.OK && mTasksView != null) {
                                        mTasksView.notifyUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, status);
                                    } else if (mTasksView != null) {
                                        mTasksView.notifyUpdateDeviceOpenStatusResult(ConstantValue.ERROR, deviceId, 0);
                                    }
                                }
                            });
                        } else if (mTasksView != null) {
                            mTasksView.notifyUpdateDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, status);
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
                        if (mTasksView != null) {
                            mTasksView.notifyGetDeviceOpenStatusResult("", "", 0);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && mTasksView != null) {
                            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, response.getData().getOpen_status());
                            mTasksView.notifyGetDeviceOpenStatusResult(ConstantValue.SUCCESS, deviceId, response.getData().getOpen_status());
                        } else if (mTasksView != null) {
                            mTasksView.notifyGetDeviceOpenStatusResult("", "", 0);
                        }
                    }
                });
    }

    @Override
    public void updateDeviceSort(final String account, final Map<String, Integer> devices, final Map<String, String> bindIdAndDeviceIdMap) {
        DeviceService.getService().updateDeviceSort(GsonHelper.convertToJson(devices))
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            for (Map.Entry<String, Integer> device : devices.entrySet()) {
                                if (device != null && !TextUtils.isEmpty(device.getKey())) {
                                    if (bindIdAndDeviceIdMap != null && bindIdAndDeviceIdMap.containsKey(device.getKey()) && !TextUtils.isEmpty(bindIdAndDeviceIdMap.get(device.getKey()))) {
                                        DeviceApi.getInstance().updateDeviceSort(false, account, bindIdAndDeviceIdMap.get(device.getKey()), ListDeviceItem.DEVICE_PLATFORM_NOOIE, device.getValue());
                                        NooieDeviceHelper.updateDeviceSort(bindIdAndDeviceIdMap.get(device.getKey()), device.getValue());
                                    }
                                }
                            }
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
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        NooieLog.d("-->> DeviceListPresenter updateDeviceSort update finish code" + (response != null ? response.getCode() : 0));
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            mTasksView.notifyUpdateDeviceCacheSort(ConstantValue.SUCCESS);
                        }
                    }
                });
    }

    @Override
    public void loadLockDevices(String account) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<List<BleDeviceEntity>>>() {
                    @Override
                    public Observable<List<BleDeviceEntity>> call(String s) {
                        List<BleDeviceEntity> bleDeviceEntities = BleDeviceService.getInstance().getDevices(account);
                        LockDeviceCache.getInstance().updateDevicesInLocalCache(bleDeviceEntities);
                        return Observable.just(bleDeviceEntities);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BleDeviceEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyLoadLockDeviceSuccess(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<BleDeviceEntity> bleDeviceEntities) {
                        if (mTasksView != null) {
                            mTasksView.notifyLoadLockDeviceSuccess(ConstantValue.SUCCESS, bleDeviceEntities);
                        }
                    }
                });
    }

    private Subscription mGetGatewayDeviceTask;

    public void getGatewayDevices(String account, String uid) {
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

    private void preConnectGatewayDevices(List<GatewayDevice> gatewayDevices, String account, String uid) {
        if (CollectionUtil.isEmpty(gatewayDevices) || TextUtils.isEmpty(uid)) {
            return;
        }

        NooieDeviceHelper.removeOffLineGatewayDeviceConn(gatewayDevices);
        NooieDeviceHelper.tryConnectionToGatewayDevice(uid, gatewayDevices, false);
    }

    @Override
    public void removeDevice(String account, String deviceId) {
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
                        if (mTasksView != null) {
                            mTasksView.onDeleteDeviceResult(ConstantValue.ERROR, deviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTasksView != null) {
                            mTasksView.onDeleteDeviceResult(response != null && response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR, deviceId);
                        }
                    }
                });
    }

    @Override
    public void checkApDirectConnection() {
        NooieLog.d("-->> debug DeviceListPresenter checkApDirectConnection: 1000");
        ApHelper.getInstance().startCheckApDirectConnection(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, new ApHelper.CheckApDirectConnectionListener() {
            @Override
            public void onCheckResult(boolean result, int resultType, String ssid, APPairStatus status, String uuid) {
                NooieLog.d("-->> debug DeviceListPresenter checkApDirectConnection: 1001");
                if (mTasksView != null) {
                    NooieLog.d("-->> debug DeviceListPresenter checkApDirectConnection: 1002");
                    mTasksView.onCheckApDirectConnection((result ? SDKConstant.SUCCESS : SDKConstant.ERROR), resultType, ssid, status, uuid);
                }
            }
        });
    }

    @Override
    public void startAPDirectConnect(String deviceSsid, boolean isAccessLive) {
        NooieLog.d("-->> debug DeviceListPresenter startAPDirectConnect: 1000");
        ApHelper.getInstance().setCheckApDirectHeartBeatCount(1);
        ApHelper.getInstance().checkApDirectConnectionByHeartBeat(deviceSsid, ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (code == Constant.OK) {
                    if (mTasksView != null) {
                        NooieLog.d("-->> debug DeviceListPresenter startAPDirectConnect: 1002");
                        mTasksView.onStartAPDirectConnect(SDKConstant.SUCCESS, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid, isAccessLive);
                    }
                } else {
                    ApHelper.getInstance().switchApConnectMode(ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid, new ApHelper.APDirectListener() {
                        @Override
                        public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                            NooieLog.d("-->> debug DeviceListPresenter startAPDirectConnect: 1001 result=" + result);
                            if (mTasksView != null) {
                                NooieLog.d("-->> debug DeviceListPresenter startAPDirectConnect: 1002");
                                mTasksView.onStartAPDirectConnect((result ? SDKConstant.SUCCESS : SDKConstant.ERROR), connectionMode, deviceSsid, isAccessLive);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void stopAPDirectConnection(String model) {
        NooieLog.d("-->> debug DeviceListPresenter stopAPDirectConnection: apConnectionExist=" + ApHelper.getInstance().checkBleApDeviceConnectingExist());
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            if (mTasksView != null) {
                mTasksView.onStopAPDirectConnection(SDKConstant.SUCCESS);
            }
            return;
        }
        tryToStopBleApConnection(model, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                if (mTasksView != null) {
                    mTasksView.onStopAPDirectConnection(SDKConstant.SUCCESS);
                }
            }
        });
    }

    @Override
    public void loadApDevice() {
        ApHelper.getInstance().loadApDevice(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, null, new ApHelper.LoadApDeviceListener() {
            @Override
            public void onLoadDevice(int state, ApDeviceInfo device) {
                if (mTasksView != null) {
                    mTasksView.onLoadApDevice(state, device);
                }
            }
        });
    }

    @Override
    public void updateApDeviceOpenStatus(String deviceSsid, String deviceId, int status) {
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK) {
                    boolean sleep = status != ApiConstant.OPEN_STATUS_ON;
                    if (sleep == on) {
                        if (mTasksView != null) {
                            mTasksView.onUpdateApDeviceOpenStatus(SDKConstant.SUCCESS, deviceSsid, deviceId, status);
                        }
                    } else {
                        DeviceCmdApi.getInstance().setSleep(deviceId, sleep, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTasksView != null) {
                                    mTasksView.onUpdateApDeviceOpenStatus(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, deviceSsid, deviceId, status);
                                }
                            }
                        });
                    }
                } else if (mTasksView != null) {
                    mTasksView.onUpdateApDeviceOpenStatus(SDKConstant.ERROR, deviceSsid, deviceId, status);
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
                if (mTasksView != null) {
                    mTasksView.onCheckApDirectWhenNetworkChange(SDKConstant.ERROR, null);
                }
            }

            @Override
            public void onNext(NetworkChangeResult result) {
                if (mTasksView != null) {
                    mTasksView.onCheckApDirectWhenNetworkChange(SDKConstant.SUCCESS, result);
                }
            }
        });
    }

    @Override
    public void loadBleApDevices(String account) {
        stopLoadBleApDevices();
        mLoadBleApDeviceTask = Observable.just("")
                .flatMap(new Func1<String, Observable<List<BleApDeviceEntity>>>() {
                    @Override
                    public Observable<List<BleApDeviceEntity>> call(String s) {
                        List<BleApDeviceEntity> bleDeviceEntities = BleApDeviceService.getInstance().getDevices();
                        BleApDeviceInfoCache.getInstance().updateApDevicesCache(bleDeviceEntities);
                        return Observable.just(bleDeviceEntities);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BleApDeviceEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.onLoadBleApDevices(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<BleApDeviceEntity> bleApDeviceEntities) {
                        if (mTasksView != null) {
                            mTasksView.onLoadBleApDevices(SDKConstant.SUCCESS, bleApDeviceEntities);
                        }
                    }
                });
    }

    public void stopLoadBleApDevices() {
        if (mLoadBleApDeviceTask != null && !mLoadBleApDeviceTask.isUnsubscribed()) {
            mLoadBleApDeviceTask.unsubscribe();
        }
        mLoadBleApDeviceTask = null;
    }

    @Override
    public void checkBleApDeviceConnecting() {
        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1001");
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1002");
            if (mTasksView != null) {
                mTasksView.checkBleApDeviceConnecting(SDKConstant.ERROR, null);
            }
            return;
        }
        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1003");
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<ApDeviceInfo>>() {
                    @Override
                    public Observable<ApDeviceInfo> call(Integer integer) {
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1004");
                        if (!NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {
                            NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1005");
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1006");
                        ApDeviceInfo apDeviceInfo = ApHelper.getInstance().getCurrentApDeviceInfo();
                        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
                            NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1007");
                            return Observable.just(null);
                        }
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1008");
                        String hotSpotSsid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                        boolean isHotSpotMatching = false;
                        if (NooieDeviceHelper.mergeIpcType(apDeviceInfo.getBindDevice().getType()) == IpcType.HC320) {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkBluetoothApFutureCode(hotSpotSsid, apDeviceInfo.getDeviceSsid());
                        } else {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkApFutureCode(hotSpotSsid);
                        }
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1009 hotSpotSsid=" + hotSpotSsid + " model=" + apDeviceInfo.getBindDevice().getType() + " isHotSpotMatching=" + isHotSpotMatching);
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
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (mTasksView != null) {
                            mTasksView.checkBleApDeviceConnecting(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(ApDeviceInfo result) {
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (mTasksView != null) {
                            mTasksView.checkBleApDeviceConnecting(SDKConstant.SUCCESS, result);
                        }
                    }
                });
    }

    @Override
    public void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid) {
        NooieLog.d("-->> debug DeviceListPresenter checkBeforeConnectBleDevice: 1001");
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        NooieLog.d("-->> debug DeviceListPresenter checkBeforeConnectBleDevice: 1001");
                        if (!NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {
                            NooieLog.d("-->> debug DeviceListPresenter checkBeforeConnectBleDevice: 1001");
                            return Observable.just("");
                        }
                        String hotSpotSsid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                        boolean isHotSpotMatching = false;
                        if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkBluetoothApFutureCode(hotSpotSsid, ssid);
                        } else {
                            isHotSpotMatching = TextUtils.isEmpty(hotSpotSsid) || NooieDeviceHelper.checkApFutureCode(hotSpotSsid);
                        }
                        NooieLog.d("-->> debug DeviceListPresenter checkBeforeConnectBleDevice: 1001 isHotSpotMatching=" + isHotSpotMatching);
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
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (mTasksView != null) {

                            mTasksView.onCheckBeforeConnectBleDevice(SDKConstant.ERROR, false, bleDeviceId, model, ssid);
                        }
                    }

                    @Override
                    public void onNext(String hotSpotSsid) {
                        NooieLog.d("-->> debug DeviceListPresenter checkBleApDeviceConnecting: 1001");
                        if (mTasksView != null) {
                            boolean result = !TextUtils.isEmpty(hotSpotSsid);
                            mTasksView.onCheckBeforeConnectBleDevice(SDKConstant.SUCCESS, result, bleDeviceId, model, ssid);
                        }
                    }
                });
    }

    @Override
    public void updateBleApDeviceSort(String user, Map<String, Integer> sortMap) {
        Observable.just(sortMap)
                .flatMap(new Func1<Map<String, Integer>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Map<String, Integer> sortMap) {
                        if (sortMap == null || sortMap.isEmpty()) {
                            return Observable.just(false);
                        }
                        for (Map.Entry<String, Integer> sortEntry : sortMap.entrySet()) {
                            if (sortEntry != null && !TextUtils.isEmpty(sortEntry.getKey())) {
                                Bundle data = new Bundle();
                                data.putInt(BleApDeviceService.KEY_SORT, sortEntry.getValue());
                                BleApDeviceService.getInstance().updateDevice(user, sortEntry.getKey(), data);
                            }
                        }
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
                        if (mTasksView != null) {
                            mTasksView.onUpdateBleApDeviceSort(SDKConstant.ERROR);
                        }
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (mTasksView != null) {
                            mTasksView.onUpdateBleApDeviceSort(SDKConstant.ERROR);
                        }
                    }
                });
    }

    private void tryToStopBleApConnection(String model, ApHelper.APDirectListener listener) {
        NooieLog.d("-->> debug DeviceListPresenter tryToStopBleApConnection: model=" + model);
        try {
            ApHelper.getInstance().tryResetApConnectMode(model, listener);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    @Override
    public void loadRouterDevices(String account, String routerWifiMac) {
        stopLoadRouterDevices();
        mLoadRouterDeviceTask = Observable.just("")
                .flatMap(new Func1<String, Observable<List<ListDeviceItem>>>() {
                    @Override
                    public Observable<List<ListDeviceItem>> call(String s) {
                        return Observable.just(getAllRouter(routerWifiMac));
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
                        if (mTasksView != null) {
                            mTasksView.onLoadRouterDevices(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<ListDeviceItem> routerDevices) {
                        if (mTasksView != null) {
                            mTasksView.onLoadRouterDevices(SDKConstant.SUCCESS, routerDevices);
                        }
                    }
                });
    }

    public void stopLoadRouterDevices() {
        if (mLoadRouterDeviceTask != null && !mLoadRouterDeviceTask.isUnsubscribed()) {
            mLoadRouterDeviceTask.unsubscribe();
        }
        mLoadRouterDeviceTask = null;
    }

    @Override
    public void deleteRouterDevice(String routerDevice) {
        Observable.just(routerDevice)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String deviceId) {
                        deleteRouter(deviceId);
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
                        if (mTasksView != null) {
                            mTasksView.onDeleteRouterDevice(SDKConstant.ERROR, routerDevice);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTasksView != null) {
                            mTasksView.onDeleteRouterDevice(SDKConstant.SUCCESS, routerDevice);
                        }
                    }
                });
    }

    // TODO: 2021/6/23
    private List<ListDeviceItem> getRouterDb(String routerWifiMac) {
        List<ListDeviceItem> routerList = new ArrayList<>();
        if (TextUtils.isEmpty(routerWifiMac)) {
            return routerList;
        }
        RouterInfo routerInfo = RouterDao.getInstance(NooieApplication.mCtx).findRouter(routerWifiMac);
        String routerName = routerInfo.getRouterName();
        String isbind = routerInfo.getIsbind();
        if ("".equals(routerName)) {
            return routerList;
        }
        ListDeviceItem listDeviceItem = new ListDeviceItem(routerName, routerWifiMac, 6, 1, isbind);
        routerList.add(listDeviceItem);
        return routerList;
    }

    /**
     * 获取所有路由器
     *  0 ： 离线
     *  1 ： 在线
     *  2 ： 未连接
     *  路由器首页显示逻辑： 没有连接路由器： 不显示文案,点击弹窗提示
     *                     连接路由器wifi但是不能上外网,显示离线
     *                     连上路由器可以上外网,在线
     * @param routerWifiMac
     * @return
     */
    private List<ListDeviceItem> getAllRouter(String routerWifiMac) {
        List<RouterInfo> routerList = RouterDao.getInstance(NooieApplication.mCtx).findAllRouter();
        List<ListDeviceItem> listDeviceItemList = new ArrayList<>();
        if (null == routerList || routerList.size() < 1) {
            return listDeviceItemList;
        }

        for (int i = 0; i < routerList.size(); i++) {
            String routerName = routerList.get(i).getRouterName();
            String isbind = routerList.get(i).getIsbind();
            String routerMac = routerList.get(i).getRouterMac();

            ListDeviceItem listDeviceItem;
            if (!routerWifiMac.equals("")) {
                if (routerWifiMac.equals(routerMac) && InternetConnectionStatus.isNetSystemUsable()) {
                    listDeviceItem = new ListDeviceItem(routerName, routerMac, 6, 1, isbind);
                } else if (routerWifiMac.equals(routerMac)){
                    listDeviceItem = new ListDeviceItem(routerName, routerMac, 6, 0, isbind);
                } else {
                    listDeviceItem = new ListDeviceItem(routerName, routerMac, 6, 2, isbind);
                }
            } else {
                listDeviceItem = new ListDeviceItem(routerName, routerMac, 6, 2, isbind);
            }

            listDeviceItemList.add(listDeviceItem);
        }

        return listDeviceItemList;
    }

    private void deleteRouter(String routerDevice) {
        if (TextUtils.isEmpty(routerDevice)) {
            return;
        }
        RouterDao.getInstance(NooieApplication.mCtx).deleteRouter(routerDevice);
    }
}
