package com.afar.osaio.bean;

public class SelectDeviceBean {

    private String model;
    private String name;
    private int iconRes;
    private String type;
    private int productType;
    private boolean showLink;
    private boolean enable;
    private boolean isSelected;

    public SelectDeviceBean() {
    }

    public SelectDeviceBean(String model, String name, int iconRes, String type, boolean showLink) {
        this.model = model;
        this.name = name;
        this.iconRes = iconRes;
        this.type = type;
        this.showLink = showLink;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public boolean getShowLink() {
        return showLink;
    }

    public void setShowLink(boolean showLink) {
        this.showLink = showLink;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
