package com.afar.osaio.smart.device.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParentalControlDeviceInfo implements Serializable {

    private String deviceMac;
    private String deviceName;
    private String startTime;
    private String endTime;
    private List<Integer> saveDays = new ArrayList<>();
    private boolean onlineState = false;
    private String titleInfo = "";
    private String ip_address = "";
    private boolean isShowStatus;

    public ParentalControlDeviceInfo(String deviceMac, String deviceName, String startTime, String endTime, List<Integer> saveDays, boolean onlineState, boolean isShowStatus) {
        this.deviceMac = deviceMac;
        this.deviceName = deviceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.saveDays = saveDays;
        this.onlineState = onlineState;
        this.isShowStatus = isShowStatus;
    }

    public ParentalControlDeviceInfo(String deviceName, boolean onlineState) {
        this.deviceName = deviceName;
        this.onlineState = onlineState;
    }

    public ParentalControlDeviceInfo(String titleInfo) {
        this.titleInfo = titleInfo;
    }

    public ParentalControlDeviceInfo(String deviceMac, String deviceName) {
        this.deviceMac = deviceMac;
        this.deviceName = deviceName;
    }

    public boolean isShowStatus() {
        return isShowStatus;
    }

    public void setShowStatus(boolean showStatus) {
        isShowStatus = showStatus;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getTitleInfo() {
        return titleInfo;
    }

    public void setTitleInfo(String titleInfo) {
        this.titleInfo = titleInfo;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSaveDays() {
        return saveDays;
    }

    public void setSaveDays(List<Integer> saveDays) {
        this.saveDays = saveDays;
    }

    public boolean isOnlineState() {
        return onlineState;
    }

    public void setOnlineState(boolean onlineState) {
        this.onlineState = onlineState;
    }
}
