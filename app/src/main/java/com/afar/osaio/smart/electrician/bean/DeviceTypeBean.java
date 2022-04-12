package com.afar.osaio.smart.electrician.bean;

import java.io.Serializable;

public class DeviceTypeBean implements Serializable {

    private String deviceName;
    private int devicePic;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDevicePic() {
        return devicePic;
    }

    public void setDevicePic(int devicePic) {
        this.devicePic = devicePic;
    }
}
