package com.afar.osaio.message.bean;

import java.util.List;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class MsgUnreadInfo {
    private int systemUnreadCount;
    private List<DevMsgUnreadInfo> devMsgUnreadInfos;

    public MsgUnreadInfo(int systemUnreadCount, List<DevMsgUnreadInfo> devMsgUnreadInfos) {
        this.systemUnreadCount = systemUnreadCount;
        this.devMsgUnreadInfos = devMsgUnreadInfos;
    }

    public int getSystemUnreadCount() {
        return systemUnreadCount;
    }

    public void setSystemUnreadCount(int systemUnreadCount) {
        this.systemUnreadCount = systemUnreadCount;
    }

    public List<DevMsgUnreadInfo> getDevMsgUnreadInfos() {
        return devMsgUnreadInfos;
    }

    public void setDevMsgUnreadInfos(List<DevMsgUnreadInfo> devMsgUnreadInfos) {
        this.devMsgUnreadInfos = devMsgUnreadInfos;
    }
}
