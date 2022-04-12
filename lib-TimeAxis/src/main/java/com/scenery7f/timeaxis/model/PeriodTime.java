package com.scenery7f.timeaxis.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 记录时间段
 * Created by snoopy on 2017/9/15.
 */

public class PeriodTime {
    private Calendar startTime;
    private Calendar stopTime;
    private int color;
    private RecordType recordType;
    private List<RecordType> recordTypes;

    public PeriodTime(Calendar startTime, Calendar stopTime, int color) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.color = color;
    }

    public PeriodTime(Calendar startTime, Calendar stopTime, int color, RecordType recordType) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.color = color;
        this.recordType = recordType;
    }

    public PeriodTime(Calendar startTime, Calendar stopTime, int color, RecordType recordType , List<RecordType> recordTypes) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.color = color;
        this.recordType = recordType;
        this.recordTypes = recordTypes;
    }

    public boolean inThisPeriod(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        return calendar.compareTo(startTime) >= 0 && calendar.compareTo(stopTime) <= 0;
    }

    public boolean inThisPeriodUtc(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(time);

        return calendar.compareTo(startTime) >= 0 && calendar.compareTo(stopTime) <= 0;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getStopTime() {
        return stopTime;
    }

    public void setStopTime(Calendar stopTime) {
        this.stopTime = stopTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public List<RecordType> getRecordTypes() {
        return recordTypes;
    }

    public void setRecordTypes(List<RecordType> recordTypeList) {
        this.recordTypes = recordTypeList;
    }
}
