package com.afar.osaio.smart.device.bean;

import com.nooie.common.utils.time.DateTimeUtil;
import com.scenery7f.timeaxis.model.RecordType;

import java.util.List;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class CloudRecordInfo {
    private String mDeviceId;
    private int mChanNo;
    private long mStartTime;
    private long mTimeLen;
    private RecordType mRecordType;
    private List<RecordType> recordTypes;
    private CloudFileBean cloudFileBean;

    public CloudRecordInfo() {
        super();
    }

    public CloudRecordInfo(String deviceId, int chanNo, long startTime,
                           long timeLen, RecordType recordType) {
        super();
        this.mDeviceId = deviceId;
        this.mChanNo = chanNo;
        this.mStartTime = startTime;
        this.mTimeLen = changeTimeLen(startTime, timeLen);
        this.mRecordType = recordType;
    }

    public CloudRecordInfo(String deviceId, int chanNo, long startTime, long timeLen, RecordType recordType, boolean isUtcTime) {
        super();
        this.mDeviceId = deviceId;
        this.mChanNo = chanNo;
        this.mStartTime = startTime;
        this.mTimeLen = changeUtcTimeLen(startTime, timeLen);
        this.mRecordType = recordType;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public int getChanNo() {
        return mChanNo;
    }

    public void setChanNo(int chanNo) {
        this.mChanNo = chanNo;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public long getTimeLen() {
        return mTimeLen;
    }

    public void setTimeLen(long timeLen) {
        this.mTimeLen = timeLen;
    }

    public RecordType getRecordType() {
        return mRecordType;
    }

    public void setRecordType(RecordType recordType) {
        this.mRecordType = recordType;
    }

    public List<RecordType> getRecordTypes() {
        return recordTypes;
    }

    public void setRecordTypes(List<RecordType> recordTypeList) {
        this.recordTypes = recordTypeList;
    }

    public CloudFileBean getCloudFileBean() {
        return cloudFileBean;
    }

    public void setCloudFileBean(CloudFileBean cloudFileBean) {
        this.cloudFileBean = cloudFileBean;
    }

    private long changeTimeLen(long startTime, long timeLen) {
        long endTime = DateTimeUtil.getDayStartTimeStamp(startTime) + (24 * 60 * 60 - 1) * 1000;
        //NooieLog.d("-->> CloudRecordInfo changeTimeLen compare net=" + DateTimeUtil.getUtcTimeString((startTime + timeLen), DateTimeUtil.PATTERN_HMS) + " real=" + DateTimeUtil.getUtcTimeString((DateTimeUtil.getDayStartTimeStamp(startTime) + (24 * 60 * 60 - 1) * 1000), DateTimeUtil.PATTERN_HMS));
        if ((startTime + timeLen) > endTime) {
            return Math.min(endTime, startTime + timeLen) - startTime;
        } else {
            return timeLen;
        }
        //return Math.min(endTime, startTime + timeLen) - startTime;
    }

    private long changeUtcTimeLen(long startTime, long timeLen) {
        long endTime = DateTimeUtil.getUtcDayStartTimeStamp(startTime) + (24 * 60 * 60 - 1) * 1000;
        //NooieLog.d("-->> CloudRecordInfo changeUtcTimeLen compare net=" + DateTimeUtil.getUtcTimeString((startTime + timeLen), DateTimeUtil.PATTERN_HMS) + " real=" + DateTimeUtil.getUtcTimeString(endTime, DateTimeUtil.PATTERN_HMS));
        if ((startTime + timeLen) > endTime) {
            return Math.min(endTime, startTime + timeLen) - startTime;
        } else {
            return timeLen;
        }
    }
}

