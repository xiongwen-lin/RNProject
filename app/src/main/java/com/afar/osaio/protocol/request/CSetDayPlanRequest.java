package com.afar.osaio.protocol.request;

import androidx.annotation.NonNull;

import com.afar.osaio.protocol.bean.Constant;
import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CSetDayPlanRequest extends CRequestImple {
    private DayNotificationsPlanPeriod plan;

    public CSetDayPlanRequest(@NonNull int key, @NonNull DayNotificationsPlanPeriod plan) {
        super(Constant.CMD_SET, key);
        this.plan = plan;
        this.len = 6;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result) {
            data[3] = (byte) ((plan.getStart() & 0xFF00) >> 8);
            data[4] = (byte) (plan.getStart() & 0xFF);
            data[5] = (byte) ((plan.getEnd() & 0xFF00) >> 8);
            data[6] = (byte) (plan.getEnd() & 0xFF);
        } else {
            return false;
        }
        return true;
    }
}
