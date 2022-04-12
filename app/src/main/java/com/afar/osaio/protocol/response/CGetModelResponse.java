package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CGetModelResponse extends CResponseImpl {
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public Object getValue() {
        return model;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            try {
                byte d[] = new byte[dataLen];
                System.arraycopy(data, 3, d, 0, dataLen);
                model = new String(d, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return result;
        }
        return true;
    }
}
