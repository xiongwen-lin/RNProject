package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;

/**
 * IScanDeviceModel
 *
 * @author Administrator
 * @date 2019/3/5
 */
public interface IScanDeviceModel extends IBaseModel {

    void start();

    void cancel();

    void destroy();

    void setEC(String ssid, String password, String token, ITuyaSmartActivatorListener listener);

    void setAP(String ssid, String password, String token, ITuyaSmartActivatorListener listener);

}
