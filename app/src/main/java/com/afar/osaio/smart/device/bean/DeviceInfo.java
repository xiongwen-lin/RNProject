package com.afar.osaio.smart.device.bean;

import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

import java.io.Serializable;

public class DeviceInfo implements Serializable {

    private String deviceId;
    private String versionCode;
    private String model;
    private boolean openCamera;
    private boolean openCloud;
    private boolean loopRecordStatus;
    private long cloudTime;

    public DeviceInfo() {}

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isOpenCamera() {
        return openCamera;
    }

    public void setOpenCamera(boolean openCamera) {
        this.openCamera = openCamera;
    }

    public boolean isOpenCloud() {
        return openCloud;
    }

    public void setOpenCloud(boolean openCloud) {
        this.openCloud = openCloud;
    }

    public boolean getLoopRecordStatus() {
        return loopRecordStatus;
    }

    public void setLoopRecordStatus(boolean loopRecordStatus) {
        this.loopRecordStatus = loopRecordStatus;
    }

    public long getCloudTime() {
        return cloudTime;
    }

    public void setCloudTime(long cloudTime) {
        this.cloudTime = cloudTime;
    }

    //nooie smart
    private int devicePlatform;
    private BindDevice nooieDevice;

    public int getDevicePlatform() {
        return devicePlatform;
    }

    public void setDevicePlatform(int devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public BindDevice getNooieDevice() {
        return nooieDevice;
    }

    public void setNooieDevice(BindDevice nooieDevice) {
        this.nooieDevice = nooieDevice;
    }
}
