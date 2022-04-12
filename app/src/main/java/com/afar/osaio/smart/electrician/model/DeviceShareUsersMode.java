package com.afar.osaio.smart.electrician.model;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

public class DeviceShareUsersMode implements IDeviceShareUsersMode {

    @Override
    public void queryDevShareUserList(String devId, ITuyaResultCallback callback) {

        TuyaHomeSdk.getDeviceShareInstance().queryDevShareUserList(devId,callback);

    }
}
