package com.afar.osaio.smart.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * BaseMediaBean
 *
 * @author Administrator
 * @date 2019/10/5
 */
public abstract class BaseMediaBean implements Parcelable {
    public enum TYPE {
        IMAGE, VIDEO
    }

    protected String mPath;
    protected String mId;
    protected String mSize;
    protected long mTime;

    public BaseMediaBean() {
    }

    public BaseMediaBean(String id, String path) {
        mId = id;
        mPath = path;
    }

    public abstract TYPE getType();

    public String getId() {
        return mId;
    }

    public long getSize() {
        try {
            long result = Long.parseLong(mSize);
            return result > 0 ? result : 0;
        }catch (NumberFormatException size) {
            return 0;
        }
    }

    public void setId(String id) {
        mId = id;
    }

    public void setSize(String size) {
        mSize = size;
    }

    public String getPath(){
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public long getTime() {
        return mTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPath);
        dest.writeString(this.mId);
        dest.writeString(this.mSize);
        dest.writeLong(this.mTime);
    }

    protected BaseMediaBean(Parcel in) {
        this.mPath = in.readString();
        this.mId = in.readString();
        this.mSize = in.readString();
        this.mTime = in.readLong();
    }

}
