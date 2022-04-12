package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CRequestImple extends CRequest {
    protected int len;
    protected int action;
    protected int key;
    protected int dataLen;
    protected byte[] data;

    public CRequestImple(int action, int key) {
        this.action = action;
        this.key = key;
    }

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

    @Override
    public boolean buildCmd() {
        if (len < Constant.LEN_BYTES_KEY + Constant.LEN_BYTES_ACTION) {
            return false;
        }

        data = new byte[len + 1];
        data[0] = (byte) (len & 0x000000FF);
        data[1] = (byte) (action & 0x000000FF);
        data[2] = (byte) (key & 0x000000FF);
        return true;
    }

    @Override
    public byte[] data() {
        return data;
    }
}
