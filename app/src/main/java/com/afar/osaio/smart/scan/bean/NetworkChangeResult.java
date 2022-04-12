package com.afar.osaio.smart.scan.bean;

public class NetworkChangeResult {

    private boolean isConnected;
    private String ssid;

    public boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
