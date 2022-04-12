package com.afar.osaio.smart.device.bean;

public class RouterDetalisInfo {
    private String itemName;
    private boolean connectDeviceState = false;
    private int connectDeviceNum = 0;

    public RouterDetalisInfo(String itemName, boolean connectDeviceState, int connectDeviceNum) {
        this.itemName = itemName;
        this.connectDeviceState = connectDeviceState;
        this.connectDeviceNum = connectDeviceNum;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isConnectDeviceState() {
        return connectDeviceState;
    }

    public void setConnectDeviceState(boolean connectDeviceState) {
        this.connectDeviceState = connectDeviceState;
    }

    public int getConnectDeviceNum() {
        return connectDeviceNum;
    }

    public void setConnectDeviceNum(int connectDeviceNum) {
        this.connectDeviceNum = connectDeviceNum;
    }
}
