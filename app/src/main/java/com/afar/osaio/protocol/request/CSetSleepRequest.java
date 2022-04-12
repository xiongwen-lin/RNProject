package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CSetSleepRequest extends CRequestImple {
    private int state;

    public CSetSleepRequest(int key, int value) {
        super(Constant.CMD_SET, key);
        this.state = value;
        this.len = 3;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result) {
            data[3] = (byte)state;
        } else {
            return false;
        }
        return true;
    }
}
