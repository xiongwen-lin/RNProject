package com.afar.osaio.bean;

import android.bluetooth.BluetoothDevice;

public class BluetoothCmdMonitorBean {

    BluetoothDevice device;
    private long time;
    private String cmdRspKey;
    private boolean isSuccess;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCmdRspKey() {
        return cmdRspKey;
    }

    public void setCmdRspKey(String cmdRspKey) {
        this.cmdRspKey = cmdRspKey;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
