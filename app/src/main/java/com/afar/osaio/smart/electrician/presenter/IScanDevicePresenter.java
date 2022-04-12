package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IScanDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/5
 */
public interface IScanDevicePresenter extends IBasePresenter {

    //开始配网
    void startDeviceSearch(int mode, String ssid, String pw,String token,int deviceType,String uuid,String address,String mac);

    //停止配网
    void stopDeviceSearch();

    //断开蓝牙时，停止涂鸦蓝牙配网
    void setDeviceBlueState(String uuid ,boolean isOn);

    //退出页面销毁一些缓存和监听
    void release();
}
