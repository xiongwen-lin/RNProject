package com.afar.osaio.message.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public abstract class AbstractMessage implements Comparable<AbstractMessage>, Serializable {
    protected String id;
    protected long utcTime;

    public AbstractMessage() {
    }

    public AbstractMessage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(long utcTime) {
        this.utcTime = utcTime;
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "id=" + id +
                ", utcTime=" + utcTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractMessage that = (AbstractMessage) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * 时间晚的（消息新的）排在前面（return负数）
     */
    @Override
    public int compareTo(AbstractMessage another) {
        if (another == null || TextUtils.isEmpty(another.getId())) {
            return 1;
        }
        if (this.utcTime > another.getUtcTime()) {
            return -1;
        } else if (this.utcTime < another.getUtcTime()) {
            return 1;
        } else {
            return 0;
        }
    }
}