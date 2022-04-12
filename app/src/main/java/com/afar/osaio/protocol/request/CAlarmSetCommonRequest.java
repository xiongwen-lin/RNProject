package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CAlarmSetCommonRequest extends CRequestImple {
    private boolean on;
    private int id;
    private int time;
    private int num;

    public CAlarmSetCommonRequest(boolean on, int id, int time, int num) {
        super(Constant.CMD_SET, Constant.KEY_CAMERA_ALRAM_AUDIO);
        this.on = on;
        this.id = id;
        this.time = time;
        this.num = num;
        this.len = 6;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result) {
            data[3] = on ? Constant.VALUE_ON : Constant.VALUE_OFF;
            data[4] = (byte)id;
            data[5] = (byte)time;
            data[6] = (byte)num;
        } else {
            return false;
        }
        return true;
    }
}
