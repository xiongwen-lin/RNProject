package com.afar.osaio.bean;

import android.os.Bundle;

public class LabelItemBean {

    private int id;
    private String title;
    private int iconRes;
    private Bundle param;

    public LabelItemBean() {
    }

    public LabelItemBean(int id, String title, int iconRes) {
        this(id, title, iconRes, null);
    }

    public LabelItemBean(int id, String title, int iconRes, Bundle param) {
        this.id = id;
        this.title = title;
        this.iconRes = iconRes;
        this.param = param;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public Bundle getParam() {
        return param;
    }

    public void setParam(Bundle param) {
        this.param = param;
    }
}
