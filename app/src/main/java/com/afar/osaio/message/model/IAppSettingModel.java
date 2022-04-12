package com.afar.osaio.message.model;

import com.afar.osaio.base.mvp.IBaseModel;

import rx.Observable;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public interface IAppSettingModel extends IBaseModel {

    Observable<Boolean> clearCache();

    Observable<String> getCacheSize();
}
