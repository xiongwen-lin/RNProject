package com.afar.osaio.message.bean;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public enum SystemMessageType {
    // TODO 我作为分享者，会收到哪些系统消息
    /**
     * 我分享设备给别人，别人接受了
     */
    OTHER_ACCEPT_MY_SHARE,
    /**
     * 我分享设备给别人，别人拒绝了
     */
    OTHER_REJECT_MY_SHARE,
    /**
     * 我分享设备给别人，别人接受后取消分享（通过删除被分享设备）
     */
    OTHER_ACCEPT_THEN_CANCEL,


    // TODO 我作为被分享者，会收到哪些系统消息
    /**
     * 别人分享设备给我，我还未处理，我这边显示"拒绝" or "同意"
     */
    OTHER_SHARE_DEVICE_TO_ME,
    /**
     * 别人分享设备给我，我同意之后别人取消了分享（通过取消分享或者删除被分享设备）
     */
    OTHER_CANCEL_SHARE_TO_ME,


    // TODO 申请分享设备（暂不做）
    /**
     * 我向别人申请分享他的设备，我这边显示"等待别人确认"
     */
    APPLY_SHARE_OTHER_DEVICE,
    /**
     * 别人向我申请分享我的设备，我这边显示"拒绝" or "同意"
     */
    OTHER_APPLY_SHARE_MY_DEVICE,


    // TODO 申请转让设备（暂不做）
    /**
     * 我向别人申请拥有他的设备，我这边显示"等待别人确认"
     */
    APPLY_OWN_OTHER_DEVICE,
    /**
     * 别人向我申请拥有我的设备，我这边显示"拒绝" or "同意"
     */
    OTHER_APPLY_OWN_MY_DEVICE,

    // TODO　其他系统消息（实质不是系统消息，是告警消息或其他）
    /**
     * 某个设备的云服务还有三天到期
     */
    CLOUD_SERVICE_WILL_EXPIRE,
    /**
     * 某个设备有新固件可供升级
     */
    FIRMWARE_UPGRADE,
    /**
     * 某个设备已离线
     */
    DEVICE_OFFLINE,
    /**
     * 某个设备电量较低
     */
    LOW_POWER,
    /**
     * APP有新版本可供升级
     */
    APP_UPGRADE
}
