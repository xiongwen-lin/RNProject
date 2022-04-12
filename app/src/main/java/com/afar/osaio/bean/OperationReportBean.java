package com.afar.osaio.bean;

/**
 * OperationReportBean
 *
 * @author Administrator
 * @date 2019/6/20
 */
public class OperationReportBean<T> {

    private String event;
    private String deviceId;
    private T description;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public T getDescription() {
        return description;
    }

    public void setDescription(T description) {
        this.description = description;
    }
}
