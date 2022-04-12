package com.afar.osaio.bean;

public class ConnectionModeBean {

    public int connectionMode;
    public String title;
    public String content;
    public int resId;
    public String model;

    public ConnectionModeBean(int connectionMode, String title, String content, int resId, String model) {
        this.connectionMode = connectionMode;
        this.title = title;
        this.content = content;
        this.resId = resId;
        this.model = model;
    }

    public int getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(int connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
