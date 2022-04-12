package com.afar.osaio.smart.cache;

import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.db.dao.GatewayDeviceService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SmartRouterDeviceCache extends BaseCache<SmartRouterDevice> {

    private SmartRouterDeviceCache() {
    }

    private static class SmartRouterDeviceCacheHolder {
        private static final SmartRouterDeviceCache INSTANCE = new SmartRouterDeviceCache();
    }

    public static SmartRouterDeviceCache getInstance() {
        return SmartRouterDeviceCacheHolder.INSTANCE;
    }

    public void addDevice(SmartRouterDevice device) {
        if (device == null) {
            return;
        }
        addCache(device.deviceId, device);
    }

    public void addDevices(List<SmartRouterDevice> devices) {
        for (SmartRouterDevice device : CollectionUtil.safeFor(devices)) {
            addDevice(device);
        }
    }

    public void updateDevice(SmartRouterDevice device) {
        if (device == null) {
            return;
        }
        SmartRouterDevice deviceCache = getCacheById(device.deviceId);
        if (deviceCache != null) {
            addCache(device.deviceId, deviceCache);
        }
    }

    public void updateDevicesInDb() {
        Observable.from(getAllCache())
                .flatMap(new Func1<SmartRouterDevice, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(SmartRouterDevice device) {
                        try {
                            if (device != null) {
                                //GatewayDeviceService.getInstance().addDevice(GlobalData.getInstance().getAccount(), device);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    public void addCacheDeviceIds(List<String> cacheDeviceIds) {
        if (mCacheDeviceIds == null) {
            mCacheDeviceIds = new ArrayList<>();
        }
        for (String deviceId: CollectionUtil.safeFor(cacheDeviceIds)) {
            if (!cacheDeviceIds.contains(deviceId)) {
                cacheDeviceIds.add(deviceId);
            }
        }
    }

    public void clearCacheDeviceIds() {
        if (mCacheDeviceIds != null) {
            mCacheDeviceIds.clear();
        }
    }

    public void removeDeviceCacheWithDb(String account, String deviceId) {
        try {
            //GatewayDeviceService.getInstance().deleteDevice(account, deviceId);
            removeCacheById(deviceId);
            removeCacheDeviceId(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeDeviceCache(String deviceId) {
        try {
            removeCacheById(deviceId);
            removeCacheDeviceId(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
