package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CSetCommonRequest extends CRequestImple {
    private boolean on;

    public CSetCommonRequest(int key, boolean on) {
        super(Constant.CMD_SET, key);
        this.on = on;
        this.len = 3;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result) {
            data[3] = on ? Constant.VALUE_ON : Constant.VALUE_OFF;
        } else {
            return false;
        }
        return true;
    }
}
