package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CUnknownResponse extends CResponseImpl {
    private byte payload[];

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public Object getValue() {
        return payload;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            payload = new byte[dataLen];
            System.arraycopy(data, data.length - dataLen, payload, 0, dataLen);
        } else {
            return result;
        }
        return true;
    }
}
