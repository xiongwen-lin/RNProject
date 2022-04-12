package com.afar.osaio.bean;

import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.FormatInfo;

public class BleApDeviceInfo {

    private String deviceId;
    private BleApDeviceEntity bleApDeviceEntity;
    private BindDevice bindDevice;
    private DevAllSettingsV2 devAllSettingsV2;
    private DevInfo devInfo;
    private FormatInfo formatInfo;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public BleApDeviceEntity getBleApDeviceEntity() {
        return bleApDeviceEntity;
    }

    public void setBleApDeviceEntity(BleApDeviceEntity bleApDeviceEntity) {
        this.bleApDeviceEntity = bleApDeviceEntity;
    }

    public BindDevice getBindDevice() {
        return bindDevice;
    }

    public void setBindDevice(BindDevice bindDevice) {
        this.bindDevice = bindDevice;
    }

    public DevAllSettingsV2 getDevAllSettingsV2() {
        return devAllSettingsV2;
    }

    public void setDevAllSettingsV2(DevAllSettingsV2 devAllSettingsV2) {
        this.devAllSettingsV2 = devAllSettingsV2;
    }

    public DevInfo getDevInfo() {
        return devInfo;
    }

    public void setDevInfo(DevInfo devInfo) {
        this.devInfo = devInfo;
    }

    public FormatInfo getFormatInfo() {
        return formatInfo;
    }

    public void setFormatInfo(FormatInfo formatInfo) {
        this.formatInfo = formatInfo;
    }
}
