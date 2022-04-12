package com.afar.osaio.smart.setting.view;

import com.afar.osaio.bean.DetectionSchedule;

import java.util.List;

public interface INooieDetectionScheduleView {

    void notifyGetDetectionSchedulesSuccess(int detectType, List<DetectionSchedule> schedules, boolean isHideLoading);

    void notifyGetDetectionSchedulesFailed(int detectType, boolean isHideLoading);

    void notifySetDetectionSchedulesResult(String result);

    void displayLoading(boolean show);
}
