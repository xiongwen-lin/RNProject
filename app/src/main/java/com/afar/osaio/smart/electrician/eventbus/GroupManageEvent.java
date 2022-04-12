package com.afar.osaio.smart.electrician.eventbus;

/**
 *  群组管理设备-- 群组添加或移除设备
 *  群组名字改变
 */
public class GroupManageEvent {

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    private boolean isChange;

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    private long groupId;

    public GroupManageEvent(boolean isChange, long groupId) {
        this.isChange = isChange;
        this.groupId = groupId;
    }


}

