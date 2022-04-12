package com.afar.osaio.bean;

public class UserManualBean {

    private String model;
    private String deviceAlias;
    private String url;

    public UserManualBean(String model, String deviceAlias, String url) {
        this.model = model;
        this.deviceAlias = deviceAlias;
        this.url = url;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
