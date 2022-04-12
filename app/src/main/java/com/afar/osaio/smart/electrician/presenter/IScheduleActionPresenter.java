package com.afar.osaio.smart.electrician.presenter;

import java.util.Map;

public interface IScheduleActionPresenter {

    void setScheduleAtion(String taskName, String devId, String loops, Map<String, Object> dps, String time);

}
