package com.afar.osaio.message.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public interface IAppSettingView extends IBaseView {
    /**
     * Cache
     *
     * @param result
     */
    void notifyClearCacheResult(String result);

    void notifyGetCacheSuccess(String size);

    void notifyGetCacheFailed(String message);
}
