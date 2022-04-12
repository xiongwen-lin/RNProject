package com.afar.osaio.smart.device.bean;

import com.nooie.sdk.bean.IpcType;

import java.io.Serializable;

/**
 * DeviceCfg
 *
 * @author Administrator
 * @date 2020/4/9
 */
public class DeviceCfg implements Serializable {

    private String deviceId;
    private String pDeviceId;
    private IpcType deviceType;
    private int bindType;
    private int connectionMode;
    private boolean isSubDevice;

    public DeviceCfg() {
    }

    public DeviceCfg(String deviceId, String pDeviceId, IpcType deviceType, int bindType, int connectionMode, boolean isSubDevice) {
        this.deviceId = deviceId;
        this.pDeviceId = pDeviceId;
        this.deviceType = deviceType;
        this.bindType = bindType;
        this.connectionMode = connectionMode;
        this.isSubDevice = isSubDevice;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getpDeviceId() {
        return pDeviceId;
    }

    public void setpDeviceId(String pDeviceId) {
        this.pDeviceId = pDeviceId;
    }

    public IpcType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(IpcType deviceType) {
        this.deviceType = deviceType;
    }

    public int getBindType() {
        return bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }

    public int getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(int connectionMode) {
        this.connectionMode = connectionMode;
    }
}
