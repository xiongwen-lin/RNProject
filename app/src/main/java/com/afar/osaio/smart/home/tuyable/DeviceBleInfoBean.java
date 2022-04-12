package com.afar.osaio.smart.home.tuyable;

import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.home.sdk.bean.ConfigProductInfoBean;

/**
 * 扫描蓝牙+设备设置信息
 */
public class DeviceBleInfoBean  {
     private ScanDeviceBean scanDeviceBean;
     private ConfigProductInfoBean configProductInfoBean;

    public DeviceBleInfoBean() {
    }

    public DeviceBleInfoBean(ScanDeviceBean scanDeviceBean, ConfigProductInfoBean configProductInfoBean) {
        this.scanDeviceBean = scanDeviceBean;
        this.configProductInfoBean = configProductInfoBean;
    }

    public ScanDeviceBean getScanDeviceBean() {
        return scanDeviceBean;
    }

    public void setScanDeviceBean(ScanDeviceBean scanDeviceBean) {
        this.scanDeviceBean = scanDeviceBean;
    }

    public ConfigProductInfoBean getConfigProductInfoBean() {
        return configProductInfoBean;
    }

    public void setConfigProductInfoBean(ConfigProductInfoBean configProductInfoBean) {
        this.configProductInfoBean = configProductInfoBean;
    }
}
