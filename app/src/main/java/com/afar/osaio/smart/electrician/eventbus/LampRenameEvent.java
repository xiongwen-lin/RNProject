package com.afar.osaio.smart.electrician.eventbus;

/**
 *  智能灯命名通知
 */
public class LampRenameEvent {

    public LampRenameEvent(boolean isRename, String newName) {
        this.isRename = isRename;
        this.newName = newName;
    }

    private boolean isRename;

    private String newName;

    public boolean isRename() {
        return isRename;
    }

    public void setRename(boolean rename) {
        isRename = rename;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
