package com.afar.osaio.smart.device.bean;

public class RouterDeviceConnectInfo {

    private String deviceName;
    private String connectWifiType;
    private String minSpeed;
    private String maxSpeed;
    private String lastConnectTime;
    private boolean isOnline;
    private String title;
    private String device_mac;
    private String id_address;
    private String isWhite;

    public RouterDeviceConnectInfo(String deviceName, String connectWifiType, String minSpeed, String maxSpeed, String lastConnectTime, boolean isOnline, String device_mac, String id_address, String isWhite) {
        this.title = "";
        this.deviceName = deviceName;
        this.connectWifiType = connectWifiType;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.lastConnectTime = lastConnectTime;
        this.isOnline = isOnline;
        this.device_mac = device_mac;
        this.id_address = id_address;
        this.isWhite = isWhite;
    }

    public RouterDeviceConnectInfo(String title) {
        this.title = title;
    }

    public String getIsWhite() {
        return isWhite;
    }

    public void setIsWhite(String isWhite) {
        this.isWhite = isWhite;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public String getId_address() {
        return id_address;
    }

    public void setId_address(String id_address) {
        this.id_address = id_address;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getConnectWifiType() {
        return connectWifiType;
    }

    public void setConnectWifiType(String connectWifiType) {
        this.connectWifiType = connectWifiType;
    }

    public String getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(String minSpeed) {
        this.minSpeed = minSpeed;
    }

    public String getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(String maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public String getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(String lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }
}
