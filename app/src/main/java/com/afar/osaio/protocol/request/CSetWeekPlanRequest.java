package com.afar.osaio.protocol.request;

import androidx.annotation.NonNull;

import com.afar.osaio.protocol.bean.Constant;
import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;

import java.util.List;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class CSetWeekPlanRequest extends CRequestImple {
    private List<DayNotificationsPlanPeriod> plans;

    public CSetWeekPlanRequest(@NonNull List<DayNotificationsPlanPeriod> plans) {
        super(Constant.CMD_SET, Constant.KEY_NOTIFICATION_WEEK_PLAN);
        this.plans = plans;
        this.len = 0x1E;
    }

    public CSetWeekPlanRequest(@NonNull List<DayNotificationsPlanPeriod> plans, int key) {
        super(Constant.CMD_SET, key);
        this.plans = plans;
        this.len = 0x1E;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result && plans.size() >= 7) {
            for (int i = 0; i < 7; i++) {
                data[3 + i * 4] = (byte) ((plans.get(i).getStart() & 0xFF00) >> 8);
                data[4 + i * 4] = (byte) (plans.get(i).getStart() & 0xFF);
                data[5 + i * 4] = (byte) ((plans.get(i).getEnd() & 0xFF00) >> 8);
                data[6 + i * 4] = (byte) (plans.get(i).getEnd() & 0xFF);
            }
        } else {
            return false;
        }
        return true;
    }

}
