package com.afar.osaio.bean;

import java.io.Serializable;

public class DeviceSettingBean implements Serializable {

    private boolean isAudioRecOpen;
    private boolean isRotateOn;
    private int icr;
    private boolean isMotionTracking;

    public boolean isAudioRecOpen() {
        return isAudioRecOpen;
    }

    public void setAudioRecOpen(boolean audioRecOpen) {
        isAudioRecOpen = audioRecOpen;
    }

    public boolean isRotateOn() {
        return isRotateOn;
    }

    public void setRotateOn(boolean rotateOn) {
        isRotateOn = rotateOn;
    }

    public int getIcr() {
        return icr;
    }

    public void setIcr(int icr) {
        this.icr = icr;
    }

    public boolean isMotionTracking() {
        return isMotionTracking;
    }

    public void setMotionTracking(boolean motionTracking) {
        isMotionTracking = motionTracking;
    }
}
