package com.afar.osaio.smart.electrician.bean;

import com.tuya.smart.sdk.bean.Timer;

public class PowerStripScheduleBean {

    private Timer timerBean;
    private Schedule scheduleBean;

    public PowerStripScheduleBean(Timer timerBean) {
        this.timerBean = timerBean;
        this.scheduleBean = null;
    }

    public PowerStripScheduleBean(Schedule scheduleBean) {
        this.scheduleBean = scheduleBean;
        this.timerBean = null;
    }

    public boolean isTimerBean() {
        return this.timerBean != null;
    }

    public Timer getTimerBean() {
        return timerBean;
    }

    public void setTimerBean(Timer timerBean) {
        this.timerBean = timerBean;
        this.scheduleBean = null;
    }

    public Schedule getScheduleBean() {
        return scheduleBean;
    }

    public void setScheduleBean(Schedule scheduleBean) {
        this.scheduleBean = scheduleBean;
        this.timerBean = null;
    }


}
