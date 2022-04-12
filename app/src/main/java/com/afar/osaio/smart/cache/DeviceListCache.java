package com.afar.osaio.smart.cache;

import android.text.TextUtils;

import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.nooie.sdk.cache.BaseCache;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DeviceListCache extends BaseCache<ListDeviceItem> {

    private DeviceListCache() {
    }

    private static class DeviceListCacheHolder {
        private static final DeviceListCache INSTANCE = new DeviceListCache();
    }

    public static DeviceListCache getInstance() {
        return DeviceListCacheHolder.INSTANCE;
    }

    public void addDevice(ListDeviceItem deviceItem) {
        if (deviceItem == null) {
            return;
        }
        if (isExisted(deviceItem.getDeviceId())) {
            updateDevice(deviceItem);
        } else {
            addCache(deviceItem.getDeviceId(), deviceItem);
        }
    }

    public void addDevices(List<ListDeviceItem> deviceItems) {
        for (ListDeviceItem deviceItem : CollectionUtil.safeFor(deviceItems)) {
            addDevice(deviceItem);
        }
    }

    public void updateDevice(ListDeviceItem device) {
        if (device == null) {
            return;
        }
        ListDeviceItem deviceCache = getCacheById(device.getDeviceId());
        if (deviceCache != null) {
            deviceCache.updateItemByEffect(device);
            addCache(device.getDeviceId(), deviceCache);
        }
    }

    public void updateDevicesInDb() {
        Observable.from(getAllCache())
                .flatMap(new Func1<ListDeviceItem, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(ListDeviceItem device) {
                        if (device != null && !TextUtils.isEmpty(device.getModel()) && device.isDataEffectByKey(ListDeviceItem.DE_KEY_OPEN_CLOUD)) {
                            DeviceCacheService.getInstance().updateDevice(GlobalData.getInstance().getAccount(), device.getDeviceId(), device.getDevicePlatform(), device.getVersion(), device.getModel(), device.isOpenCloud(), device.isMountedSDCard(), device.getCloudTime(), -1,device.getBindDevice());
                            //DeviceCacheService.getInstance().log(prefs.account());
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
            if (!mCacheDeviceIds.contains(deviceId)) {
                mCacheDeviceIds.add(deviceId);
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
        if (mCacheMap == null || mCacheMap.isEmpty() || mCacheDeviceIds == null) {
            return;
        }

        Iterator<Map.Entry<String, ListDeviceItem>> itemIterator = mCacheMap.entrySet().iterator();
        while (itemIterator.hasNext()) {
            Map.Entry<String, ListDeviceItem> deviceItemEntry = itemIterator.next();
            if (deviceItemEntry != null && !mCacheDeviceIds.contains(deviceItemEntry.getKey())) {
                DeviceCacheService.getInstance().deleteDevice(account, deviceItemEntry.getKey());
                itemIterator.remove();
                removeCacheDeviceId(deviceItemEntry.getKey());
            }
        }
    }

    public List<ListDeviceItem> getAllSubDevice(String pDeviceId) {
        List<ListDeviceItem> deviceItems = new ArrayList<>();
        if (CollectionUtil.isEmpty(getAllCache())) {
            return deviceItems;
        }
        for (ListDeviceItem deviceItem : CollectionUtil.safeFor(getAllCache())) {
            if (deviceItem != null && deviceItem.getBindDevice() != null && NooieDeviceHelper.isSubDevice(deviceItem.getBindDevice().getPuuid(), deviceItem.getBindDevice().getType()) && pDeviceId.equalsIgnoreCase(deviceItem.getBindDevice().getPuuid())) {
                deviceItems.add(deviceItem);
            }
        }
        return deviceItems;
    }

    public void removeDeviceCacheWithDb(String account, String deviceId) {
        try {
            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
            DeviceInfoCache.getInstance().removeCacheById(deviceId);
            removeCacheById(deviceId);
            removeCacheDeviceId(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSubDevicesCacheWithDb(String account, String pDeviceId) {
        try {
            List<ListDeviceItem> subDeviceItems = getAllSubDevice(pDeviceId);
            for (ListDeviceItem subDeviceItem : CollectionUtil.safeFor(subDeviceItems)) {
                if (subDeviceItem != null && subDeviceItem.getBindDevice() != null) {
                    removeDeviceCacheWithDb(account, subDeviceItem.getBindDevice().getUuid());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
