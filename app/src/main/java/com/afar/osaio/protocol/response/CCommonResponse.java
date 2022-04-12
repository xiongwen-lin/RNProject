package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CCommonResponse extends CResponseImpl {
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public Object getValue() {
        return code;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            code = data[3];
        } else {
            return result;
        }
        return true;
    }
}
