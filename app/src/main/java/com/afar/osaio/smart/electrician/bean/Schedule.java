package com.afar.osaio.smart.electrician.bean;

/**
 * Schedule
 *
 * @author Administrator
 * @date 2019/3/26
 */
public class Schedule {

    private int scheduleState;
    //开始时间
    private String timeOn;
    //结束时间
    private String timeOff;
    //判断开关
    private boolean open;
    //定时星期的描述
    private String des;
    //是否循环定时
    private boolean isCycleTime;
    //十六进制字符串
    private String hexStr;

    private boolean isShowCycleTime;

    private boolean countDownConflict;

    public String getTimeOn() {
        return timeOn;
    }

    public void setTimeOn(String timeOn) {
        this.timeOn = timeOn;
    }

    public String getTimeOff() {
        return timeOff;
    }

    public void setTimeOff(String timeOff) {
        this.timeOff = timeOff;
    }

    public int getScheduleState() {
        return scheduleState;
    }

    public void setScheduleState(int scheduleState) {
        this.scheduleState = scheduleState;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public boolean isCycleTime() {
        return isCycleTime;
    }

    public void setCycleTime(boolean cycleTime) {
        isCycleTime = cycleTime;
    }

    public String getHexStr() {
        return hexStr;
    }

    public void setHexStr(String hexStr) {
        this.hexStr = hexStr;
    }

    public boolean isCountDownConflict() {
        return countDownConflict;
    }

    public void setCountDownConflict(boolean countDownConflict) {
        this.countDownConflict = countDownConflict;
    }

    public boolean isShowCycleTime() {
        return isShowCycleTime;
    }

    public void setShowCycleTime(boolean showCycleTime) {
        isShowCycleTime = showCycleTime;
    }
}
