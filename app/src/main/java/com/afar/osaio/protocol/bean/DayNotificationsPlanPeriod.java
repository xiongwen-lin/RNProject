package com.afar.osaio.protocol.bean;

import androidx.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class DayNotificationsPlanPeriod {
    Week week;
    int start;
    int startHour;
    int startMinutes;
    int end;
    int endHour;
    int endMinutes;
    boolean open;

    public static DayNotificationsPlanPeriod create(@NonNull Week week, @NonNull Calendar start, @NonNull Calendar end) {
        return create(week, start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE));
    }

    public static DayNotificationsPlanPeriod create(@NonNull Week week, int startH, int startM, int endH, int endM) {
        DayNotificationsPlanPeriod period = null;
        if (startH < 0 || startH > 24 || endH < 0 || endM > 24) {
            return period;
        } else {
            return new DayNotificationsPlanPeriod(week, startH * 60 + startM, endH * 60 + endM);
        }
    }

    public DayNotificationsPlanPeriod(Week week, int start, int end) {
        this.start = start;
        this.end = end;
        this.open = false;

        this.startHour = start / 60;
        this.startMinutes = start % 60;

        this.endHour = start / 60;
        this.endMinutes = start % 60;
        this.week = week;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        this.startHour = start / 60;
        this.startMinutes = start % 60;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
        this.endHour = start / 60;
        this.endMinutes = start % 60;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Week getWeek() {
        return week;
    }

    public void setWeek(Week week) {
        this.week = week;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("open:" + (open ? "true" : "false"));
        builder.append(" start:" + start);
        builder.append(" startHour:" + startHour);
        builder.append(" startMinutes:" + startMinutes);
        builder.append(" end:" + end);
        builder.append(" endHour:" + endHour);
        builder.append(" endMinutes:" + endMinutes);
        return new String(builder);
    }
}
