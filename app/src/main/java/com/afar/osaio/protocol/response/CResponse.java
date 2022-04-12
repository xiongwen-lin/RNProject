package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public abstract class CResponse {
    public abstract boolean parse(@NonNull byte data[]);
}
