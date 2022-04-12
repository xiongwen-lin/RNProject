package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

import com.afar.osaio.protocol.ProtocolHelper;
import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;
import com.afar.osaio.protocol.bean.Week;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CGetDayPlanResponse extends CResponseImpl {
    private DayNotificationsPlanPeriod plan;

    public DayNotificationsPlanPeriod getPlan() {
        return plan;
    }

    public void setPlan(DayNotificationsPlanPeriod plan) {
        this.plan = plan;
    }

    @Override
    public Object getValue() {
        return plan;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            int start = ProtocolHelper.bytesToInt(data, 3, 2);
            int end = ProtocolHelper.bytesToInt(data, 5, 2);
            Week week = ProtocolHelper.weekByNotificationDayPlanKey(key);
            plan = new DayNotificationsPlanPeriod(week, start, end);
        } else {
            return result;
        }
        return true;
    }
}
