package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CGetCommonRequest extends CRequestImple {

    public CGetCommonRequest(int key) {
        super(Constant.CMD_GET, key);
        this.len = 2;
    }
}
