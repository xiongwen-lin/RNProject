package com.afar.osaio.smart.electrician.bean;

import com.tuya.smart.sdk.bean.Timer;

public class MixScheduleBean {

    private Schedule scheduleBean;
    private Timer timerBean;


    public MixScheduleBean() {

    }

    public MixScheduleBean(Timer timerBean) {
        this.timerBean = timerBean;
    }

    public Schedule getScheduleBean() {
        return scheduleBean;
    }

    public void setScheduleBean(Schedule scheduleBean) {
        this.scheduleBean = scheduleBean;
        this.timerBean = null;
    }

    public Timer getTimerBean() {
        return timerBean;
    }

    public void setTimerBean(Timer timerBean) {
        this.timerBean = timerBean;
        this.scheduleBean = null;
    }

    public boolean isTimerBean() {
        return this.timerBean != null;
    }

}
