package com.afar.osaio.smart.device.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParentalControlRuleInfo implements Serializable {

    private String deviceMac;
    private List<String> ruleTimeDay = new ArrayList<>();
    private String ruleStartTimeH;
    private String ruleStartTimeM;
    private String ruleEndTimeH;
    private String ruleEndTimeM;
    private String state;
    private String deviceName;
    private String deviceRuleName;

    public ParentalControlRuleInfo(String deviceMac, List<String> ruleTimeDay, String ruleStartTimeH,
                                   String ruleStartTimeM, String ruleEndTimeH, String ruleEndTimeM, String state, String deviceName, String deviceRuleName) {
        this.deviceMac = deviceMac;
        this.ruleTimeDay = ruleTimeDay;
        this.ruleStartTimeH = ruleStartTimeH;
        this.ruleStartTimeM = ruleStartTimeM;
        this.ruleEndTimeH = ruleEndTimeH;
        this.ruleEndTimeM = ruleEndTimeM;
        this.state = state;
        this.deviceName = deviceName;
        this.deviceRuleName = deviceRuleName;
    }

    public ParentalControlRuleInfo(List<String> ruleTimeDay, String ruleStartTimeH, String ruleStartTimeM, String ruleEndTimeH,
                                   String ruleEndTimeM, String state, String deviceName, String deviceRuleName) {
        this.ruleTimeDay = ruleTimeDay;
        this.ruleStartTimeH = ruleStartTimeH;
        this.ruleStartTimeM = ruleStartTimeM;
        this.ruleEndTimeH = ruleEndTimeH;
        this.ruleEndTimeM = ruleEndTimeM;
        this.state = state;
        this.deviceName = deviceName;
        this.deviceRuleName = deviceRuleName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public List<String> getRuleTimeDay() {
        return ruleTimeDay;
    }

    public void setRuleTimeDay(List<String> ruleTimeDay) {
        this.ruleTimeDay = ruleTimeDay;
    }

    public String getRuleStartTimeH() {
        return ruleStartTimeH;
    }

    public void setRuleStartTimeH(String ruleStartTimeH) {
        this.ruleStartTimeH = ruleStartTimeH;
    }

    public String getRuleStartTimeM() {
        return ruleStartTimeM;
    }

    public void setRuleStartTimeM(String ruleStartTimeM) {
        this.ruleStartTimeM = ruleStartTimeM;
    }

    public String getRuleEndTimeH() {
        return ruleEndTimeH;
    }

    public void setRuleEndTimeH(String ruleEndTimeH) {
        this.ruleEndTimeH = ruleEndTimeH;
    }

    public String getRuleEndTimeM() {
        return ruleEndTimeM;
    }

    public void setRuleEndTimeM(String ruleEndTimeM) {
        this.ruleEndTimeM = ruleEndTimeM;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceRuleName() {
        return deviceRuleName;
    }

    public void setDeviceRuleName(String deviceRuleName) {
        this.deviceRuleName = deviceRuleName;
    }
}
