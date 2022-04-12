package com.afar.osaio.bean;

import java.util.List;

public class SelectProduct {

    private String name;
    private int type;
    private boolean isSelected;
    private List<SelectDeviceBean> children;
    private boolean enable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public List<SelectDeviceBean> getChildren() {
        return children;
    }

    public void setChildren(List<SelectDeviceBean> children) {
        this.children = children;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
