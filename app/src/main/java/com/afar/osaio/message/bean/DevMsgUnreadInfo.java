package com.afar.osaio.message.bean;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class DevMsgUnreadInfo {
    private String id;
    private int unreadCount;

    public DevMsgUnreadInfo(String id, int unreadCount) {
        this.id = id;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
