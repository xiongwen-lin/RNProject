package com.afar.osaio.widget;

import java.util.Calendar;

/**
 * Created by victor on 2018/7/28
 * Email is victor.qiao.0604@gmail.com
 */
public class CalenderBean {
    private Calendar calendar;
    private boolean selected;
    private boolean haveSDCardRecord;

    public CalenderBean(Calendar calendar, boolean selected) {
        this.calendar = calendar;
        this.selected = selected;
        this.haveSDCardRecord = true;
    }

    public CalenderBean(Calendar calendar, boolean selected, boolean haveSDCardRecord) {
        this.calendar = calendar;
        this.selected = selected;
        this.haveSDCardRecord = haveSDCardRecord;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHaveSDCardRecord() {
        return haveSDCardRecord;
    }

    public void setHaveSDCardRecord(boolean haveSDCardRecord) {
        this.haveSDCardRecord = haveSDCardRecord;
    }
}