package com.afar.osaio.bean;

public class ShortLinkDeviceParam {

    private String account;
    private String deviceId;
    private String model;
    private boolean isSubDevice;
    private boolean isInit;
    private int connectionMode;

    public ShortLinkDeviceParam() {
    }

    public ShortLinkDeviceParam(String account, String deviceId, String model, boolean isSubDevice, boolean isInit, int connectionMode) {
        this.account = account;
        this.deviceId = deviceId;
        this.model = model;
        this.isSubDevice = isSubDevice;
        this.isInit = isInit;
        this.connectionMode = connectionMode;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isSubDevice() {
        return isSubDevice;
    }

    public void setSubDevice(boolean subDevice) {
        isSubDevice = subDevice;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public int getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(int connectionMode) {
        this.connectionMode = connectionMode;
    }
}
