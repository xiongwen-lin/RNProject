package com.afar.osaio.smart.electrician.model;

import android.text.TextUtils;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;

/**
 * NameDeviceModel
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class NameDeviceModel implements INameDeviceModel {

    private ITuyaDevice mDevice;

    public NameDeviceModel(String deviceId) {
        initTuyaDevice(deviceId);
    }

    private void initTuyaDevice(String deviceId) {
        mDevice = TuyaHomeSdk.newDeviceInstance(deviceId);
    }

    @Override
    public void renameDevice(String name, IResultCallback callback) {
        if (!TextUtils.isEmpty(name)) {
            mDevice.renameDevice(name, callback);
        }
    }

    @Override
    public void removeDevice(IResultCallback callback) {
        mDevice.removeDevice(callback);
    }

    @Override
    public void release() {
        if (mDevice != null) {
            mDevice.onDestroy();
            mDevice = null;
        }
    }
}
