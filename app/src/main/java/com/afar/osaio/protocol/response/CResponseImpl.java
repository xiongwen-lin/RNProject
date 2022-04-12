package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CResponseImpl extends CResponse {
    protected int len;
    protected int action;
    protected int key;
    protected int dataLen;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public Object getValue() {
        return null;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        if (data.length < (Constant.LEN_BYTES_LEN + Constant.LEN_BYTES_ACTION + Constant.LEN_BYTES_KEY + 1)) {
            return false;
        }

        len = data[0] & 0xFF;
        action = data[1] & 0xFF;
        key = data[2] & 0xFF;
        dataLen = len - Constant.LEN_BYTES_ACTION - Constant.LEN_BYTES_KEY;
        return len == data.length - 1;
    }
}
