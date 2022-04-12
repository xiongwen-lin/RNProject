package com.afar.osaio.smart.home.bean;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/28 3:59 下午
 * 说明:
 *
 * 备注:
 *
 *
 ***********************************************************/
public class SmartBaseDevice {

    /** deviceCategory 设备品类
     * CAMERA
     * APPLIANCES
     * ELECTRICIAN
     * LIGHT
     * ROUTER
     */
    public String deviceCategory = null;
    /** deviceSubCategory 设备子品类
     * NORMAL 通用类型，默认值
     */
    public String deviceSubCategory = null;
    /** deviceId 设备id */
    public String deviceId = null;
    /** 设备型号 */
    public String model = null;
    /** 父设备id */
    public String parentDeviceId = null;
    /** deviceName 设备名 */
    public String deviceName = null;
    /** deviceState 设备在线状态
     *  ONLINE 在线
     *  OFFLINE 离线
     */
    public String deviceState = null;
    /** deviceSwitchState 设备开关状态
     *  ON 开
     *  OFF 关
     */
    public String deviceSwitchState = null;
    /** 设备绑定类型
     * OWNER 所有者
     * SHARER 分享者
     */
    public String bindType = "";
    /** deviceIconUrl 设备图标链接
     *  文件或web链接
     */
    public String deviceIconUrl = null;
    /** 设备版本时间 */
    public long deviceBindTime = 0L;

    @Override
    public String toString() {
        return "SmartBaseDevice{" +
                "deviceCategory='" + deviceCategory + '\'' +
                ", deviceSubCategory='" + deviceSubCategory + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", model='" + model + '\'' +
                ", parentDeviceId='" + parentDeviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceState='" + deviceState + '\'' +
                ", deviceSwitchState='" + deviceSwitchState + '\'' +
                ", bindType='" + bindType + '\'' +
                ", deviceIconUrl='" + deviceIconUrl + '\'' +
                ", deviceBindTime=" + deviceBindTime +
                '}';
    }
}
