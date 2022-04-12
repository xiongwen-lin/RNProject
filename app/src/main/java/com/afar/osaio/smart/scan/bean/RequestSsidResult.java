package com.afar.osaio.smart.scan.bean;

public class RequestSsidResult {

    public static final int TYPE_REQUEST_SSID_WIFI_INVALID = 1;
    public static final int TYPE_REQUEST_SSID_WIFI_VALID = 2;

    private int type;
    private String ssid;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
