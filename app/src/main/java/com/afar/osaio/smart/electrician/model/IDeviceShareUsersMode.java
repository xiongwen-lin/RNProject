package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

public interface IDeviceShareUsersMode extends IBaseModel {

    void queryDevShareUserList(String devId, ITuyaResultCallback callback);


}
