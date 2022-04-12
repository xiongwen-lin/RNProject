package com.afar.osaio.smart.device.bean;

import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

public class NooieDevice {

    private BindDevice device;
    private GatewayDevice gatewayDevice;
    private AppVersionResult appVersionResult;
    private DeviceUpdateStatusResult deviceUpdateStatusResult;

    public BindDevice getDevice() {
        return device;
    }

    public void setDevice(BindDevice device) {
        this.device = device;
    }

    public GatewayDevice getGatewayDevice() {
        return gatewayDevice;
    }

    public void setGatewayDevice(GatewayDevice gatewayDevice) {
        this.gatewayDevice = gatewayDevice;
    }

    public AppVersionResult getAppVersionResult() {
        return appVersionResult;
    }

    public void setAppVersionResult(AppVersionResult appVersionResult) {
        this.appVersionResult = appVersionResult;
    }

    public DeviceUpdateStatusResult getDeviceUpdateStatusResult() {
        return deviceUpdateStatusResult;
    }

    public void setDeviceUpdateStatusResult(DeviceUpdateStatusResult deviceUpdateStatusResult) {
        this.deviceUpdateStatusResult = deviceUpdateStatusResult;
    }
}
