package com.afar.osaio.account.view;

import com.afar.osaio.base.view.IBaseActivityView;

/**
 * Created by victor on 2018/8/30
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISplashView extends IBaseActivityView {

    void onInitGlobalDataResult(String result, boolean initResult);

    void notifySignInResult(String msg);

    void onCheckAppIsStarted(int state, boolean isStarted);
}
