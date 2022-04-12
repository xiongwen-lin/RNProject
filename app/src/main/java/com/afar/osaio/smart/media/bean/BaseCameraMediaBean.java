package com.afar.osaio.smart.media.bean;

import android.os.Parcel;

public abstract class BaseCameraMediaBean extends BaseMediaBean {

    protected String deviceId;
    protected long startTs;
    protected long startMs;
    protected String downloadUrl;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public long getStartMs() {
        return startMs;
    }

    public void setStartMs(long startMs) {
        this.startMs = startMs;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.deviceId);
        dest.writeLong(this.startTs);
        dest.writeLong(this.startMs);
        dest.writeString(this.downloadUrl);
    }

    public BaseCameraMediaBean() {
    }

    protected BaseCameraMediaBean(Parcel in) {
        super(in);
        this.deviceId = in.readString();
        this.startTs = in.readLong();
        this.startMs = in.readLong();
        this.downloadUrl = in.readString();
    }
}
