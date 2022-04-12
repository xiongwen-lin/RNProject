package com.afar.osaio.smart.electrician.bean;

/**
 * SwitchBean
 *
 * @author Administrator
 * @date 2019/3/8
 */
public class SwitchBean extends DpBean {
    
    private boolean open;

    public SwitchBean(String dpId) {
        setDpId(dpId);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
