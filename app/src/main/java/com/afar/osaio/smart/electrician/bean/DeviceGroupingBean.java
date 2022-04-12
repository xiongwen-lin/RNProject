package com.afar.osaio.smart.electrician.bean;

import java.io.Serializable;
import java.util.List;

public class DeviceGroupingBean implements Serializable {

    private String groupingTitle;
    private List<DeviceTypeBean> deviceTypeBeanList;

    public String getGroupingTitle() {
        return groupingTitle;
    }

    public void setGroupingTitle(String groupingTitle) {
        this.groupingTitle = groupingTitle;
    }

    public List<DeviceTypeBean> getDeviceTypeBeanList() {
        return deviceTypeBeanList;
    }

    public void setDeviceTypeBeanList(List<DeviceTypeBean> deviceTypeBeanList) {
        this.deviceTypeBeanList = deviceTypeBeanList;
    }
}
