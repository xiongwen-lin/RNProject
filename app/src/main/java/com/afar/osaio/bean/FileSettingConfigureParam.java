package com.afar.osaio.bean;

public class FileSettingConfigureParam {

    private int type;
    private boolean isSelected;
    private int mode;
    private int recordingTime;
    private int snapNumber;

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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getSnapNumber() {
        return snapNumber;
    }

    public void setSnapNumber(int snapNumber) {
        this.snapNumber = snapNumber;
    }
}
