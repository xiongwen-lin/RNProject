package com.afar.osaio.bean;

import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetectionSchedule implements Serializable {

    private int id;
    private List<Integer> weekDays = new ArrayList<>();
    private int start;
    private int startH;
    private int startM;
    private int end;
    private int endH;
    private int endM;
    boolean open;
    boolean effective;
    private String scheduleType = "";

    // router
    private String deviceName;
    private String deviceMac;

    public DetectionSchedule(int startTime, int endTime, boolean open) {
        start = startTime;
        startH = startTime/60;
        startM = startTime%60;
        end = endTime;
        endH = endTime/60;
        endM = endTime%60;
        this.open = open;
    }

    public DetectionSchedule(List<Integer> weekDays, int start, int end, boolean open, String deviceName, String deviceMac) {
        this.weekDays = weekDays;
        this.start = start;
        this.end = end;
        this.open = open;
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
    }

    public void resetWeekDays(boolean isDefault) {
        if (weekDays == null) {
            weekDays = new ArrayList<>();
        }
        weekDays.clear();
        if (isDefault) {
            weekDays.add(Calendar.MONDAY);
            weekDays.add(Calendar.TUESDAY);
            weekDays.add(Calendar.WEDNESDAY);
            weekDays.add(Calendar.THURSDAY);
            weekDays.add(Calendar.FRIDAY);
            weekDays.add(Calendar.SATURDAY);
            weekDays.add(Calendar.SUNDAY);
        }
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(List<Integer> weekDays) {
        this.weekDays.clear();
        if (CollectionUtil.isNotEmpty(weekDays) && weekDays.size() <= 7) {
            this.weekDays.addAll(weekDays);
        }
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        startH = start/60;
        startM = start%60;
    }

    public int getStartH() {
        return startH;
    }

    public void setStartH(int startH) {
        this.startH = startH;
    }

    public int getStartM() {
        return startM;
    }

    public void setStartM(int startM) {
        this.startM = startM;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
        endH = end/60;
        endM = end%60;
    }

    public int getEndH() {
        return endH;
    }

    public void setEndH(int endH) {
        this.endH = endH;
    }

    public int getEndM() {
        return endM;
    }

    public void setEndM(int endM) {
        this.endM = endM;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isEffective() {
        return effective;
    }

    public void setEffective(boolean effective) {
        this.effective = effective;
    }

    public String convertString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(getId());
        sb.append(" start=");
        sb.append(getStart());
        sb.append(" end=");
        sb.append(getEnd());
        sb.append(" open=");
        sb.append(isOpen());
        sb.append(" effective=");
        sb.append(isEffective());
        sb.append(" weekdays=");
        for (int i = 0; i < 7; i++) {
            sb.append(getWeekDays() != null && getWeekDays().contains(DateTimeUtil.convertWeekDay(i)) ? 1 : 0);
        }
        return sb.toString();
    }

    public void clear() {
        if (weekDays != null) {
            weekDays.clear();
            weekDays = null;
        }
    }
}
