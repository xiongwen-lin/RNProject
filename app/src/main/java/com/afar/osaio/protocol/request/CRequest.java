package com.afar.osaio.protocol.request;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public abstract class CRequest {
    public abstract byte[] data();

    public abstract boolean buildCmd();
}
