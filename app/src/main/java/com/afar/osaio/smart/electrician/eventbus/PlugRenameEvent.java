package com.afar.osaio.smart.electrician.eventbus;

/**
 *  排插命名通知
 */
public class PlugRenameEvent {

    public PlugRenameEvent(boolean isRename, String dpId, String newName) {
        this.isRename = isRename;
        this.dpId = dpId;
        this.newName = newName;
    }

    private boolean isRename;

    private String dpId;

    private String newName;

    public boolean isRename() {
        return isRename;
    }

    public void setRename(boolean rename) {
        isRename = rename;
    }

    public String getDpId() {
        return dpId;
    }

    public void setDpId(String dpId) {
        this.dpId = dpId;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
