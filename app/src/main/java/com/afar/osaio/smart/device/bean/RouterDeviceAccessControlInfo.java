package com.afar.osaio.smart.device.bean;

public class RouterDeviceAccessControlInfo {
    private String deviceMac;
    private int selectNum;
    private String deviceName;
    private String ruleTime;
    private int itemType;
    private String ruleName;

    public RouterDeviceAccessControlInfo(String deviceMac, String deviceName, String ruleTime, String ruleName, int itemType) {
        this.deviceMac = deviceMac;
        this.deviceName = deviceName;
        this.ruleTime = ruleTime;
        this.ruleName = ruleName;
        this.itemType = itemType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int getSelectNum() {
        return selectNum;
    }

    public void setSelectNum(int selectNum) {
        this.selectNum = selectNum;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRuleTime() {
        return ruleTime;
    }

    public void setRuleTime(String ruleTime) {
        this.ruleTime = ruleTime;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
