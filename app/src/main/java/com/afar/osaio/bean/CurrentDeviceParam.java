package com.afar.osaio.bean;

public class CurrentDeviceParam {

    private String deviceId;
    private String pDeviceId;
    private String model;
    private int connectionMode;

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(int connectionMode) {
        this.connectionMode = connectionMode;
    }
}
