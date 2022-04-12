package com.afar.osaio.smart.cache;

import com.nooie.sdk.bean.CacheData;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BaseCache
 *
 * @author Administrator
 * @date 2019/8/6
 */
public class BaseNormalCache<K,T> {
    private static final String TAG = BaseNormalCache.class.getSimpleName();

    public ConcurrentHashMap<K, CacheData<K,T>> mCacheMap = new ConcurrentHashMap<>();
    public List<K> mCacheDeviceIds = new ArrayList<>();

    public void initCache() {
        if (mCacheMap == null) {
            mCacheMap = new ConcurrentHashMap<>();
        }

        if (mCacheDeviceIds == null) {
            mCacheDeviceIds = new ArrayList<>();
        }
    }

    public void addCache(K id, CacheData<K, T> data) {
        initCache();
        mCacheMap.put(id, data);
    }

    public void addCacheId(K id) {
        if (isContainCacheDeviceId(id)) {
            return;
        }

        if (mCacheDeviceIds == null) {
            mCacheDeviceIds = new ArrayList<>();
        }
        mCacheDeviceIds.add(id);
    }

    public boolean isContainCacheDeviceId(K id) {
        return CollectionUtil.isNotEmpty(mCacheDeviceIds) && mCacheDeviceIds.contains(id);
    }

    public void removeCacheById(K id) {
        if (mCacheMap != null && mCacheMap.containsKey(id)) {
            mCacheMap.remove(id);
        }

        if (mCacheDeviceIds != null && mCacheDeviceIds.contains(id)) {
            mCacheDeviceIds.remove(id);
        }
    }

    public void clearCacheMap() {
        if (mCacheMap != null) {
            mCacheMap.clear();
        }
    }

    public void clearCache() {
        if (mCacheMap != null) {
            mCacheMap.clear();
            mCacheMap = null;
        }

        if (mCacheDeviceIds != null) {
            mCacheDeviceIds.clear();
            mCacheDeviceIds = null;
        }
    }

    public T getCacheById(K id) {
        return mCacheMap != null ? mCacheMap.get(id).getData() : null;
    }

    public List<T> getCachesByIds(List<String> ids) {
        List<T> list = new ArrayList<>();
        if (mCacheMap == null) {
            return list;
        }
        for (String id : CollectionUtil.safeFor(ids)) {
            T data = mCacheMap.get(id).getData();
            if (data != null) {
                list.add(data);
            }
        }
        return list;
    }

    public List<T> getAllCache() {
        List<T> list = new ArrayList<>();
        if (mCacheMap == null) {
            return list;
        }
        for(Map.Entry<K, CacheData<K, T>> entry : mCacheMap.entrySet()) {
            if (entry.getValue() != null) {
                list.add(entry.getValue().getData());
            }
        }
        return list;
    }

    public boolean checkNotNull() {
        return mCacheMap != null;
    }

    public boolean isExisted(String deviceId) {
        return checkNotNull() && mCacheMap.containsKey(deviceId);
    }

    public boolean isEmpty() {
        return mCacheMap == null || mCacheMap.isEmpty();
    }

    public void removeCacheDeviceId(K deviceId) {
//        if (TextUtils.isEmpty(deviceId) || CollectionUtil.isEmpty(mCacheDeviceIds) || !mCacheDeviceIds.contains(deviceId)) {
//            return;
//        }
//
//        Iterator<K> cacheIdIterator = mCacheDeviceIds.iterator();
//        while (cacheIdIterator.hasNext()) {
//            if (deviceId.equalsIgnoreCase(cacheIdIterator.next())) {
//                cacheIdIterator.remove();
//            }
//        }
    }

    public int cacheSize() {
        return mCacheMap != null ? mCacheMap.size() : 0;
    }
}
