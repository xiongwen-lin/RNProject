package com.afar.osaio.base.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/8/21
 * Email is victor.qiao.0604@gmail.com
 */
public interface IBaseActivityPresenter extends IBasePresenter {
    void handleNooieSharedDevice(int msgId, int sharedId, boolean agree);

    void changeDeviceUpgradeState(String user, String deviceId, int platform, int upgradeState);

//    void connectShortLinkDevice(String taskId, String account, String deviceId, ConnectShortLinkDeviceListener listener);

}
