package com.afar.osaio.smart.cache;

import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.db.dao.GatewayDeviceService;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GatewayDeviceCache extends BaseCache<GatewayDevice> {

    private GatewayDeviceCache() {
    }

    private static class GatewayDeviceCacheHolder {
        private static final GatewayDeviceCache INSTANCE = new GatewayDeviceCache();
    }

    public static GatewayDeviceCache getInstance() {
        return GatewayDeviceCacheHolder.INSTANCE;
    }

    public void addDevice(GatewayDevice gatewayDevice) {
        if (gatewayDevice == null) {
            return;
        }
        /*
        if (isExisted(gatewayDevice.getUuid())) {
            updateDevice(gatewayDevice);
        } else {
            addCache(gatewayDevice.getUuid(), gatewayDevice);
        }
        */

        addCache(gatewayDevice.getUuid(), gatewayDevice);
    }

    public void addDevices(List<GatewayDevice> devices) {
        for (GatewayDevice device : CollectionUtil.safeFor(devices)) {
            addDevice(device);
        }
    }

    public void updateDevice(GatewayDevice device) {
        if (device == null) {
            return;
        }
        GatewayDevice deviceCache = getCacheById(device.getUuid());
        if (deviceCache != null) {
            addCache(device.getUuid(), deviceCache);
        }
    }

    public void updateDevicesInDb() {
        Observable.from(getAllCache())
                .flatMap(new Func1<GatewayDevice, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(GatewayDevice device) {
                        try {
                            if (device != null) {
                                GatewayDeviceService.getInstance().addDevice(GlobalData.getInstance().getAccount(), device);
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

    /**
     * 根据请求获取全部的设备id,移除不存在的设备缓存.该方法调用了数据库，需在线程中运行
     * @param account
     */
    public void compareDevcieListCache(String account) {
        if (mCacheMap == null || mCacheMap.isEmpty() || CollectionUtil.isEmpty(mCacheDeviceIds)) {
            return;
        }

        Iterator<Map.Entry<String, GatewayDevice>> itemIterator = mCacheMap.entrySet().iterator();
        while (itemIterator.hasNext()) {
            Map.Entry<String, GatewayDevice> deviceEntry = itemIterator.next();
            if (deviceEntry != null && !mCacheDeviceIds.contains(deviceEntry.getKey())) {
                GatewayDeviceService.getInstance().deleteDevice(account, deviceEntry.getKey());
                itemIterator.remove();
            }
        }
    }

    public void removeDeviceCacheWithDb(String account, String deviceId) {
        try {
            GatewayDeviceService.getInstance().deleteDevice(account, deviceId);
            removeCacheById(deviceId);
            removeCacheDeviceId(deviceId);
            DeviceListCache.getInstance().removeSubDevicesCacheWithDb(account, deviceId);
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
