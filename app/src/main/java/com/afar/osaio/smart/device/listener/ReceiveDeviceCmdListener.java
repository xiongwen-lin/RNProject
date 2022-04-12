package com.afar.osaio.smart.device.listener;

/**
 * DeviceConnectionListener
 *
 * @author Administrator
 * @date 2020/8/3
 */
public interface ReceiveDeviceCmdListener {

    public void onReceiveDeviceCmdConnect(String action, String deviceId);
}