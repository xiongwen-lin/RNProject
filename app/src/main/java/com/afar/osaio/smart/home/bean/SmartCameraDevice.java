package com.afar.osaio.smart.home.bean;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/28 4:56 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class SmartCameraDevice extends SmartBaseDevice {

    /** 设备云状态
     *  ACTIVE 开通
     *  NONE 未开通
     */
    public String cloudState = "";
    /** 设备ssid */
    public String deviceSsid;
    /** 设备蓝牙mac地址，作为蓝牙设备id */
    public String bleDeviceId;
    /** Ipc设备链接模式，目前有WiFi（远程）、NetSpot（热点直连）两种模式
     * connectionMode 链接模式WIFI、NET_SPOT
     */
    public String deviceInfoType = "";

}
