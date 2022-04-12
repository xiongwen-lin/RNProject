package com.afar.osaio.smart.event;

public class RouterDeviceInfo {

    public String routerName;
    public int deviceType;
    public boolean isOnline;
    public boolean isBind;

    public RouterDeviceInfo(String routerName, int deviceType) {
        this.routerName = routerName;
        this.deviceType = deviceType;
    }

    public RouterDeviceInfo(String routerName, int deviceType, boolean isOnline, boolean isBind) {
        this.routerName = routerName;
        this.deviceType = deviceType;
        this.isOnline = isOnline;
        this.isBind = isBind;
    }
}
