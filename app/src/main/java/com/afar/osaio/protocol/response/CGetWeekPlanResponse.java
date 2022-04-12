package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;
import com.afar.osaio.protocol.bean.Week;
import com.afar.osaio.protocol.ProtocolHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CGetWeekPlanResponse extends CResponseImpl {
    private List<DayNotificationsPlanPeriod> plans = new ArrayList<>();

    public List<DayNotificationsPlanPeriod> getPlans() {
        return plans;
    }

    public void setPlans(List<DayNotificationsPlanPeriod> plans) {
        this.plans = plans;
    }

    @Override
    public Object getValue() {
        return plans;
    }

    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            int start, end;
            for (int i = 0; i < 7; i++) {
                start = ProtocolHelper.bytesToInt(data, 3 + i * 4, 2);
                end = ProtocolHelper.bytesToInt(data, 5 + i * 4, 2);
                plans.add(new DayNotificationsPlanPeriod(Week.getWeek(i), start, end));
            }
        } else {
            return result;
        }
        return true;
    }
}
