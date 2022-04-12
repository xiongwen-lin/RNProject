package com.afar.osaio.message.model;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.GlideUtil;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.file.FileUtil;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class AppSettingModelImpl implements IAppSettingModel {

    /**
     * 清除缓存，包括文件缓存，图片缓存
     * @return
     */
    @Override
    public Observable<Boolean> clearCache() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                GlideUtil.clearDiskCache();
                FileUtil.clearAllCache(NooieApplication.mCtx, GlobalData.getInstance().getAccount());
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<String> getCacheSize() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(FileUtil.getTotalCacheSize(NooieApplication.mCtx, GlobalData.getInstance().getAccount()));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }
}
