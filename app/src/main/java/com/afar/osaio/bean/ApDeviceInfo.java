package com.afar.osaio.bean;

import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.hub.CameraInfo;

public class ApDeviceInfo {

    private String deviceSsid;
    private BindDevice bindDevice;
    private DevAllSettingsV2 devAllSettingsV2;
    private DevInfo devInfo;
    private FormatInfo formatInfo;
    private CameraInfo cameraInfo;
    private String deviceId;
    private String bleDeviceId;

    public String getDeviceSsid() {
        return deviceSsid;
    }

    public void setDeviceSsid(String deviceSsid) {
        this.deviceSsid = deviceSsid;
    }

    public BindDevice getBindDevice() {
        return bindDevice;
    }

    public void setBindDevice(BindDevice bindDevice) {
        this.bindDevice = bindDevice;
    }

    public DevAllSettingsV2 getDevAllSettingsV2() {
        return devAllSettingsV2;
    }

    public void setDevAllSettingsV2(DevAllSettingsV2 devAllSettingsV2) {
        this.devAllSettingsV2 = devAllSettingsV2;
    }

    public DevInfo getDevInfo() {
        return devInfo;
    }

    public void setDevInfo(DevInfo devInfo) {
        this.devInfo = devInfo;
    }

    public FormatInfo getFormatInfo() {
        return formatInfo;
    }

    public void setFormatInfo(FormatInfo formatInfo) {
        this.formatInfo = formatInfo;
    }

    public CameraInfo getCameraInfo() {
        return cameraInfo;
    }

    public void setCameraInfo(CameraInfo cameraInfo) {
        this.cameraInfo = cameraInfo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBleDeviceId() {
        return bleDeviceId;
    }

    public void setBleDeviceId(String bleDeviceId) {
        this.bleDeviceId = bleDeviceId;
    }
}
