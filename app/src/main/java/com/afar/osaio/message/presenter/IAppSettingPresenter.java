package com.afar.osaio.message.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public interface IAppSettingPresenter extends IBasePresenter {
    void getCacheSize();

    void clearCache();
}
