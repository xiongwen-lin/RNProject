package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IDeviceInfoPresenter
 *
 * @author Administrator
 * @date 2019/3/18
 */
public interface IDeviceInfoPresenter extends IBasePresenter {

    void loadDeviceInfo(String deviceId);

    void release();

    void queryShareDev(String devId);

    void getDeviceIp(String devId);

}
