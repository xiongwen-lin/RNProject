package com.afar.osaio.smart.electrician.bean;

import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

/**
 * MixDeviceBean
 *
 * @author Administrator
 * @date 2019/3/22
 */
public class MixDeviceBean {

    private GroupBean groupBean;
    private DeviceBean deviceBean;

    public GroupBean getGroupBean() {
        return groupBean;
    }

    public void setGroupBean(GroupBean groupBean) {
        this.groupBean = groupBean;
        this.deviceBean = null;
    }

    public DeviceBean getDeviceBean() {
        return deviceBean;
    }

    public void setDeviceBean(DeviceBean deviceBean) {
        this.deviceBean = deviceBean;
        this.groupBean = null;
    }

    public boolean isGroupBean() {
        return this.groupBean != null;
    }
}
