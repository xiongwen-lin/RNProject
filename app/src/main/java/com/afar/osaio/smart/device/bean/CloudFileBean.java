package com.afar.osaio.smart.device.bean;

public class CloudFileBean {

    private String deviceId;
    private String userId;
    private String fileType;
    private String picType;
    private long startTime;
    private int expiration;
    private int bindType;
    private String preSignUrl;
    private String fileUrl;
    private int motionDetectionTime;
    private int soundDetectionTime;
    private int pirDetectionTime;
    private long baseTime;
    private int recordType;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPicType() {
        return picType;
    }

    public void setPicType(String picType) {
        this.picType = picType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public int getBindType() {
        return bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }

    public String getPreSignUrl() {
        return preSignUrl;
    }

    public void setPreSignUrl(String preSignUrl) {
        this.preSignUrl = preSignUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public int getMotionDetectionTime() {
        return motionDetectionTime;
    }

    public void setMotionDetectionTime(int motionDetectionTime) {
        this.motionDetectionTime = motionDetectionTime;
    }

    public int getSoundDetectionTime() {
        return soundDetectionTime;
    }

    public void setSoundDetectionTime(int soundDetectionTime) {
        this.soundDetectionTime = soundDetectionTime;
    }

    public int getPirDetectionTime() {
        return pirDetectionTime;
    }

    public void setPirDetectionTime(int pirDetectionTime) {
        this.pirDetectionTime = pirDetectionTime;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }
}
