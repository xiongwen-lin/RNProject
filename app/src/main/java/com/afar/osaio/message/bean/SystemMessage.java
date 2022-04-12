package com.afar.osaio.message.bean;

import android.graphics.drawable.Drawable;

import com.nooie.sdk.api.network.base.bean.entity.Message;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public class SystemMessage extends AbstractMessage {
    private SystemMessageType messageType;
    private Drawable icon; // 由消息类型决定
    private String title;
    private String content;
    private String deviceId;
    private boolean showAgreeReject; // 是否显示"同意" or "拒绝"的布局
    private String state; // 消息的处理状态

    //nooie smart new value
    private int devicePlatform;
    private int shareId;
    private Message message;

    public SystemMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(SystemMessageType messageType) {
        this.messageType = messageType;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isShowAgreeReject() {
        return showAgreeReject;
    }

    public void setShowAgreeReject(boolean showAgreeReject) {
        this.showAgreeReject = showAgreeReject;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    //nooie smart
    public int getDevicePlatform() {
        return devicePlatform;
    }

    public void setDevicePlatform(int devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
