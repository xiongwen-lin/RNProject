package com.afar.osaio.smart.electrician.bean;

import java.io.Serializable;

public class PowerStripName implements Serializable {
    private String code;
    private int dpId;
    private String name;
    private boolean value;
    private boolean isSelected;

    public PowerStripName(String code, int dpId) {
        this.code = code;
        this.dpId = dpId;
    }

    public PowerStripName(String code, int dpId, String name, boolean value) {
        this.code = code;
        this.dpId = dpId;
        this.name = name;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDpId() {
        return String.valueOf(dpId);
    }

    public void setDpId(int dpId) {
        this.dpId = dpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

