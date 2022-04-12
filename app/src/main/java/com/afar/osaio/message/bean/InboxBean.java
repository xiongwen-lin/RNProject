package com.afar.osaio.message.bean;

import java.io.Serializable;

public class InboxBean implements Serializable {

    private String id;
    private String name;
    private int unreadCount;

    public InboxBean(String id, String name, int unreadCount) {
        this.id = id;
        this.name = name;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

}
