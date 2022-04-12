package com.afar.osaio.smart.device.listener;

public interface ConnectShortLinkDeviceListener {

    void onResult(int code, String taskId, String account, String deviceId);
}
