package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/8/30
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISplashPresenter extends IBasePresenter {

    void initGlobalData();

    void autoLogin(boolean isRetryLogin);

    void checkAppIsStarted();
}
