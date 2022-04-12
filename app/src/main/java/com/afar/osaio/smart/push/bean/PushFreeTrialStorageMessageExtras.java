package com.afar.osaio.smart.push.bean;

/**
 * JPushFeedbackMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushFreeTrialStorageMessageExtras extends PushMessageBaseExtras {

    private String device_name;
    private String uuid;
    private int uid;
    private long expire_date;
    private int file_time;
    private int duration;
    private String time_unit;

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(long expire_date) {
        this.expire_date = expire_date;
    }

    public int getFile_time() {
        return file_time;
    }

    public void setFile_time(int file_time) {
        this.file_time = file_time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTime_unit() {
        return time_unit;
    }

    public void setTime_unit(String time_unit) {
        this.time_unit = time_unit;
    }
}
